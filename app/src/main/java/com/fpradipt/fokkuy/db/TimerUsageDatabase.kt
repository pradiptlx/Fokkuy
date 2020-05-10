package com.fpradipt.fokkuy.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fpradipt.fokkuy.model.UsageModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(entities = [UsageModel::class], version = 1, exportSchema = false)
abstract class TimerUsageDatabase : RoomDatabase() {
    abstract val timerUsageDatabaseDao: TimerUsageDao

    companion object {
        @Volatile
        private var INSTANCE: TimerUsageDatabase? = null

        @InternalCoroutinesApi
        fun getInstance(context: Context): TimerUsageDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TimerUsageDatabase::class.java,
                        "usage_timer_table"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
