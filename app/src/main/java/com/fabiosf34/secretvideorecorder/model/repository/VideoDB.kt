package com.fabiosf34.secretvideorecorder.model.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fabiosf34.secretvideorecorder.model.Video
import com.fabiosf34.secretvideorecorder.model.utilities.DBConstants

@Database(entities = [Video::class], version = 1)
abstract class VideoDB : RoomDatabase() {
    companion object {
        private lateinit var INSTANCE: VideoDB

        fun getDB(context: Context): VideoDB {
            INSTANCE = Room.databaseBuilder(context, VideoDB::class.java, "videodb")
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .build()

            return INSTANCE
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM ${DBConstants.Video.TABLE_NAME}")
            }

        }
    }

    abstract fun videoDao(): VideoDAO
}
