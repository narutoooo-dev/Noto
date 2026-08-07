package com.noto.app.util

import com.noto.app.domain.model.NotoColor

object MarkdownUtils {

    /**
     * تحويل نص Markdown إلى HTML
     */
    fun markdownToHtml(md: String): String {
        if (md.isBlank()) return "<p><em>Empty note</em></p>"

        val lines = md.split("\n")
        val sb = StringBuilder()
        var inCode = false
        var inList = false

        for (line in lines) {
            when {
                // Code blocks
                line.startsWith("```") -> {
                    if (inCode) {
                        sb.append("</code></pre>")
                        inCode = false
                    } else {
                        sb.append("<pre><code>")
                        inCode = true
                    }
                    continue
                }

                inCode -> {
                    sb.append(escapeHtml(line)).append("\n")
                    continue
                }

                // Headings
                line.startsWith("### ") -> {
                    closeListIfNeeded(inList) { inList = false }
                    sb.append("<h3>").append(processInlineMarkdown(line.substring(4))).append("</h3>")
                }

                line.startsWith("## ") -> {
                    closeListIfNeeded(inList) { inList = false }
                    sb.append("<h2>").append(processInlineMarkdown(line.substring(3))).append("</h2>")
                }

                line.startsWith("# ") -> {
                    closeListIfNeeded(inList) { inList = false }
                    sb.append("<h1>").append(processInlineMarkdown(line.substring(2))).append("</h1>")
                }

                // Blockquotes
                line.startsWith("> ") -> {
                    closeListIfNeeded(inList) { inList = false }
                    sb.append("<blockquote>").append(processInlineMarkdown(line.substring(2))).append("</blockquote>")
                }

                // Unordered lists
                line.startsWith("- ") || line.startsWith("* ") -> {
                    if (!inList) {
                        sb.append("<ul>")
                        inList = true
                    }
                    sb.append("<li>").append(processInlineMarkdown(line.substring(2))).append("</li>")
                }

                // Ordered lists
                line.matches(Regex("^\\d+\\..*")) -> {
                    if (!inList) {
                        sb.append("<ol>")
                        inList = true
                    }
                    sb.append("<li>").append(processInlineMarkdown(line.replaceFirst(Regex("\\d+\\. "), ""))).append("</li>")
                }

                // Horizontal rules
                line == "---" || line == "***" || line == "___" -> {
                    closeListIfNeeded(inList) { inList = false }
                    sb.append("<hr/>")
                }

                // Empty lines
                line.isBlank() -> {
                    closeListIfNeeded(inList) { inList = false }
                    sb.append("<br/>")
                }

                // Normal paragraphs
                else -> {
                    closeListIfNeeded(inList) { inList = false }
                    sb.append("<p>").append(processInlineMarkdown(line)).append("</p>")
                }
            }
        }

        // Close any open list
        closeListIfNeeded(inList) { inList = false }

        // Close any open code block
        if (inCode) {
            sb.append("</code></pre>")
        }

        return sb.toString()
    }

    /**
     * معالجة التنسيقات المضمنة في النص (Bold, Italic, Code, Strikethrough, Links)
     */
    private fun processInlineMarkdown(text: String): String {
        var processed = text

        // Bold: **text** or __text__
        processed = processed.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
        processed = processed.replace(Regex("__(.+?)__"), "<strong>$1</strong>")

        // Italic: *text* or _text_
        processed = processed.replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
        processed = processed.replace(Regex("_(.+?)_"), "<em>$1</em>")

        // Strikethrough: ~~text~~
        processed = processed.replace(Regex("~~(.+?)~~"), "<del>$1</del>")

        // Inline code: `text`
        processed = processed.replace(Regex("`(.+?)`"), "<code>$1</code>")

        // Links: [text](url)
        processed = processed.replace(Regex("\\[(.+?)\\]\\((.+?)\\)"), "<a href=\"$2\">$1</a>")

        // Images: ![alt](url)
        processed = processed.replace(Regex("!\\[(.+?)\\]\\((.+?)\\)"), "<img src=\"$2\" alt=\"$1\"/>")

        return processed
    }

    /**
     * إغلاق قائمة مفتوحة إذا كانت موجودة
     */
    private fun closeListIfNeeded(inList: Boolean, closeList: () -> Unit) {
        if (inList) {
            closeList()
        }
    }

    /**
     * ترميز النص لمنع حقن HTML
     */
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;")
    }

    /**
     * بناء مستند HTML كامل للمعاينة
     */
    fun buildHtmlDocument(
        title: String,
        content: String,
        accentColor: NotoColor,
    ): String {
        val accentHex = when (accentColor) {
            NotoColor.Red -> "#E53935"
            NotoColor.Pink -> "#D81B60"
            NotoColor.Purple -> "#8E24AA"
            NotoColor.DeepPurple -> "#5E35B1"
            NotoColor.Indigo -> "#3949AB"
            NotoColor.Blue -> "#1E88E5"
            NotoColor.LightBlue -> "#039BE5"
            NotoColor.Cyan -> "#00ACC1"
            NotoColor.Teal -> "#00897B"
            NotoColor.Green -> "#43A047"
            NotoColor.LightGreen -> "#7CB342"
            NotoColor.Lime -> "#C0CA33"
            NotoColor.Yellow -> "#FDD835"
            NotoColor.Amber -> "#FFB300"
            NotoColor.Orange -> "#FB8C00"
            NotoColor.DeepOrange -> "#F4511E"
            NotoColor.Brown -> "#6D4C41"
            NotoColor.Grey -> "#757575"
            NotoColor.BlueGrey -> "#546E7A"
            NotoColor.Black -> "#000000"
            NotoColor.White -> "#FFFFFF"
        }

        val titleText = title.ifBlank { "Untitled" }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$titleText</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        background-color: #121212;
                        color: #E0E0E0;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        font-size: 16px;
                        line-height: 1.8;
                        padding: 24px 20px 40px;
                        -webkit-font-smoothing: antialiased;
                    }
                    h1 {
                        color: #FFFFFF;
                        font-size: 28px;
                        font-weight: 700;
                        margin-bottom: 16px;
                        line-height: 1.3;
                    }
                    h2 {
                        color: $accentHex;
                        font-size: 22px;
                        font-weight: 600;
                        margin-top: 24px;
                        margin-bottom: 12px;
                        padding-bottom: 6px;
                        border-bottom: 2px solid ${accentHex}33;
                        line-height: 1.4;
                    }
                    h3 {
                        color: #FFFFFF;
                        font-size: 18px;
                        font-weight: 600;
                        margin-top: 18px;
                        margin-bottom: 10px;
                        line-height: 1.4;
                    }
                    p {
                        margin-bottom: 12px;
                        line-height: 1.8;
                    }
                    strong {
                        color: #FFFFFF;
                        font-weight: 700;
                    }
                    em {
                        color: #BDBDBD;
                        font-style: italic;
                    }
                    code {
                        background-color: #1E1E1E;
                        color: $accentHex;
                        padding: 2px 8px;
                        border-radius: 4px;
                        font-family: "SF Mono", "Consolas", "Liberation Mono", monospace;
                        font-size: 14px;
                    }
                    pre {
                        background-color: #1E1E1E;
                        padding: 16px;
                        border-radius: 8px;
                        overflow-x: auto;
                        margin: 12px 0;
                        border-left: 3px solid $accentHex;
                    }
                    pre code {
                        background-color: transparent;
                        color: #E0E0E0;
                        padding: 0;
                        border-radius: 0;
                        font-size: 14px;
                    }
                    blockquote {
                        border-left: 3px solid $accentHex;
                        margin: 12px 0;
                        padding: 8px 16px;
                        color: #9E9E9E;
                        background-color: #1A1A1A;
                        border-radius: 0 8px 8px 0;
                    }
                    ul, ol {
                        padding-left: 24px;
                        margin: 8px 0 12px;
                    }
                    li {
                        margin-bottom: 4px;
                        line-height: 1.6;
                    }
                    li:last-child {
                        margin-bottom: 0;
                    }
                    hr {
                        border: none;
                        border-top: 1px solid #2A2A2A;
                        margin: 16px 0;
                    }
                    a {
                        color: $accentHex;
                        text-decoration: none;
                    }
                    a:hover {
                        text-decoration: underline;
                    }
                    img {
                        max-width: 100%;
                        border-radius: 8px;
                        margin: 8px 0;
                    }
                    del {
                        color: #757575;
                    }
                    br {
                        display: block;
                        content: "";
                        margin: 4px 0;
                    }
                    .empty-note {
                        color: #757575;
                        font-style: italic;
                    }
                </style>
            </head>
            <body>
                <h1>${escapeHtml(titleText)}</h1>
                $content
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * اختصار لتحويل Markdown مباشرة إلى مستند HTML كامل
     */
    fun markdownToHtmlDocument(
        title: String,
        body: String,
        accentColor: NotoColor,
    ): String {
        val htmlContent = markdownToHtml(body)
        return buildHtmlDocument(title, htmlContent, accentColor)
    }
}
