package com.zplus.tataskymsales.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.databse.table.LogTable
import io.realm.RealmResults
import kotlinx.android.synthetic.main.logcate_layout.view.*

class AdapterLog (val context: Context, val log : RealmResults<LogTable>,
                  onItemClickListener: OnClick) : RecyclerView.Adapter<loglistHolder>() {
    interface OnClick {
        fun OnClick(log: LogTable, type : Int)
    }

    var onclick : OnClick? = null
    var log_list  = log
    var onItemClickListener = onItemClickListener
    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return log_list.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): loglistHolder {
        return loglistHolder(LayoutInflater.from(context).inflate(R.layout.logcate_layout, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(loglistholder: loglistHolder, position: Int) {
        //holder?.tvAnimalType?.text = items.get(position)
        loglistholder.txn_id.text = log_list[position].tr_id
        loglistholder.response_text.text = log_list[position].response
        loglistholder.date_time.text = log_list[position].date_time

    }
}

class loglistHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val txn_id = view.txn_id
    val response_text = view.response_text
    val date_time = view.date_time
}
