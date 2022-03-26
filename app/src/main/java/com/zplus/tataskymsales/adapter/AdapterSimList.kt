package com.zplus.tataskymsales.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.databse.table.SimList
import com.zplus.tataskymsales.utility.StaticUtility
import io.realm.RealmResults
import kotlinx.android.synthetic.main.row_layout_sim_list.view.*

class AdapterSimList (val context: Context, Sim_list : RealmResults<SimList>,
                      onItemClickListener: OnClick) : RecyclerView.Adapter<simlistHolder>() {
    interface OnClick {
        fun OnClick(sim: SimList, type : Int)
    }

    var onclick : OnClick? = null
    var sim_list  = Sim_list
    var onItemClickListener = onItemClickListener
    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return sim_list.size
    }

    fun updateData(sim_List : RealmResults<SimList>){
        sim_list = sim_List
        notifyDataSetChanged()
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): simlistHolder {
        return simlistHolder(LayoutInflater.from(context).inflate(R.layout.row_layout_sim_list, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(simlistholder: simlistHolder, position: Int) {
        //holder?.tvAnimalType?.text = items.get(position)
        var maskpattern = ""
        for(i in 1..sim_list[position].pin_no.length){
            if(i == sim_list[position].pin_no.length){
                maskpattern += "#"
            }else{
                maskpattern += "*"
            }
        }
        var maskmobile = StaticUtility.maskCardNumber(sim_list[position].lapu_no,"******####")
        simlistholder.txt_mobile_no.text = maskmobile
        var mask = StaticUtility.maskCardNumber(sim_list[position].pin_no,maskpattern)
        simlistholder.txt_pin.text = mask
        //simlistholder.txt_pin.text = sim_list[position].pin_no

        simlistholder.btn_login.setOnClickListener{
            onclick = onItemClickListener
            if(simlistholder.btn_login.text == "LogOut"){
                onclick?.OnClick(sim_list[position],0)
            } else {
                onclick?.OnClick(sim_list[position],1)
            }
        }

        if(sim_list[position].status == "1"){
            //val backgroundGradient = simlistholder.img_status.background as ColorDrawable
            //backgroundGradient.color = Color.parseColor("#008000")
            simlistholder.btn_login.text = "LogOut"
            simlistholder.btn_login.setBackgroundColor(
                ContextCompat.getColor(context,
                    R.color.green))
            simlistholder.img_status.setBackgroundResource(R.drawable.status_g)
        }else{
            //val backgroundGradient = simlistholder.img_status.background as ColorDrawable
            //backgroundGradient.color = Color.parseColor("#e42828")
            simlistholder.btn_login.text = "LogIn"
            simlistholder.btn_login.setBackgroundColor(ContextCompat.getColor(context,
                R.color.red))
            simlistholder.img_status.setBackgroundResource(R.drawable.status)
        }

    }
}

class simlistHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val txt_mobile_no = view.txt_mobile_no
    val txt_pin = view.txt_pin
    val img_status = view.img_status
    val btn_login = view.btn_login
}

