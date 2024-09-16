package com.mikifus.padland.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mikifus.padland.Database.Migrations.CompatibilityMigration
import com.mikifus.padland.Database.Migrations.MIGRATION_BEFORE_ROOM
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupDao
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadDao
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.Database.ServerModel.ServerDao
import com.mikifus.padland.Database.TypeConverters.DateConverter


@Database(
    entities = [
        Pad::class,
        PadGroup::class,
        Server::class,
        PadGroupsAndPadList::class
   ],
    version = 9,
    autoMigrations = [
        AutoMigration(from=8, to=9)
    ]
)
@TypeConverters(DateConverter::class)
abstract class PadListDatabase : RoomDatabase() {

    abstract fun padDao(): PadDao

    abstract fun padGroupDao(): PadGroupDao

    abstract fun serverDao(): ServerDao

    companion object {
        @Volatile
        private var INSTANCE: PadListDatabase? = null

        fun getInstance(context: Context): PadListDatabase {
            return INSTANCE?: synchronized(this){
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): PadListDatabase {
            return getDatabaseBuilder(context).build()
        }

        private fun getDatabaseBuilder(context: Context): Builder<PadListDatabase> {
            return Room.databaseBuilder(context, PadListDatabase::class.java, "padlist")
                .addMigrations(MIGRATION_BEFORE_ROOM/*, MIGRATION_8_9*/)
        }

        /**
         * WARNING: Use only for tests
         */
        fun getMainThreadInstance(context: Context): PadListDatabase {
            return INSTANCE?: synchronized(this){
                val instance = getDatabaseBuilder(context)
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * WARNING: Compatibility transformation
         */
        fun migrateBeforeRoom(context: Context) {
            Log.w("MIGRATION_BEFORE_ROOM", "migrateBeforeRoom called")
            try {
                Log.w("MIGRATION_BEFORE_ROOM", "migrateBeforeRoom getting instance")
                getMainThreadInstance(context).inTransaction()
                Log.w("MIGRATION_BEFORE_ROOM", "migrateBeforeRoom got instance")
            } catch (exception: IllegalStateException) {
                Log.w("MIGRATION_BEFORE_ROOM", "migrateBeforeRoom got exception")
                Log.w("MIGRATION_BEFORE_ROOM", exception.stackTraceToString())
                val db = PadlandDbHelper(context)
                CompatibilityMigration.realMigrate(context, db.writableDatabase)
                db.close()
            }
        }
    }



    open class PadlandDbHelper(protected open var context: Context?) :
        SQLiteOpenHelper(context,
            "padlist",
            null,
            8) {
        override fun onCreate(p0: SQLiteDatabase?) {}
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }

}

