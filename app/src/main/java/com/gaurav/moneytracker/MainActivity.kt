package com.gaurav.moneytracker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.gaurav.moneytracker.DB.SMS
import com.gaurav.moneytracker.DB.SMSDB
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.roundToInt

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        setData()

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

                val body = cur.getString(cur.getColumnIndexOrThrow("body"))

                if(pattern.containsMatchIn(body)) {

                    val address = cur.getString(cur.getColumnIndex("address"))
                    val date = cur.getString(cur.getColumnIndex("date"))

                    var amount = extractTransAmount(body)?:""
//                    if(amountPattern.containsMatchIn(body)) {
//                        amount = amountPattern.find(body)?.value?:""
//                    }

                    var type = ""
                    val isDeposit = validateTransType(key_debit_search, body)
                    val isCredit = validateTransType(key_credit_search, body)
                    if(isDeposit) {
                        type = "Debit"
                    } else if(isCredit) {
                        type = "Credit"
                    }

                    if(amount.isNotEmpty() && type.isNotEmpty()) {
                        smsList.add(SMS(date, body, address, date, amount, type))
                    }

                }

            }
            cur.close()
            SMSDB.getDatabase(this).SMSDao().insertAll(smsList)
            setData()
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

    fun getSMSTypes() {
        val smsDao = SMSDB.getDatabase(this).SMSDao()
        val creditSMS = smsDao.getSMSByType("Credit")
        val debitSMS = smsDao.getSMSByType("Debit")

        var creditAmount = 0.0
        var debitAmount = 0.0

        for (sms in creditSMS) {
            val amount = sms.amount.toDouble()
            creditAmount = creditAmount.plus(amount)
        }

        for (sms in debitSMS) {
            val amount = sms.amount.toDouble()
            debitAmount = debitAmount.plus(amount)
        }

       runOnUiThread {

           val Pie = AnyChart.pie()
           val data = mutableListOf<DataEntry>()
           data.add(ValueDataEntry("Expense", debitAmount))
           data.add(ValueDataEntry("Income", creditAmount))

           Pie.data(data)
           Pie.title("Total transaction made in last 30 days")
           Pie.legend().title().enabled(true)

           Pie.legend()
               .position("center-bottom")
               .itemsLayout(LegendLayout.HORIZONTAL)
               .align(Align.CENTER)

           pie_chart.setChart(Pie)

       }

    }

    private fun setData() {

       runOnUiThread {

           val smsDao = SMSDB.getDatabase(this).SMSDao()

           val monthParse = SimpleDateFormat("MM")
           val monthDisplay = SimpleDateFormat("MMMM")
           val monthDate = monthParse.parse((Calendar.getInstance().get(Calendar.MONTH)+1).toString())
           val monthValue = monthDisplay.format(monthDate)
           month.text = monthValue

           val debitSMS = smsDao.getSMSByType("Debit")
           var debitAmount = 0.0
           for (sms in debitSMS) {
               val amount = sms.amount.toDouble()
               debitAmount = debitAmount.plus(amount)
           }

           val debitString = NumberFormat.getNumberInstance(Locale.getDefault()).format(debitAmount.roundToInt())
           amount.text = debitString

           val list = smsDao.get5SMS()
           val adapter = TransactionRCVAdapter(list)
           transaction_rcv.adapter = adapter

           more_transaction.setOnClickListener {
               startActivity(Intent(this@MainActivity, AllSMS::class.java))
               finish()
           }

       }

    }

    private fun extractTransAmount(
        smsMsg: String
    ): String? {
        var smsMsg = smsMsg
        var reqMatch = ""
        smsMsg = smsMsg.replace(",", "")
        val searchFor = "((\\s)?##SEARCH4CURRENCY##(.)?(\\s)?((\\d+)(\\.\\d+)?))"
        val getGroup = intArrayOf(5)
        var indx = 0

        val searchCurrency: Array<String> = arrayOf("INR", "Rs")
        try {
            for (element in searchCurrency) {
                val p = Pattern.compile(
                    searchFor.replace(
                        "##SEARCH4CURRENCY##",
                        element
                    )
                )
                val m = p.matcher(smsMsg)
                if (reqMatch.isEmpty()) {
                    while (m.find()) {
                        if (indx == 0) {
                            reqMatch = m.group(getGroup[0]).trim { it <= ' ' }
                            break
                        }
                        indx += 1
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            Timber.e(e, "extractTransAmount")
        }
        return reqMatch
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