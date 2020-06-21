package com.gaurav.moneytracker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gaurav.moneytracker.DB.SMS
import com.gaurav.moneytracker.DB.SMSDB
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    val pattern = Regex("(?=.*[Aa]ccount.*|.*[Aa]/[Cc].*|.*[Aa][Cc][Cc][Tt].*|.*[Cc][Aa][Rr][Dd].*)(?=.*[Cc]redit.*|.*[Dd]ebit.*)(?=.*[Ii][Nn][Rr].*|.*[Rr][Ss].*)")
    val amountPattern = Regex("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)")

    var key_credit_search = arrayOf(
        "(credited)",
        "(received)",
        "(added)",
        "(reloaded)",
        "(deposited)",
        "(refunded)",
        "(debited)(.*?)(towards)(\\s)",
        "(\\s)(received)(.*?)(in(\\s)your)(\\s)",
        "(sent)(.*?)(to)(\\s)",
        "(debited)(.*?)(to)(\\s)",
        "(credited)(.*?)(in)(\\s)",
        "(credited)(.*?)(to)(\\s)"
    )

    var key_debit_search = arrayOf(
        "(made)", "(debited)", "(using)", "(paid)", "(purchase)", "(withdrawn)", "(done)",
        "(credited)(.*?)(from)(\\s)", "(sent)(.*?)(from)(\\s)", "(\\s)(received)(.*?)(from)(\\s)",
        "(Sales\\sDraft)"
    )

    val cardPattern = Regex("(?i)(?:\\smade on|ur|made a\\s|in\\*)([A-Za-z]*\\s?-?\\s[A-Za-z]*\\s?-?\\s[A-Za-z]*\\s?-?)")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(Check_SMS_READ(this@MainActivity)) {
            getSMSAndSaveInDB()
        } else {
            try {
                val permissions = arrayOf(Manifest.permission.READ_SMS)
                ActivityCompat.requestPermissions(this@MainActivity, permissions,0)
            } catch (e: Exception) {
            }
        }

    }

    private fun getSMSAndSaveInDB() {
        thread {
            val smsList: MutableList<SMS> = ArrayList()
            val uriSMSURI = Uri.parse("content://sms/inbox")
            val filter = "date>=" + Calendar.getInstance().time.time.minus(2592000000)
            val cur =
                contentResolver.query(uriSMSURI, null, filter, null, null)
            while (cur!!.moveToNext()) {

                val address = cur.getString(cur.getColumnIndex("address"))
                val body = cur.getString(cur.getColumnIndexOrThrow("body"))
                smsList.add(SMS(0, body, address))
            }
            SMSDB.getDatabase(this).SMSDao().insertAll(smsList)
            filterSMS()
        }
    }

    private fun filterSMS() {
        thread {
            val listSMS = SMSDB.getDatabase(this).SMSDao().getAllSMS()
            val transactionalSMS = mutableListOf<SMS>()
            for(sms in listSMS) {
                if(pattern.containsMatchIn(sms.body)) {
                    transactionalSMS.add(sms)
                }
            }
            runOnUiThread {
                sms.text = ""
                for (i in listSMS) {
                    var text = sms.text
                    var amount = ""
                    val isDeposit = validateTransType(key_debit_search, i.body)
                    val isCredit = validateTransType(key_credit_search, i.body)
                    var type = ""
                    if(isDeposit) {
                        type = "Debit"
                    } else if(isCredit) {
                        type = "Credit"
                    }
                    if(amountPattern.containsMatchIn(i.body)) {
                        amount = amountPattern.find(i.body)?.value?:""
                    }
                    if(type.isNotEmpty() && amount.isNotEmpty()) {
                        text = "$text"+i.sender+"\n"+amount+"\n"+type+"\n\n\n"
                    }
                    sms.text = text
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            0 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSMSAndSaveInDB()
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

    private fun validateTransType(
        keySearch: Array<String>,
        smsMsg: String
    ): Boolean {
        var reqMatch = false
        try {
            for (i in 0..keySearch.size - 1) {
                val p: Pattern = Pattern.compile(keySearch[i])
                val m: Matcher = p.matcher(smsMsg)
                if (m != null && reqMatch == false) {
                    while (m.find()) {
                        reqMatch = true
                        break
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            Timber.e(e, "validateTransType")
        }
        return reqMatch
    }

}