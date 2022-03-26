package com.zplus.tataskymsales.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.adapter.AdapterLog
import com.zplus.tataskymsales.databse.model.LogModel
import com.zplus.tataskymsales.databse.table.LogTable
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_log.view.*

class LogFragment : Fragment() {

    lateinit var log : RealmResults<LogTable>
    lateinit var mContext : Context
    lateinit var adapter : AdapterLog
    lateinit var realm : Realm
    var logmodel = LogModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_log, container, false)

        mContext = activity!!
        realm = Realm.getDefaultInstance()

        log = logmodel.getlog(realm)

        view.recycler_list_log.layoutManager = LinearLayoutManager(mContext)

        adapter = AdapterLog(mContext, log,
            object : AdapterLog.OnClick {
                override fun OnClick(log: LogTable, type: Int) {

                }
            })
        view.recycler_list_log.adapter = adapter

        return view
    }


}
