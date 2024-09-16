package com.mikifus.padland.Database.Migrations

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.regex.Pattern

abstract class CompatibilityMigration: Migration(-1, 8) {
    companion object {
        fun realMigrate(context: Context, db: SQLiteDatabase) {
            db.beginTransaction()
            Log.w("MIGRATION_BEFORE_ROOM", "migrateBeforeRoom called beginTransaction")
            try {
                db.setForeignKeyConstraintsEnabled(false)
                db.execSQL("CREATE TABLE IF NOT EXISTS `padlist_migration` (" +
                        "    `_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "    `name` TEXT NOT NULL," +
                        "    `local_name` TEXT NOT NULL," +
                        "    `server` TEXT NOT NULL," +
                        "    `url` TEXT NOT NULL," +
                        "    `last_used_date` INTEGER NOT NULL DEFAULT ((strftime('%s','now')))," +
                        "    `create_date` INTEGER NOT NULL DEFAULT ((strftime('%s','now')))," +
                        "    `access_count` INTEGER NOT NULL DEFAULT 0" +
                        ")")
                db.execSQL("INSERT INTO padlist_migration SELECT * FROM padlist")
                db.execSQL("DROP TABLE padlist")
                db.execSQL("ALTER TABLE padlist_migration RENAME TO padlist")


                db.execSQL("CREATE TABLE IF NOT EXISTS `padgroups_migration` (" +
                        "    `_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "    `name` TEXT NOT NULL," +
                        "    `last_used_date` INTEGER NOT NULL DEFAULT ((strftime('%s','now')))," +
                        "    `create_date` INTEGER NOT NULL DEFAULT ((strftime('%s','now')))," +
                        "    `access_count` INTEGER NOT NULL DEFAULT 0," +
                        "    `position` INTEGER NOT NULL DEFAULT 0" +
                        ")")
                db.execSQL("INSERT INTO padgroups_migration SELECT * FROM padgroups")
                db.execSQL("DROP TABLE padgroups")
                db.execSQL("ALTER TABLE padgroups_migration RENAME TO padgroups")


                db.execSQL("CREATE TABLE IF NOT EXISTS `padland_servers_migration` (" +
                        "    `_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "    `name` TEXT NOT NULL," +
                        "    `url` TEXT NOT NULL," +
                        "    `padprefix` TEXT NOT NULL," +
                        "    `position` INTEGER NOT NULL DEFAULT 0," +
                        "    `jquery` INTEGER NOT NULL DEFAULT 0," +
                        "    `enabled` INTEGER NOT NULL DEFAULT 1" +
                        ")")
                db.execSQL("INSERT INTO padland_servers_migration SELECT * FROM padland_servers")
                db.execSQL("UPDATE padland_servers_migration" +
                        "    SET padprefix=REPLACE(padprefix, url, '')")
                db.execSQL("DROP TABLE padland_servers")
                db.execSQL("ALTER TABLE padland_servers_migration RENAME TO padland_servers")


                db.execSQL("CREATE TABLE IF NOT EXISTS `padlist_padgroups_migration` (" +
                        "    `_id_group` INTEGER NOT NULL," +
                        "    `_id_pad` INTEGER NOT NULL," +
                        "    PRIMARY KEY(`_id_group`, `_id_pad`)," +
                        "    FOREIGN KEY(`_id_group`) REFERENCES `padgroups`(`_id`)" +
                        "       ON UPDATE NO ACTION ON DELETE NO ACTION ," +
                        "    FOREIGN KEY(`_id_pad`) REFERENCES `padlist`(`_id`)" +
                        "       ON UPDATE NO ACTION ON DELETE NO ACTION )")

                db.execSQL("INSERT INTO padlist_padgroups_migration SELECT * FROM padlist_padgroups")
                db.execSQL("DROP TABLE padlist_padgroups")
                db.execSQL("ALTER TABLE padlist_padgroups_migration RENAME TO padlist_padgroups")
                db.setTransactionSuccessful()
            } catch (exception: Exception) {
                Log.e("MIGRATION_BEFORE_ROOM", "migrateBeforeRoom failed")
            } finally {
                db.endTransaction()
                Log.w("MIGRATION_BEFORE_ROOM", "migrateBeforeRoom has modified the database")
            }

            // Let's use this opportunity to update also the changed color preference
            val userDetails =
                context.getSharedPreferences(
                    context.packageName + "_preferences",
                    AppCompatActivity.MODE_PRIVATE
                )
            var oldPreference = userDetails.getString("padland_default_color", "")
            if(!oldPreference.isNullOrBlank()) {
                if (oldPreference.length == 4) { // #XXX
                    oldPreference = oldPreference.replace(
                        Pattern.compile(
                            "#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])"
                        ).toRegex(),
                        "#$1$1$2$2$3$3")
                }
                val colorInt: Int = Color.parseColor(oldPreference)
                userDetails.edit().remove("padland_default_color").apply()
                userDetails.edit().putInt("padland_default_color", colorInt).apply()
            } else {
                userDetails.edit().remove("padland_default_color").apply()
            }
        }
    }
}

val MIGRATION_BEFORE_ROOM = object : CompatibilityMigration() {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Empty implementation, because the schema isn't changing.
    }
}

//val MIGRATION_8_9 = object : Migration(8, 9) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE padland_servers ADD COLUMN 'cryptpad' INTEGER NOT NULL DEFAULT 0")
//    }
//}