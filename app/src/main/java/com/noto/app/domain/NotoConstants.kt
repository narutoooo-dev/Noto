package com.noto.app.domain

object NotoConstants {
    const val Email = "noto@alialbaali.com"
    const val DeveloperUrl = "https://www.alialbaali.com"
    const val LicenseUrl = "https://www.apache.org/licenses/LICENSE-2.0"
    const val PlayStoreUrl = "https://play.google.com/store/apps/details?id=com.noto"
    const val GithubUrl = "https://github.com/alialbaali/Noto"
    const val TelegramUrl = "https://t.me/notoapp"
    const val PrivacyPolicyUrl = "https://github.com/alialbaali/Noto/blob/master/PrivacyPolicy.md"
    const val GithubIssueUrl = "https://github.com/alialbaali/Noto/issues/new"
    const val ReportIssueEmailSubject = "Issue Regarding Noto"
    const val GitHubReleasesUrl = "https://github.com/alialbaali/Noto/releases"
    fun GitHubReleaseUrl(version: String) = "https://github.com/alialbaali/Noto/releases/tag/v$version"

    fun ReportIssueEmailBody(androidVersion: String, sdkVersion: String, appVersion: String) = """
        Hi there,
        
        I'm having an issue with [ISSUE].
        
        Android version: $androidVersion
        SDK version: $sdkVersion
        App version: $appVersion
        
        Regards,
    """.trimIndent()

    const val TranslationEmailSubject = "Translate Noto"
    val TranslationEmailBody = """
        Hi there,
        
        I would like to translate Noto to [LANGUAGE].
        
        I want to be credited as (optional):
        Name: [NAME]
        Link (optional): [LINK]
        
        Regards,
    """.trimIndent()
}