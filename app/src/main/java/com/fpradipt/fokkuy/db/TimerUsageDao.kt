package com.fpradipt.fokkuy.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fpradipt.fokkuy.model.UsageModel

@Dao
interface TimerUsageDao {
    @Insert
    fun insert(usageTimer: UsageModel)

    @Update
    fun update(usageTimer: UsageModel)

    @Query("SELECT * FROM usage_timer_table WHERE timerId=:key")
    fun get(key: Long): UsageModel?

    @Query("SELECT * FROM usage_timer_table ORDER BY created_at DESC")
    fun getHistory(): LiveData<List<UsageModel>>

    @Query("SELECT * FROM usage_timer_table ORDER BY created_at DESC")
    fun getAll(): List<UsageModel>

    @Query("SELECT * FROM usage_timer_table ORDER BY timerId DESC LIMIT 1")
    fun getCurrent(): UsageModel?

    @Query("DELETE FROM usage_timer_table")
    fun clearHistory()
}