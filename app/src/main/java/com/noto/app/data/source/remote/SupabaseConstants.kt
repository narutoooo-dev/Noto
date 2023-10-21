package com.noto.app.data.source.remote

data object SupabaseConstants {

    const val Name = "name"
    const val Email = "email"
    const val PasswordParameters = "password_parameters"
    const val Type = "type"
    const val SignUp = "signup"
    const val EmailChange = "email_change"

    data object RPCs {
        const val IsEmailExist = "is_email_exist"
        const val DeleteUser = "delete_user"
        const val GetPasswordParameters = "get_password_parameters"
    }

    data object Tables {
        const val Users = "users"
        const val Folders = "folders"
    }

    data object URLs {
        const val SupabaseUrl = "https://bcehffsgkofhyjoktqpe.supabase.co"
        const val NotoHost = "noto.dev"
        const val NotoVerifyEmail = "https://noto.dev/verify"
    }

}