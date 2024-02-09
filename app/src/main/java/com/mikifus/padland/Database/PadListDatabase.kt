package com.mikifus.padland.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mikifus.padland.Database.Migrations.MIGRATION_8_9
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupDao
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadDao
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.Database.ServerModel.ServerDao
import com.mikifus.padland.Database.TypeConverters.DateConverter

@Database(entities = [Pad::class, PadGroup::class, Server::class, PadGroupsAndPadList::class], version = 9)
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
            return Room.databaseBuilder(context, PadListDatabase::class.java, "padlist")
                .addMigrations(MIGRATION_8_9)
                .build()
        }
    }

}

