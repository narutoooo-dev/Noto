package com.noto.app.domain

object RegexConstants {
    val Email = Regex(".+@.+\\..+")

    val MinChars = Regex(".{8,}")
    val UppercaseChars = Regex(".*?[A-Z].*?")
    val LowercaseChars = Regex(".*?[a-z].*?")
    val NumberChars = Regex(".*?[0-9].*?")
    val SpecialChars = Regex(".*?[!@#\$%^&*()?-].*?")

    fun matchesEmail(email: String): Boolean {
        return email.none { it.isWhitespace() } && email.matches(Email)
    }

    fun matchesPassword(password: String): Boolean {
        return password.matches(MinChars)
                && password.matches(UppercaseChars)
                && password.matches(LowercaseChars)
                && password.matches(NumberChars)
                && password.matches(SpecialChars)
    }

    fun matchesBackupPasscode(passcode: String): Boolean {
        return passcode.matches(MinChars) && passcode.matches(NumberChars)
    }
}