package com.fpradipt.fokkuy.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_timer_table")
data class UsageModel(
    @PrimaryKey(autoGenerate = true)
    var timerId:Long = 0L,

    @ColumnInfo(name = "duration_timer")
    val duration: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: String = "",

    @ColumnInfo(name = "user")
    val user: String = ""
)