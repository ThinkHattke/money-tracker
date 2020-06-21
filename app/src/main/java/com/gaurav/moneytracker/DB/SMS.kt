package com.gaurav.moneytracker.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SMS(

    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "sender")
    val sender: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "amount")
    val amount: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "tag")
    val tag: String? = ""

)