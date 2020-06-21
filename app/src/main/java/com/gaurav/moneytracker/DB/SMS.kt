package com.gaurav.moneytracker.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SMS(

    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "sender")
    val sender: String,
    @ColumnInfo(name = "tag")
    val tag: String? = ""

)