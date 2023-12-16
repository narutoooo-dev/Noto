package com.noto.app.domain.model

sealed class NotoException(message: String? = null) : RuntimeException(message) {

    operator fun invoke(): Nothing = throw this

    sealed class Auth : NotoException() {
        data object UserAlreadyExists : Auth()
        data object EmailNotVerified : Auth()
        data object InvalidCredentials : Auth()
        data object InvalidEmail : Auth()
        data object InvalidPassword : Auth()
        data object InvalidOtp : Auth()
    }

    sealed class Entity : NotoException() {
        data object InvalidLocalItem : Entity()
        data object MissingRemoteId : Entity()
    }

    sealed class Model : NotoException() {
        data object TitleIsRequired : Model()
        data object NameIsRequired : Model()
    }

    sealed class LocalBackup : NotoException() {
        sealed class Export : NotoException() {
            data object ExportFailed : Export()
            data object FileCreationFailed : Export()
            data object NoFolderSelected : Export()
        }

        sealed class Import : NotoException() {
            data object ImportFailed : Import()
            data object NoFileSelected : Import()
        }
    }

    sealed class Vault : NotoException() {
        data object PasscodeIsRequired : Vault()
        data object NewPasscodeIsRequired : Vault()
        data object InvalidPasscode : Vault()
        data object PasscodeRequirements : Vault()
        data object MismatchedPasscodes : Vault()
    }

    data object TryAgainLater : NotoException(message = null)

    class Unknown(message: String?) : NotoException(message)

}

inline fun <T> tryCatching(noinline onException: ((Throwable) -> Nothing)? = null, block: () -> T): T {
    return try {
        block()
    } catch (exception: Throwable) {
        if (onException != null) onException(exception) else unknownException(exception.message)
    }
}

fun unknownException(message: String?): Nothing = NotoException.Unknown(message).invoke()