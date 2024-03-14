package com.noto.app.data.source.remote

data object SupabaseConstants {

    const val Id = "id"
    const val Name = "name"
    const val Email = "email"
    const val PasswordParameters = "password_parameters"
    const val Type = "type"
    const val SignUp = "signup"
    const val EmailChange = "email_change"
    const val MetaDataUpdatedAtColumn = "meta_data->>updated_at"
    const val MetaDataCreatedAtColumn = "meta_data->>created_at"

    data object RPCs {
        const val IsEmailExist = "is_email_exist"
        const val DeleteUser = "delete_user"
        const val GetPasswordParameters = "get_password_parameters"
    }

    data object Tables {
        const val Users = "users"
        const val Folders = "folders"
        const val Notes = "notes"
        const val Labels = "labels"
        const val NoteLabels = "note_labels"
    }

    data object URLs {
        const val SupabaseUrl = "https://bcehffsgkofhyjoktqpe.supabase.co"
        const val NotoHost = "noto.dev"
        const val NotoVerifyEmail = "https://noto.dev/verify"
    }

    data object Schemas {
        const val Public = "public"
    }

    data object RealtimeChannelIds {

        data object Folder {
            const val Insert = "insert_folder"
            const val Update = "update_folder"
            const val Delete = "delete_folder"
        }

        data object Note {
            const val Insert = "insert_note"
            const val Update = "update_note"
            const val Delete = "delete_note"
        }

        data object Label {
            const val Insert = "insert_label"
            const val Update = "update_label"
            const val Delete = "delete_label"
        }

        data object NoteLabel {
            const val Insert = "insert_note_label"
            const val Delete = "delete_note_label"
        }

    }

}