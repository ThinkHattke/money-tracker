package com.gaurav.moneytracker.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SMS::class], version = 1)
abstract class SMSDB : RoomDatabase() {
    abstract fun SMSDao(): SMSDao
    companion object {
        private var INSTANCE: SMSDB? = null
        fun getDatabase(context: Context): SMSDB {
            if (INSTANCE == null) {
                synchronized(SMSDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        SMSDB::class.java, "sms_db"
                    )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}