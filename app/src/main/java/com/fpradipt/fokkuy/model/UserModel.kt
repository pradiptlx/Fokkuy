package com.fpradipt.fokkuy.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserModel(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,

    @ColumnInfo(name = "fullname")
    val fullname: String = "",

    @ColumnInfo(name = "email")
    val email: String = ""
)