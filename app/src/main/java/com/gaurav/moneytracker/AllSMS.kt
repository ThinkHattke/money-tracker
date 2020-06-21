package com.gaurav.moneytracker

import android.content.AbstractThreadedSyncAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import com.gaurav.moneytracker.DB.SMS
import com.gaurav.moneytracker.DB.SMSDB
import com.gaurav.moneytracker.DB.SMSDao
import kotlinx.android.synthetic.main.activity_all_s_m_s.*

class AllSMS : AppCompatActivity() {

    var currentType = "all"
    lateinit var smsDao: SMSDao
    lateinit var adapter: TransactionRCVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_s_m_s)

        smsDao = SMSDB.getDatabase(this).SMSDao()
        val smsList = smsDao.getAllSMS()
        adapter = TransactionRCVAdapter(smsList)
        transaction_rcv.adapter = adapter

        sort.setOnClickListener {
            PopupMenu(this@AllSMS, sort).apply {
                menuInflater.inflate(R.menu.see_all_sort, menu)
                setOnMenuItemClickListener { item ->
                    if(item.itemId == R.id.all) {
                        updateList("all")
                    } else if(item.itemId == R.id.debit) {
                        updateList("Debit")
                    } else {
                        updateList("Credit")
                    }
                    true
                }
            }.show()
        }

    }

    private fun updateList(type: String) {

        if(currentType != type) {

            currentType = type
            var smsList = listOf<SMS>()

            if(type == "Credit" || type == "Debit") {
                smsList = smsDao.getSMSByType(type)
            } else {
                smsList = smsDao.getAllSMS()
            }

            adapter.smsList = smsList
            adapter.notifyDataSetChanged()

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@AllSMS, MainActivity::class.java))
        finish()
    }

}