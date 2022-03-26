package com.zplus.tataskymsales.fragment

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.atul.attendance.model.UpdateSimStatusBodyParam

import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.Security.MCrypt
import com.zplus.tataskymsales.adapter.AdapterSimList
import com.zplus.tataskymsales.api.ApiCall
import com.zplus.tataskymsales.databse.migration.RealmMigrations
import com.zplus.tataskymsales.databse.model.SimListModel
import com.zplus.tataskymsales.databse.table.SimList
import com.zplus.tataskymsales.model.tataresponse.LoginResponse
import com.zplus.tataskymsales.model.tataresponse.response
import com.zplus.tataskymsales.model.zplusresponse.MainResponse
import com.zplus.tataskymsales.reciever.NetworkChangeReceiver
import com.zplus.tataskymsales.service.RechargeService
import com.zplus.tataskymsales.utility.NetworkAvailable
import com.zplus.tataskymsales.utility.StaticUtility
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_homefragment.view.*
import org.json.JSONArray
import org.json.JSONObject
import android.content.BroadcastReceiver
import android.content.Context.ACTIVITY_SERVICE
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class Homefragment : Fragment(), NetworkChangeReceiver.ConnectivityReceiverListener {
    lateinit var loginHandler: Handler
    lateinit var updatesimstatusHandler: Handler
    lateinit var getconnectedsimlistHandler: Handler
    lateinit var instanceidtokenHandler: Handler
    lateinit var enckeyivspecHandler: Handler
    lateinit var mContext : Activity
    lateinit var customerinfoHandler: Handler
    lateinit var adapter : AdapterSimList
    lateinit var sim_list : RealmResults<SimList>
    lateinit var sim_list_db : RealmResults<SimList>
    lateinit var realm : Realm
    var simListModel = SimListModel()
    lateinit var receiver : BroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_homefragment, container, false)

        Realm.init(activity!!)
        var c = RealmConfiguration.Builder().schemaVersion(1).
            migration(RealmMigrations())
        //c.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(c.build())

        realm = Realm.getDefaultInstance()
        mContext = activity!!
        mContext.registerReceiver(
            NetworkChangeReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        setdata(view)
        return view
    }
    fun isServiceRunning(serviceClassName: String): Boolean {
        val manager = mContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { it.service.className == serviceClassName }
    }

    private fun setdata(view: View) {
        sim_list_db = simListModel.getSimList(realm)
        sim_list = simListModel.getSimList(realm)

        if(sim_list.size>0){
            for(sim in sim_list){
                if(sim.status == "1"){
                    if(!isServiceRunning(RechargeService::class.java.name)){
                        mContext.startService(Intent(mContext, RechargeService::class.java))
                    }
                    break
                }
            }
        }

        view.recyclrer_sim_list.layoutManager = LinearLayoutManager(mContext)
        adapter = AdapterSimList(mContext, sim_list,
            object : AdapterSimList.OnClick {
                override fun OnClick(sim: SimList, type : Int) {
                    /* databaseHandler.updateSim(sim, "1", "DL", "10211511")
                     sim_list = databaseHandler.GetSimList()
                     adapter.updateData(sim_list)*/
                    if(type == 0){
                        //Logout(sim, view)
                        //NetworkAvailable(loginHandler).execute()
                        //var sim = simListModel.getsimfromlapuno(realm, lapuno)
                        /*last10TransactionApiCall(sim)
                        NetworkAvailable(loginHandler).execute()*/
                    }else {
                        GetEncKeyApiCall(sim)
                        NetworkAvailable(enckeyivspecHandler).execute()
                    }
                }
            })
        view.recyclrer_sim_list.adapter = adapter
        GetConnectedSimList(view)
        NetworkAvailable(getconnectedsimlistHandler).execute()

        view.swiperefresh.setOnRefreshListener {
            sim_list_db = simListModel.getSimList(realm)
            GetConnectedSimList(view)
            NetworkAvailable(getconnectedsimlistHandler).execute()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun Updatedata(lapuno : String) {
        var sim = simListModel.getsimfromlapuno(realm, lapuno)
        var simobj = SimList(sim._ID,sim.lapu_no, sim.lapu_name, sim.sim_no, sim.pin_no, sim.sim_type,
            sim.recharge_type_name, sim.recharge_type_code, sim.hash_id, sim.has_credentials,
            sim.user_type, sim.instanceid,
            sim.token, sim.cookie,sim.uuid,sim.id, sim.enckey, sim.ivspec, sim.role, sim.recharge_token_id,"0")
        simListModel.addSim(realm, simobj)
        sim_list = simListModel.getSimList(realm)
        adapter.updateData(sim_list)
        //setdata(view!!)
    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
    /**
     * Callback will be called when there is change
     */
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if(isConnected) {
            if(!isServiceRunning(RechargeService::class.java.name)){
                mContext.startService(Intent(mContext, RechargeService::class.java))
            }
        }
        //Toast.makeText(mContext, isConnected.toString(), Toast.LENGTH_LONG).show()
    }

    //region for get sim list
    fun GetConnectedSimList(view : View){
        view.home_loader.visibility = View.VISIBLE
        getconnectedsimlistHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    ApiCall.GetConnectedSimListcall(getconnectedsimlistHandler, mContext)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                view.home_loader.visibility = View.GONE
                view.swiperefresh.isRefreshing = false
                var respo = msg.obj as MainResponse
                if(respo.code == "200"){
                    for(sim in respo.payload!!.sim){
                        if(sim_list.size > 0) {
                            var _id = simListModel.getLastSim(realm)._ID+1
                            var status = "0"
                            var usertype = "0"
                            var instanceid = "0"
                            var token = "0"
                            var cookie = "0"
                            var id = "0"
                            var enckey = "0"
                            var ivspec = "0"
                            var role = "0"
                            var recharge_token_id = "0"
                            var uuid = StaticUtility.getuuid(mContext)
                            for (dbsim in sim_list_db) {
                                if (sim.lapu_no == dbsim.lapu_no) {
                                    status = dbsim.status
                                    usertype = dbsim.user_type
                                    _id = dbsim._ID
                                    instanceid = dbsim.instanceid
                                    token = dbsim.token
                                    cookie = dbsim.cookie
                                    uuid = dbsim.uuid
                                    id = dbsim.id
                                    enckey = dbsim.enckey
                                    ivspec = dbsim.ivspec
                                    role = dbsim.role
                                    recharge_token_id = dbsim.recharge_token_id
                                    //sim.app_credentials.eps_password = "4mD6A39d"
                                }
                            }
                            var simobj = SimList(_id,sim.lapu_no, sim.lapu_name, sim.sim_no, sim.pin_no, sim.sim_type,
                                sim.recharge_type_name, sim.recharge_type_code, sim.hash_id, sim.has_credentials,
                                usertype, instanceid,
                                token, cookie,uuid,id, enckey, ivspec, role, recharge_token_id,status)
                            simListModel.addSim(realm, simobj)
                        }else{
                            var simobj = SimList(0,sim.lapu_no, sim.lapu_name, sim.sim_no, sim.pin_no, sim.sim_type,
                                sim.recharge_type_name, sim.recharge_type_code, sim.hash_id, sim.has_credentials,
                                "0", "0",
                                "0","0",StaticUtility.getuuid(mContext),"0","0","0","0",
                                "0","0")
                            simListModel.addSim(realm, simobj)
                        }
                    }
                    for(dbsim in sim_list_db){
                        var isremove = true
                        var lapuno = dbsim.lapu_no
                        for(sim in respo.payload!!.sim){
                            if (sim.lapu_no == dbsim.lapu_no) {
                                isremove = false
                            }
                        }
                        if(isremove){
                            simListModel.delsim(realm,dbsim._ID)
                        }
                    }
                    sim_list = simListModel.getSimList(realm)
                    adapter.updateData(sim_list)

                }
            }
            true
        })
    }
    //endregion

    fun GetInstanceIdandTokenApiCall(sim : SimList){
        instanceidtokenHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var param = JSONArray()
                    param.put("")
                    var params = mapOf("adapter" to "HttpEncGetKey", "procedure" to "getkey",
                        "compressResponse" to "true", "parameters" to param.toString(),
                        "isAjaxRequest" to "true", "x" to "0.05143110301586007c")
                    StaticUtility.call(params, sim.uuid,instanceidtokenHandler)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                var res = msg.obj.toString().split("coockie")
                var header = res[1]
                var headers = header.split("\n")
                var cookie = ""
                for((i, head) in headers.withIndex()){
                    if(head.contains("Set-Cookie"))
                    {
                        var headers = head.split(";")
                        if(cookie == "") {
                            cookie = headers[0].replace(" ","")
                        }else{
                            cookie = cookie+"; "+ headers[0].replace(" ","").replace(";","")
                        }
                    }
                }
                cookie = cookie.replace("Set-Cookie:","").replace("Path=/","").
                    replace("HttpOnly","").replace("path=/","")
                val response = JSONObject(res[0])
                var challenges = response.getJSONObject("challenges")
                var wl_antiXSRFRealm = challenges.getJSONObject("wl_antiXSRFRealm")
                var wl_deviceNoProvisioningRealm = challenges.getJSONObject("wl_deviceNoProvisioningRealm")
                var WL_Instance_Id = wl_antiXSRFRealm.getString("WL-Instance-Id")
                var token = wl_deviceNoProvisioningRealm.getString("token")

                var simobj = SimList(sim._ID,sim.lapu_no, sim.lapu_name, sim.sim_no, sim.pin_no, sim.sim_type,
                    sim.recharge_type_name, sim.recharge_type_code, sim.hash_id, sim.has_credentials,
                    sim.user_type, WL_Instance_Id ,
                    token,cookie,sim.id, sim.enckey, sim.ivspec,sim.role, sim.recharge_token_id,sim.status)
                /*GetEncKeyApiCall(simobj)
                NetworkAvailable(enckeyivspecHandler).execute()*/
            }
            true
        })
    }

    fun GetEncKeyApiCall(sim : SimList){
        enckeyivspecHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var param = JSONArray()
                    param.put("")
                    StaticUtility.GetEncKeycall(param.toString(), enckeyivspecHandler)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                val response = msg.obj as response

                /*SharedPreference.CreatePreference(mContext, StaticUtility.ENCKEYPREFERENCE)
                SharedPreference.SavePreference(StaticUtility.ID, response.result.payload.id)
                SharedPreference.SavePreference(StaticUtility.IVKEY, response.result.payload.ivKey)
                SharedPreference.SavePreference(StaticUtility.ENCKEY, response.result.payload.enckey)*/

                var simobj = SimList(sim._ID,sim.lapu_no, sim.lapu_name, sim.sim_no, sim.pin_no, sim.sim_type,
                    sim.recharge_type_name, sim.recharge_type_code, sim.hash_id, sim.has_credentials,
                    sim.user_type, sim.instanceid ,
                    sim.token,sim.cookie,sim.uuid,response.result.payload.id, response.result.payload.enckey,
                    response.result.payload.ivKey,sim.role, sim.recharge_token_id,sim.status)
                LoginApiCall(simobj)
                NetworkAvailable(loginHandler).execute()
            }
            true
        })
    }

    fun LoginApiCall(sim : SimList){
        loginHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var obj = JSONArray()
                    obj.put("9178497060")
                    obj.put("zplus@2022")
                    obj.put("MOBWEB")
                    var mycript = MCrypt(sim.enckey, sim.ivspec)
                    var str = mycript.encrypt(obj.toString())
                    var param = JSONArray()
                    param.put(str+":"+sim.id)
                    param.put("phase3")
                    param.put("null")
                    StaticUtility.Logincall(param.toString(), loginHandler)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                val response = msg.obj as LoginResponse
                if(response.result.status == "failure"){
                    StaticUtility.showMessage(mContext, response.result.errorMessage)
                }else {
                    var mycript = MCrypt(sim.enckey, sim.ivspec)
                    var str = mycript.decrypt(response.result.payload)
                    var json = JSONObject(str)
                    var simobj = SimList(sim._ID,sim.lapu_no, sim.lapu_name, sim.sim_no, sim.pin_no, sim.sim_type,
                        sim.recharge_type_name, sim.recharge_type_code, sim.hash_id, sim.has_credentials,
                        sim.user_type, sim.instanceid ,
                        sim.token,sim.cookie,sim.uuid,sim.id, sim.enckey, sim.ivspec,json.optString("role"),
                        json.optString("recharge_token_id"),"1")
                    simListModel.addSim(realm, simobj)
                    sim_list = simListModel.getSimList(realm)
                    adapter.updateData(sim_list)
                    if(!isServiceRunning(RechargeService::class.java.name)){
                        mContext.startService(Intent(mContext, RechargeService::class.java))
                    }
                    StaticUtility.showMessage(mContext, json.optString("message"))
                    UpdateSimStatus(sim.hash_id,"1")
                    NetworkAvailable(updatesimstatusHandler).execute()
                }

                //api response success
               /* {
                    "MobileNumber":"9178497060",
                    "role":"Dealer",
                    "timestamp":"28/09/2019 15:05:03",
                    "message":"Login authentication Success.",
                    "recharge_token_id":"83e22789-8114-4598-9faf-5adaa5060c59",
                    "iconToDisable":"packageAdd~accountSetup~rePushOrder~packageAddTrai",
                    "appSignature":"0qcQdwjN32IsGIqFqpuiJkor//E",
                    "ForASIASM":[
                    {
                        "OU_NUM":"ASI_5855",
                        "ASI_NAME":"Sandeep Bhadoria",
                        "ASI_PH_NUM":"9727754623",
                        "ASM_NAME":"Pradeep Maurya",
                        "ASM_PH_NUM":"9687600022"
                    }
                    ],
                    "imgPath":[
                    "https://www.tatasky.com/msales/SlotSetB/Slot1.jpg~ ",
                    "https://www.tatasky.com/msales/SlotSetB/Slot2.jpg~ ",
                    "https://www.tatasky.com/msales/SlotSetB/Slot3.jpg~"
                    ]
                }*/
            }
            true
        })
    }

    fun last10TransactionApiCall(sim : SimList){
        loginHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var obj = JSONArray()
                    obj.put("9178497060")
                    obj.put("Recharge")
                    obj.put("MOBWEB")
                    var mycript = MCrypt(sim.enckey, sim.ivspec)
                    var str = mycript.encrypt(obj.toString())
                    var param = JSONArray()
                    param.put(str+":"+sim.id)
                    param.put("phase3")
                    param.put("null")
                    StaticUtility.Last10Transactioncall(param.toString(), loginHandler)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                val response = msg.obj as LoginResponse
                if(response.result.status == "failure"){
                    StaticUtility.showMessage(mContext, response.result.errorMessage)
                }else {
                    var mycript = MCrypt(sim.enckey, sim.ivspec)
                    var str = mycript.decrypt(response.result.payload)
                    var json = JSONObject(str)
                    var simobj = SimList(sim._ID,sim.lapu_no, sim.lapu_name, sim.sim_no, sim.pin_no, sim.sim_type,
                        sim.recharge_type_name, sim.recharge_type_code, sim.hash_id, sim.has_credentials,
                        sim.user_type, sim.instanceid ,
                        sim.token,sim.cookie,sim.uuid,sim.id, sim.enckey, sim.ivspec,json.optString("role"),
                        json.optString("recharge_token_id"),"1")
                    simListModel.addSim(realm, simobj)
                    sim_list = simListModel.getSimList(realm)
                    adapter.updateData(sim_list)
                    if(!isServiceRunning(RechargeService::class.java.name)){
                        mContext.startService(Intent(mContext, RechargeService::class.java))
                    }
                    StaticUtility.showMessage(mContext, json.optString("message"))
                    UpdateSimStatus(sim.hash_id,"1")
                    NetworkAvailable(updatesimstatusHandler).execute()
                }

                //api response success
                /* {
                     "MobileNumber":"9178497060",
                     "role":"Dealer",
                     "timestamp":"28/09/2019 15:05:03",
                     "message":"Login authentication Success.",
                     "recharge_token_id":"83e22789-8114-4598-9faf-5adaa5060c59",
                     "iconToDisable":"packageAdd~accountSetup~rePushOrder~packageAddTrai",
                     "appSignature":"0qcQdwjN32IsGIqFqpuiJkor//E",
                     "ForASIASM":[
                     {
                         "OU_NUM":"ASI_5855",
                         "ASI_NAME":"Sandeep Bhadoria",
                         "ASI_PH_NUM":"9727754623",
                         "ASM_NAME":"Pradeep Maurya",
                         "ASM_PH_NUM":"9687600022"
                     }
                     ],
                     "imgPath":[
                     "https://www.tatasky.com/msales/SlotSetB/Slot1.jpg~ ",
                     "https://www.tatasky.com/msales/SlotSetB/Slot2.jpg~ ",
                     "https://www.tatasky.com/msales/SlotSetB/Slot3.jpg~"
                     ]
                 }*/
            }
            true
        })
    }

    //region for update sim status
    fun UpdateSimStatus(hash_id : String, status : String){
        view!!.home_loader.visibility = View.VISIBLE
        updatesimstatusHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var body = UpdateSimStatusBodyParam(hash_id,status)
                    ApiCall.UpdateSimStatuscall(body,updatesimstatusHandler, mContext)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                view!!.home_loader.visibility = View.GONE
                var respo = msg.obj as MainResponse
            }
            true
        })
    }
    //endregion

    fun updateui(lapuno : String){
        realm = Realm.getDefaultInstance()
        var sim = simListModel.getsimfromlapuno(realm, lapuno)
        var simobj = SimList(sim._ID,sim.lapu_no, sim.lapu_name,sim.sim_no,sim.pin_no,sim.sim_type,sim.recharge_type_name,
            sim.recharge_type_code,sim.hash_id,sim.has_credentials,sim.user_type,sim.instanceid,sim.token,sim.cookie,sim.uuid,
            sim.id,sim.enckey,sim.ivspec,sim.role,sim.recharge_token_id,"0")
        simListModel.addSim(realm, simobj)
        sim_list = simListModel.getSimList(realm)
        adapter.updateData(sim_list)
    }
}

