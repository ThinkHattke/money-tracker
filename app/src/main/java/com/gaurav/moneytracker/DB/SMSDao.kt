package com.gaurav.moneytracker.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SMSDao {

    @Query("SELECT * FROM SMS")
    fun getAllSMS(): List<SMS>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sms: SMS)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(smsList: List<SMS>)

    @Query("UPDATE SMS SET tag = :tag WHERE id = :id")
    fun updateTag(id: Int, tag: String)

    @Query("SELECT * FROM SMS WHERE tag = :tag")
    fun getSMSByTag(tag: String): List<SMS>

    @Query("SELECT * FROM SMS WHERE type = :type")
    fun getSMSByType(type: String): List<SMS>

}