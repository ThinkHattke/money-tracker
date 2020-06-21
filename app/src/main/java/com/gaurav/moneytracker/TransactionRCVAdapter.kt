package com.gaurav.moneytracker

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gaurav.moneytracker.DB.SMS
import kotlinx.android.synthetic.main.card_transaction.*
import kotlinx.android.synthetic.main.card_transaction.view.*
import timber.log.Timber
import java.lang.Exception
import java.util.*

class TransactionRCVAdapter(var smsList: List<SMS>) :
    RecyclerView.Adapter<TransactionRCVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).run {
            inflate(R.layout.card_transaction, parent, false)
        }
        return ViewHolder(view)
    }

    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bindToViewHolder(position: Int) {
            val data = smsList[position]

            if(data.type == "Debit") {
                view.image_type.setImageResource(R.drawable.icon_minus)
                view.type.text = "Debit"
            } else {
                view.image_type.setImageResource(R.drawable.icon_add)
                view.type.text = "Credit"
            }

            view.amount.text = "₹ "+data.amount+" - "+data.sender

            try {
                val timeAgo = DateUtils.getRelativeTimeSpanString(data.date.toLong() , Calendar.getInstance().timeInMillis, DateUtils.MINUTE_IN_MILLIS)
                view.date.text = timeAgo
            } catch (e: Exception) {
                Timber.e(e)
            }

            if(position == smsList.size-1) {
                view.partition.visibility = View.GONE
            }

        }
    }

    override fun getItemCount(): Int {
        return smsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindToViewHolder(position)
    }

}