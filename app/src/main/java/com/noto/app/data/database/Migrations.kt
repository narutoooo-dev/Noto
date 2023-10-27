package com.noto.app.data.database

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.useCursor
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

object Migrations {
    @DeleteColumn.Entries(
        DeleteColumn(tableName = "libraries", columnName = "sorting_type"),
        DeleteColumn(tableName = "libraries", columnName = "sorting_method"),
    )
    class DeleteSortingTypeAndSortingMethodColumns : AutoMigrationSpec

    @RenameColumn(tableName = "notes", fromColumnName = "is_starred", toColumnName = "is_pinned")
    class RenameIsStarredColumn : AutoMigrationSpec

    @DeleteTable.Entries(
        DeleteTable(tableName = "labels"),
        DeleteTable(tableName = "noto_labels"),
    )
    class DeleteLabelAndNoteLabelTables : AutoMigrationSpec

    @RenameTable(fromTableName = "noto_labels", toTableName = "note_labels")
    class RenameNoteLabelsTable : AutoMigrationSpec

    @RenameColumn(tableName = "libraries", fromColumnName = "layout_manager", toColumnName = "layout")
    class RenameLayoutManagerColumn : AutoMigrationSpec

    @RenameColumn(tableName = "libraries", fromColumnName = "sorting", toColumnName = "sorting_type")
    class RenameNoteListSortingTypeColumn : AutoMigrationSpec

    @RenameColumn(tableName = "libraries", fromColumnName = "is_set_new_note_cursor_on_title", toColumnName = "new_note_cursor_position")
    class RenameIsSetNewNoteCursorOnTitle : AutoMigrationSpec

    @RenameTable(fromTableName = "libraries", toTableName = "folders")
    @RenameColumn.Entries(
        RenameColumn(tableName = "notes", fromColumnName = "library_id", toColumnName = "folder_id"),
        RenameColumn(tableName = "labels", fromColumnName = "library_id", toColumnName = "folder_id"),
    )
    class RenameLibraryToFolder : AutoMigrationSpec

    object SetAccessDateToCreationDate : Migration(30, 31) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            try {
                database.execSQL(
                    """
                CREATE TABLE notes_tmp(
                   id INTEGER NOT NULL PRIMARY KEY, 
                   folder_id INTEGER NOT NULL REFERENCES folders(id) ON DELETE CASCADE,
                   title TEXT NOT NULL,
                   body TEXT NOT NULL,
                   position INTEGER NOT NULL,
                   creation_date TEXT NOT NULL,
                   is_pinned INTEGER NOT NULL DEFAULT 0,
                   is_archived INTEGER NOT NULL DEFAULT 0,
                   reminder_date TEXT DEFAULT NULL,
                   is_vaulted INTEGER NOT NULL DEFAULT 0,
                   access_date TEXT     NOT NULL DEFAULT 'creation_date',
                   scrolling_position INTEGER NOT NULL DEFAULT 0
                );
                """.trimIndent()
                )
                database.execSQL(
                    """INSERT INTO notes_tmp 
                    |SELECT 
                    | id, folder_id, title, body, position, creation_date,
                    | is_pinned, is_archived, reminder_date,
                    | is_vaulted,
                    | CASE WHEN access_date IS NULL THEN creation_date ELSE access_date END,
                    | scrolling_position FROM notes;""".trimMargin()
                )
                database.execSQL("""DROP TABLE notes;""")
                database.execSQL("""ALTER TABLE notes_tmp RENAME TO notes;""")
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
        }
    }

    object AddRemoteIdToLocalFolder : Migration(32, 33) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            try {
                database.execSQL("ALTER TABLE folders ADD COLUMN remote_id TEXT DEFAULT NULL;")
                database.query(
                    """
                    SELECT * FROM folders;
                """.trimIndent()
                ).useCursor { cursor ->
                    repeat(cursor.count) { rowIndex ->
                        cursor.moveToPosition(rowIndex)
                        val folderId = cursor.getLong(0)
                        val remoteId = UUID.randomUUID().toString()
                        database.execSQL(
                            """
                    UPDATE folders SET remote_id = "$remoteId" WHERE id = $folderId;
                """.trimIndent()
                        )
                    }
                }
                database.execSQL(
                    """
                    CREATE TABLE folders_new(
                    id INTEGER NOT NULL PRIMARY KEY,
                    remote_id TEXT NOT NULL UNIQUE,
                    parent_id INTEGER DEFAULT NULL,
                    title TEXT NOT NULL,
                    position INTEGER NOT NULL,
                    color INTEGER NOT NULL,
                    creation_date TEXT NOT NULL,
                    layout INTEGER NOT NULL DEFAULT 0,
                    note_preview_size INTEGER NOT NULL DEFAULT 15,
                    is_archived INTEGER NOT NULL DEFAULT 0,
                    is_pinned INTEGER NOT NULL DEFAULT 0,
                    is_show_note_creation_date INTEGER NOT NULL DEFAULT 0,
                    new_note_cursor_position INTEGER NOT NULL DEFAULT 0,
                    sorting_type INTEGER NOT NULL DEFAULT 1,
                    sorting_order INTEGER NOT NULL DEFAULT 1,
                    grouping INTEGER NOT NULL DEFAULT 0,
                    grouping_order INTEGER NOT NULL DEFAULT 1,
                    is_vaulted INTEGER NOT NULL DEFAULT 0,
                    scrolling_position INTEGER NOT NULL DEFAULT 0,
                    filtering_type INTEGER NOT NULL DEFAULT 0,
                    open_notes_in INTEGER NOT NULL DEFAULT 0
                    );
                """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO folders_new
                    SELECT id, remote_id, parent_id, title, position, color, creation_date, layout, note_preview_size, is_archived,
                    is_pinned, is_show_note_creation_date, new_note_cursor_position, sorting_type, sorting_order, grouping, grouping_order,
                    is_vaulted, scrolling_position, filtering_type, open_notes_in FROM folders;
                """.trimIndent()
                )
                database.execSQL("""DROP TABLE folders;""")
                database.execSQL("""ALTER TABLE folders_new RENAME TO folders;""")
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
        }
    }

    object SetGeneralFolderTitleToBlank : Migration(33, 34) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            try {
                database.execSQL(
                    """
                    UPDATE folders SET title = "" WHERE id = -1;
                    """.trimIndent()
                )
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
        }
    }
}