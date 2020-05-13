package com.fpradipt.fokkuy.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_timer_table")
data class UsageModel(
    @PrimaryKey(autoGenerate = true)
    var timerId: Long = 0L,

    @ColumnInfo(name = "start_timer")
    var startTimer: Long = 0L,

    @ColumnInfo(name = "end_timer")
    var endTimer: Long = 0L,

    @ColumnInfo(name = "duration_timer")
    var duration: Int = 0,

    @ColumnInfo(name = "created_at")
    var createdAt: String = "",

    @ColumnInfo(name = "user")
    val user: String = ""
)