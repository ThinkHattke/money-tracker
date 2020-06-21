package com.gaurav.moneytracker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(Check_SMS_READ(this@MainActivity)) {
            getSMS()
        } else {
            try {
                val permissions = arrayOf(Manifest.permission.READ_SMS)
                ActivityCompat.requestPermissions(this@MainActivity, permissions,0)
            } catch (e: Exception) {
            }
        }

    }

    fun fetchSMS() {

        val smsInbox: ArrayList<Message> = ArrayList()

        val uriSms: Uri = Uri.parse("content://sms")

        val cursor: Cursor? = this.contentResolver
            .query(
                uriSms, arrayOf(
                    "_id", "address", "date", "body",
                    "type", "read"
                ), null, null,
                "date" + " COLLATE LOCALIZED ASC"
            )
        if (cursor != null) {
            cursor.moveToLast()
            if (cursor.count > 0) {
                do {
                    val message = Message(
                        cursor.getString(cursor.getColumnIndex("body")),
                        cursor.getString(cursor.getColumnIndex("address"))
                    )
                    smsInbox.add(message)
                } while (cursor.moveToPrevious())
            }
        }
        cursor?.close()

        for (i in smsInbox) {
            var text = sms.text
            text = "$text\n${i.number} -> ${i.body}"
            sms.text = text
        }

    }

    private fun getSMS() {
        val SMS: MutableList<String> = ArrayList()
        val uriSMSURI = Uri.parse("content://sms/inbox")
        val cur =
            contentResolver.query(uriSMSURI, null, null, null, null)
        while (cur!!.moveToNext()) {
            val address = cur.getString(cur.getColumnIndex("address"))
            val body = cur.getString(cur.getColumnIndexOrThrow("body"))
            SMS.add("Number: $address .Message: $body")
        }
        for (i in SMS) {
            var text = sms.text
            text = "$text\n$i"
            sms.text = text
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            0 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSMS()
                }
                return
            }
        }
    }

    fun Check_SMS_READ(act: Activity?): Boolean {
        val result =
            ContextCompat.checkSelfPermission(act!!, Manifest.permission.READ_SMS)
        return result == PackageManager.PERMISSION_GRANTED
    }

}