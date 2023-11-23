package com.noto.app.domain.model

sealed class NotoException(message: String?) : RuntimeException(message) {

    operator fun invoke(): Nothing = throw this

    sealed class Auth : NotoException(message = null) {
        data object UserAlreadyExists : Auth()
        data object EmailNotVerified : Auth()
        data object InvalidCredentials : Auth()
        data object InvalidEmail : Auth()
        data object InvalidPassword : Auth()
        data object InvalidOtp: Auth()
    }

    sealed class Entity : NotoException(message = null) {
        data object InvalidLocalItem : Entity()
        data object MissingRemoteId: Entity()
    }

    data object TryAgainLater : NotoException(message = null)

    sealed class ExportImport: NotoException(message = null) {
        data object ExportFailed: ExportImport()
        data object ImportFailed: ExportImport()
        data object FileCreationFailed: ExportImport()
        data object NoFolderSelected: ExportImport()
        data object NoFileSelected: ExportImport()
    }

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