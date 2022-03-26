package com.zplus.tataskymsales.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.example.atul.attendance.model.RechargeRequestBodyParam
import com.example.atul.attendance.model.UpdateCurrentBalance
import com.example.atul.attendance.model.UpdateRechargeStatus
import com.google.gson.Gson
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.Security.MCrypt
import com.zplus.tataskymsales.activity.LoginActivity
import com.zplus.tataskymsales.api.ApiCall
import com.zplus.tataskymsales.databse.migration.RealmMigrations
import com.zplus.tataskymsales.databse.model.LogModel
import com.zplus.tataskymsales.databse.model.RechargeRequestModel
import com.zplus.tataskymsales.databse.model.SimListModel
import com.zplus.tataskymsales.databse.table.LogTable
import com.zplus.tataskymsales.databse.table.RechargeRequest
import com.zplus.tataskymsales.databse.table.SimList
import com.zplus.tataskymsales.fragment.Homefragment
import com.zplus.tataskymsales.interfaces.GetRechargeRequestInterface
import com.zplus.tataskymsales.interfaces.ResponseInterface
import com.zplus.tataskymsales.model.tataresponse.LoginResponse
import com.zplus.tataskymsales.model.zplusresponse.MainResponse
import com.zplus.tataskymsales.model.zplusresponse.RechargeRequestResponse
import com.zplus.tataskymsales.reciever.NetworkChangeReceiver
import com.zplus.tataskymsales.utility.NetworkAvailable
import com.zplus.tataskymsales.utility.SharedPreference
import com.zplus.tataskymsales.utility.StaticUtility
import com.zplus.tataskymsales.utility.StaticUtility.log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class RechargeService : Service(), NetworkChangeReceiver.ConnectivityReceiverListener {
    var mContext: Context = this
    lateinit var Sim_List: RealmResults<SimList>
    lateinit var rechargearray: RealmResults<RechargeRequest>
    lateinit var RechargeReqHandler1: Handler
    lateinit var RechargeReqHandler2: Handler
    lateinit var RechargeReqHandler3: Handler
    lateinit var RechargeReqHandler4: Handler
    lateinit var RechargeReqHandler5: Handler
    lateinit var RechargeReqHandler6: Handler
    lateinit var RechargeReqHandler7: Handler
    lateinit var RechargeReqHandler8: Handler
    lateinit var RechargeReqHandler9: Handler
    lateinit var RechargeReqHandler10: Handler
    lateinit var retailertransactionHandler: Handler
    lateinit var mastertransactionHandler: Handler
    lateinit var fostransactionHandler: Handler
    lateinit var UpdateStatusRechargeHandler: Handler
    lateinit var currentbalanceHandler: Handler
    lateinit var getcurrentbalanceHandler: Handler
    lateinit var RechargeReqUpdateHandler: Handler
    lateinit var transactionFOSHandler: Handler
    lateinit var BalanceHandler: Handler
    lateinit var realm: Realm
    var simListModel = SimListModel()
    var rechargeRequestModel = RechargeRequestModel()
    var logModel = LogModel()
    var iscall = false
    var homefragment = Homefragment()
    lateinit var broadcaster : LocalBroadcastManager
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var isstop = false
    lateinit var obj : ResponseInterface
    lateinit var getRechargeRequestinterface : GetRechargeRequestInterface
    var networkchangereceiver : NetworkChangeReceiver = NetworkChangeReceiver()
    var isinternetavailable = true
    var islog = false
    lateinit var mSocket : Socket
    var balancechktime = 0
    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
    val current = sdf.format(Date())
    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        broadcaster = LocalBroadcastManager.getInstance(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground()
        else
            startForeground(1, Notification())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startMyOwnForeground() {
        var NOTIFICATION_CHANNEL_ID = "permanence"
        var channelName = "Background Service"
        var chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        manager.createNotificationChannel(chan)

        var notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        var notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        Log.d("Service:=>", "Service started.")

        isstop = false
        Log.d("Service:=>", "Service started.")
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val current = sdf.format(Date())
        StaticUtility.log("service started. :="+current)
        mContext.registerReceiver(networkchangereceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        initsocket()
        obj = Onresponse()
        getRechargeRequestinterface = OnGetRequest()
        Realm.init(mContext)
        var c = RealmConfiguration.Builder().schemaVersion(1).
        migration(RealmMigrations())
        //c.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(c.build())
        realm = Realm.getDefaultInstance()
        Sim_List = simListModel.getSimList(realm)
        rechargearray = rechargeRequestModel.getRequest(realm)
        //rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
        // Do a periodic task
        val handler = Handler()
        val delay = 1000 * 120 * 60 //milliseconds
        currentbalance(0)

        return START_STICKY
    }

    fun currentbalance(pos : Int){
        Handler().postDelayed({
            Log.d("currentbalance:=>", "in")
            var realm = Realm.getDefaultInstance()
            Sim_List = simListModel.getSimList(realm)
            GetBalance(pos)
            NetworkAvailable(getcurrentbalanceHandler).execute()
            balancechktime = 1000*180*60
        }, balancechktime.toLong())
    }

    private fun initsocket() {
        val mOptions = IO.Options()
        //mOptions.query = "user_id=1234&firm_id=1&username=atul"
        var firm_name = SharedPreference.GetPreference(mContext,StaticUtility.LOGINPREFERENCE, StaticUtility.FIRMNAME).toString()
        var firm_id = SharedPreference.GetPreference(mContext,StaticUtility.LOGINPREFERENCE, StaticUtility.FIRM_ID).toString()
        var user_id = SharedPreference.GetPreference(mContext,StaticUtility.LOGINPREFERENCE, StaticUtility.HASH_ID).toString()
        var user_token = SharedPreference.GetPreference(mContext,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        mOptions.query = MessageFormat.format(
            "firm_id={0}&user_id={1}&firm_name={2}&token={3}",
            firm_id, user_id, firm_name, user_token
        )
        mSocket = IO.socket(StaticUtility.SOCKETURL, mOptions)
        //if(mSocket.disconnect()) {

        mSocket.on(Socket.EVENT_CONNECT, onConnected)
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onError)
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onError)
        mSocket.on("new_recharge", new_Recharge)
        mSocket.connect()
        /*}else{
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val current = sdf.format(Date())
            Staticutility.log("Socket Connected..! "+current)
        }*/
    }

    private fun destroySocket() {
        if (mSocket == null) return
        mSocket.disconnect()
        mSocket.off(Socket.EVENT_CONNECT, onConnected)
        mSocket.off("new_recharge", new_Recharge)
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onError)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onError)
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val current = sdf.format(Date())
        StaticUtility.log("Socket destroy.. := "+current)
        initsocket()
    }

    private val new_Recharge = Emitter.Listener { args ->
        if(args.size > 0){
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val current = sdf.format(Date())
            val data = args[0] as JSONObject
            if(data != null){
                StaticUtility.log("Recharge Reqest data: \n"+data+"\n"+current)
                //var data = "{\"request_datetime\":\"2020-06-26 13:18:58\",\"recharge_txn_code\":\"20200626178641851981000\",\"operator_type_code\":\"AD\",\"operator_type\":\"AirtelDTH\",\"recharge_type\":\"DTH Recharge\",\"recharge_type_code\":\"Dth\",\"gateway_slug\":\"robotic_mob_app\",\"from_sim_lapu_no\":\"7683861013\",\"to_sim_lapu_no\":\"9727293009\",\"amount\":\"1\",\"category_name\":\"Recharge\",\"sub_category_name\":\"Normal Recharge\",\"sub_category_code\":\"NRC\",\"rechargetype_code\":\"Dth\",\"from_sim_pin_no\":\"1111\",\"usertype\":\"recharge_sim\",\"is_frc\":\"0\",\"salutation\":\"\",\"name\":\"\",\"last_name\":\"\",\"date_of_birth\":\"\",\"address_1\":\"\",\"address_2\":\"\",\"landmark\":\"\",\"pincode\":\"\",\"city\":\"\",\"state\":\"\",\"box_no\":\"\",\"vc_no\":\"\",\"alt_mob_no\":\"\",\"product_frc_id\":\"\",\"sbttype\":\"\",\"stbcategory\":\"\",\"av_pin\":\"\",\"package_price\":\"\",\"app_type\":\"stk\",\"sms_text\":\"RC 1111 9727293009 1\",\"sms_to\":\"7008548381\",\"request_type\":\"recharge\"}"
                //val type = object : TypeToken<RechargeRequestResponse>() {}.type
                var resModel = Gson().fromJson(data.toString(), RechargeRequestResponse::class.java)
                //rechargeRequestarray.add(resModel)
                //if(!iscall) {
                //iscall = true
                getRechargeRequestinterface.OnGet(resModel)

                //}
                //StaticUtility.log(resModel.toString())
            }else{
                StaticUtility.log("json data empty..! "+current)
            }
        }
        /*runOnUiThread(java.lang.Runnable {
            Toast.makeText(mContext, data.toString(), Toast.LENGTH_LONG).show()
        })*/
    }

    private val onConnected = Emitter.Listener { args ->
        //val data = args[0] as JSONObject
        /*runOnUiThread(java.lang.Runnable {
            Toast.makeText(mContext, "Connected", Toast.LENGTH_LONG).show()
        })*/
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val current = sdf.format(Date())
        StaticUtility.log("Socket Connection established..! "+current)
    }

    private val onDisconnect = Emitter.Listener { args ->
//        val data = args[0] as JSONObject
        /*runOnUiThread(java.lang.Runnable {
            Toast.makeText(mContext, "Disconnect", Toast.LENGTH_LONG).show()
        })*/
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val current = sdf.format(Date())
        StaticUtility.log("Socket disconnected "+current)
        initsocket()
    }

    private val onError = Emitter.Listener { args ->
        //val data = args[0] as JSONObject
        /* runOnUiThread(java.lang.Runnable {
             Toast.makeText(mContext, data.toString(), Toast.LENGTH_LONG).show()
         })*/
        var writelog = false
        if(isinternetavailable) {
            writelog = true
        }else if(!islog){
            writelog = true
            islog = true
        }
        if(writelog){
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val current = sdf.format(Date())
            StaticUtility.log("socket error:=  " + current)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        var broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, Restarter::class.java)
        this.sendBroadcast(broadcastIntent)
        Log.d("Service:=>", "Service destroyed.")
    }

    fun Onresponse() : ResponseInterface{
        return object : ResponseInterface{
            override fun OnResponse(response: String, rechreq : RechargeRequest) {
                var split = response.split("~")
                var respo = JSONObject(split[0])

                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val current = sdf.format(Date())
                /*var id = 0
                if(logModel.getlog(realm).size > 0){
                    id = logModel.getLastid(realm)._ID + 1
                }
                var log = LogTable(id,hash_id, respo.toString(),current)
                logModel.addLog(realm, log)*/
                StaticUtility.log(split[1]+respo.toString()+current)
                if (respo.optString("code") == "200") {
                    //rechargeRequestModel.addrequest(realm, rechreq)
                    doGetrechargeRequest()
                }else{
                    var realm = Realm.getDefaultInstance()
                    rechargeRequestModel.delrequest(realm, rechreq.recharge_txn_code)
                }
            }

            override fun OnError(response: String) {
            }

        }
    }

    fun OnGetRequest() : GetRechargeRequestInterface{
        return object : GetRechargeRequestInterface{
            override fun OnGet(rechreq: RechargeRequestResponse) {
                processrequest(rechreq)
            }
        }
    }

    fun processrequest(rechreq : RechargeRequestResponse){
        var hash_array = JSONArray()
        var realm = Realm.getDefaultInstance()
        Sim_List = simListModel.getSimList(realm)
        //var sim_hash_id = ""
        //var sim_uuid = ""
        //var sim_circle = ""
        //var sim_token = ""
        var lapuno = if(rechreq.lapu_no != null && rechreq.lapu_no != "" && rechreq.lapu_no.isNotEmpty()){
            rechreq.lapu_no
        }else{
            rechreq.from_sim_lapu_no
        }
        for(sim in Sim_List){
            if(sim.lapu_no == lapuno && sim.status == "1"){
                rechreq.hash_id = sim.hash_id
                rechreq.uuid = sim.uuid
                rechreq.circle = sim.circle
                rechreq.token = sim.token
                rechreq.recharge_token_id = sim.recharge_token_id
                rechreq.enckey = sim.enckey
                rechreq.ivspec = sim.ivspec
                rechreq.id = sim.id
                break
            }
        }
        var isadd = true
        var rechargesarray = rechargeRequestModel.getRequest(realm)
        for(recharge in rechargesarray){
            if(recharge.recharge_txn_code == rechreq.recharge_txn_code){
                isadd = false
                break
            }
        }
        if(isadd) {
            var id = 0
            if (rechargesarray.size > 0) {
                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
            }
            var rechargerequestobj: RechargeRequest
            if(rechreq.request_type == "balance_check"){
                rechargerequestobj = RechargeRequest(id, rechreq.request_datetime, rechreq.hash_id, rechreq.recharge_txn_code,
                    rechreq.operator_type_code, rechreq.operator_type, "0", "0",
                    "0", rechreq.lapu_no, "0", "0", "0",
                    "0", "0", "0", rechreq.pin_no, "", "0", "0",
                    "0", "0", "0", "0", "0", "0", "0", "0",
                    "0", "0", "0", "0", "0", "0", "0", "0", rechreq.uuid,
                    rechreq.circle, rechreq.token, "0", rechreq.request_type, rechreq.to_lapu_id, rechreq.from_lapu_id,
                    rechreq.apptype, rechreq.otp,rechreq.entitytid, rechreq.entitylogintid, rechreq.entityloginpassword,
                    rechreq.entityeprs, rechreq.role, rechreq.recharge_token_id, rechreq.enckey, rechreq.ivspec,
                rechreq.id)
            }else{
                if(rechreq.is_frc == "1") {
                    rechargerequestobj = RechargeRequest(id, rechreq.request_datetime, rechreq.hash_id, rechreq.recharge_txn_code,
                        rechreq.operator_type_code, rechreq.operator_type, rechreq.recharge_type, rechreq.recharge_type_code, rechreq.gateway_slug,
                        rechreq.from_sim_lapu_no, rechreq.to_sim_lapu_no, rechreq.amount, rechreq.category_name, rechreq.sub_category_name,
                        rechreq.sub_category_code, rechreq.rechargetype_code, rechreq.from_sim_pin_no, rechreq.usertype, rechreq.is_frc,
                        rechreq.salutation, rechreq.name, rechreq.last_name, rechreq.date_of_birth, rechreq.address_1, rechreq.address_2,
                        rechreq.alt_mob_no, rechreq.landmark, rechreq.pincode, rechreq.city, rechreq.state, rechreq.box_no, rechreq.vc_no,
                        rechreq.product_frc_id, rechreq.sbttype, rechreq.stbcategory, rechreq.av_pin, rechreq.uuid, rechreq.circle, rechreq.token,
                        rechreq.package_price, rechreq.request_type, rechreq.to_lapu_id, rechreq.from_lapu_id, rechreq.apptype, rechreq.otp,
                        rechreq.entitytid, rechreq.entitylogintid, rechreq.entityloginpassword, rechreq.entityeprs,
                        rechreq.role, rechreq.recharge_token_id, rechreq.enckey, rechreq.ivspec, rechreq.id)
                }else{
                    rechargerequestobj = RechargeRequest(id, rechreq.request_datetime, rechreq.hash_id, rechreq.recharge_txn_code,
                        rechreq.operator_type_code, rechreq.operator_type, rechreq.recharge_type, rechreq.recharge_type_code,
                        rechreq.gateway_slug, rechreq.from_sim_lapu_no, rechreq.to_sim_lapu_no, rechreq.amount, rechreq.category_name,
                        rechreq.sub_category_name, rechreq.sub_category_code, rechreq.rechargetype_code, rechreq.from_sim_pin_no, rechreq.usertype,
                        rechreq.is_frc, "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", rechreq.uuid,
                        rechreq.circle, rechreq.token, "", "", rechreq.to_lapu_id, rechreq.from_lapu_id, rechreq.apptype,
                        rechreq.otp,rechreq.entitytid,rechreq.entitylogintid, rechreq.entityloginpassword,
                        rechreq.entityeprs, rechreq.role, rechreq.recharge_token_id, rechreq.enckey, rechreq.ivspec,
                        rechreq.id)
                }
            }
            rechargeRequestModel.addrequest(realm, rechargerequestobj)
            var mainobj = JSONObject()
            mainobj.put("txn_id", rechreq.recharge_txn_code)
            mainobj.put("status", "accepted")
            log(rechreq.recharge_txn_code+" /Update recharge request Send/ "+sdf.format(Date()))
            //RechargeRequestUpdate(rechreq.hash_id)
            var authtoken = SharedPreference.GetPreference(mContext, StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
            OkHttpHandler1(mainobj,JSON,authtoken,StaticUtility.queryStringUrl1(mContext),obj, rechreq.hash_id, rechargerequestobj).execute()
        }
        /*hash_array.put(rechreq.recharge_txn_code)
        var mainobj = JSONObject()
        mainobj.put("txn_id", hash_array)
        mainobj.put("status", "accepted")
        req1 = false
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val current = sdf.format(Date())
        StaticUtility.log(sim_hash_id+"/Update recharge request Send/"+current)
        //RechargeRequestUpdate(sim_hash_id)
        var authtoken = SharedPreference.GetPreference(mContext,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        OkHttpHandler1(mainobj,JSON,authtoken,StaticUtility.queryStringUrl1(mContext),obj, sim_hash_id, rechreq).execute()*/
    }

    //region for update recharge request status
    class OkHttpHandler1(var mainobj: JSONObject, var JSON: MediaType, var auth: String, var query: String,
                         obj: ResponseInterface, hashid : String, rechreq : RechargeRequest/*,handler: Handler*/)
        : AsyncTask<Void, Void, String>() {
        var client = OkHttpClient()
        var obj = obj
        var hashid = hashid
        var rechreq = rechreq
        //var handler = handler
        override fun doInBackground(vararg params: Void?): String {
            Log.d("Recharge update Get","send")
            val body = RequestBody.create(JSON, mainobj.toString())
            val request = Request.Builder()
                .addHeader("Content-Type", StaticUtility.CONTENT_TYPE)
                .addHeader("app-id", StaticUtility.APP_ID)
                .addHeader("app-secret", StaticUtility.APP_SECRET)
                .addHeader("auth-token", auth)
                .url(StaticUtility.URLMAIN + StaticUtility.RECHARGEREQUESTSTATUS + query)
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            var responseobj = response.body().string()

            return responseobj
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            var json = JSONObject(result)
            obj.OnResponse(result+"~"+hashid, rechreq)
            /*if (handler != null) {
                val msg = Message()
                msg.obj = result
                msg.arg1 = 0
                handler.sendMessage(msg)
            }*/
        }
    }
    //endregion

    //region for get current balance
    fun GetBalance(pos : Int) {
        BalanceHandler = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var obj = JSONArray()
                    obj.put(Sim_List[pos].lapu_no)
                    obj.put(Sim_List[pos].lapu_no)
                    obj.put("MOBWEB")
                    obj.put("")
                    obj.put("parentBalance")
                    var mycript = MCrypt(Sim_List[pos].enckey, Sim_List[pos].ivspec)
                    var str = mycript.encrypt(obj.toString())
                    var param = JSONArray()
                    param.put(str + ":" + Sim_List[pos].id)
                    param.put("null")
                    param.put("null")
                    param.put("null")
                    param.put("null")
                    StaticUtility.GetBalancecall(
                        param.toString(),
                        BalanceHandler
                    )
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                val response = msg.obj as LoginResponse
                var balance = ""
                /*rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
                rechargearray = rechargeRequestModel.getRequest(realm)*/
                if (response.result.status == "failure") {
                    //StaticUtility.showMessage(mContext, response.result.errorMessage)
                } else {
                    var mycript = MCrypt(Sim_List[pos].enckey, Sim_List[pos].ivspec)
                    var str = mycript.decrypt(response.result.payload)
                    var json = JSONObject(str)
                    balance = json.optString("currentBalance")
                    UpdatCurrentBalance(Sim_List[pos].hash_id, balance,pos)
                    NetworkAvailable(currentbalanceHandler).execute()
                }
            }
            true
        })
    }
    //endregion

    //region for current balance dealer/fos
    fun UpdatCurrentBalance(
        hash_id: String,
        bal: String,
        pos : Int
    ) {
        currentbalanceHandler = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    val body = UpdateCurrentBalance(hash_id, bal)
                    StaticUtility.UpdatecurrentBalancecall(body, currentbalanceHandler, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                /*val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val current = sdf.format(Date())
                var id = 0
                if(logModel.getlog(realm).size > 0){
                    id = logModel.getLastid(realm)._ID + 1
                }
                var log = LogTable(id,"",
                    bal,current)
                logModel.addLog(realm, log)*/
                var posi = pos + 1
                if (posi < Sim_List.size) {
                    GetBalance(posi)
                    NetworkAvailable(BalanceHandler).execute()
                }
            }
            true
        })
    }
    //endregion

    fun getRechargeList() {
        Log.d("Service:=>", "Service started.")
        if (Sim_List.size > 0) {
            if (Sim_List[0].status == "1") {
                GetRechargeRequest1(Sim_List[0])
                NetworkAvailable(RechargeReqHandler1).execute()
            }
        }
        if (Sim_List.size > 1) {
            if (Sim_List[1].status == "1") {
                GetRechargeRequest2(Sim_List[1])
                NetworkAvailable(RechargeReqHandler2).execute()
            }
        }
        if (Sim_List.size > 2) {
            if (Sim_List[2].status == "1") {
                GetRechargeRequest3(Sim_List[2])
                NetworkAvailable(RechargeReqHandler3).execute()
            }
        }

        if (Sim_List.size > 3) {
            if (Sim_List[3].status == "1") {
                GetRechargeRequest4(Sim_List[3])
                NetworkAvailable(RechargeReqHandler4).execute()
            }
        }

        if (Sim_List.size > 4) {
            if (Sim_List[4].status == "1") {
                GetRechargeRequest5(Sim_List[4])
                NetworkAvailable(RechargeReqHandler5).execute()
            }
        }

        if (Sim_List.size > 5) {
            if (Sim_List[5].status == "1") {
                GetRechargeRequest6(Sim_List[5])
                NetworkAvailable(RechargeReqHandler6).execute()
            }
        }

        if (Sim_List.size > 6) {
            if (Sim_List[6].status == "1") {
                GetRechargeRequest7(Sim_List[6])
                NetworkAvailable(RechargeReqHandler7).execute()
            }
        }

        if (Sim_List.size > 7) {
            if (Sim_List[7].status == "1") {
                GetRechargeRequest8(Sim_List[7])
                NetworkAvailable(RechargeReqHandler8).execute()
            }
        }

        if (Sim_List.size > 8) {
            if (Sim_List[8].status == "1") {
                GetRechargeRequest9(Sim_List[8])
                NetworkAvailable(RechargeReqHandler9).execute()
            }
        }

        if (Sim_List.size > 9) {
            if (Sim_List[9].status == "1") {
                GetRechargeRequest10(Sim_List[9])
                NetworkAvailable(RechargeReqHandler10).execute()
            }
        }

    }

    //region for get recharge request list
    fun GetRechargeRequest1(sim: SimList) {
        RechargeReqHandler1 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler1, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }

                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest2(sim: SimList) {
        RechargeReqHandler2 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler2, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest3(sim: SimList) {
        RechargeReqHandler3 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler3, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest4(sim: SimList) {
        RechargeReqHandler4 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler4, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest5(sim: SimList) {
        RechargeReqHandler5 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler5, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest6(sim: SimList) {
        RechargeReqHandler6 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler6, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest7(sim: SimList) {
        RechargeReqHandler7 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler7, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest8(sim: SimList) {
        RechargeReqHandler8 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler8, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest9(sim: SimList) {
        RechargeReqHandler9 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler9, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")

                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for get recharge request list
    fun GetRechargeRequest10(sim: SimList) {
        RechargeReqHandler10 = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    var body = RechargeRequestBodyParam(sim.hash_id)
                    ApiCall.GetRechargeRequestcall(body, RechargeReqHandler10, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                if (msg.arg2 == 1) {
                    var obj = JSONObject(msg.obj.toString())
                    if (obj.optString("code") == "401") {
                        SharedPreference.ClearPreference(mContext, StaticUtility.LOGINPREFERENCE)
                        startActivity(Intent(mContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                } else {
                    var respo = msg.obj as MainResponse
                    var hash_array = JSONArray()
                    for (rechreq in respo.payload!!.recharges) {
                        var isadd = true
                        var rechargesarray = rechargeRequestModel.getRequest(realm)
                        for (recharge in rechargesarray) {
                            if (recharge.recharge_txn_code == rechreq.recharge_txn_code) {
                                isadd = false
                                break
                            }
                        }
                        if (isadd) {
                            var id = 0
                            if (rechargesarray.size > 0) {
                                id = rechargeRequestModel.getLastRequest(realm)._ID + 1
                            }
                            var rechargerequestobj = RechargeRequest(
                                id,
                                rechreq.request_datetime,
                                rechreq.recharge_txn_code,
                                rechreq.operator_type_code,
                                rechreq.operator_type,
                                rechreq.recharge_type,
                                rechreq.recharge_type_code,
                                rechreq.gateway_slug,
                                rechreq.from_sim_lapu_no,
                                rechreq.to_sim_lapu_no,
                                rechreq.amount,
                                rechreq.category_name,
                                rechreq.sub_category_name,
                                rechreq.sub_category_code,
                                rechreq.rechargetype_code,
                                rechreq.from_sim_pin_no,
                                rechreq.usertype,
                                sim.role,
                                sim.enckey,
                                sim.ivspec,
                                sim.id,
                                sim.instanceid,
                                sim.uuid,
                                sim.cookie,
                                sim.recharge_token_id
                            )
                            rechargeRequestModel.addrequest(realm, rechargerequestobj)
                            hash_array.put(rechreq.recharge_txn_code)
                        }
                    }
                    var mainobj = JSONObject()
                    mainobj.put("txn_id", hash_array)
                    mainobj.put("status", "accepted")
                    if (hash_array.length() > 0) {
                        RechargeRequestUpdate(hash_array)
                        var authtoken = SharedPreference.GetPreference(
                            mContext,
                            StaticUtility.LOGINPREFERENCE,
                            StaticUtility.AUTHTOKEN
                        ).toString()
                        OkHttpHandler(
                            mainobj,
                            JSON,
                            authtoken,
                            StaticUtility.queryStringUrl1(mContext),
                            RechargeReqUpdateHandler
                        ).execute()
                    } else {
                        getRechargeList()
                    }
                    //doGetrechargeRequest()
                }
            }
            true
        })
    }
    //endregion

    //region for recharge request update
    fun RechargeRequestUpdate(hash_array : JSONArray) {
        RechargeReqUpdateHandler = Handler(Handler.Callback { msg ->
            var respo = JSONObject(msg.obj.toString())
            if (respo.optString("code") == "200") {

            }

            for (i in 0 until hash_array.length()) {
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val current = sdf.format(Date())
                val hash_id = hash_array.get(i)
                var id = 0
                if(logModel.getlog(realm).size > 0){
                    id = logModel.getLastid(realm)._ID + 1
                }
                var log = LogTable(id,hash_id.toString(),
                    respo.toString(),current)
                logModel.addLog(realm, log)
            }
            doGetrechargeRequest()
            //getRechargeList()
            true
        })
    }
    //endregion

    //region for update recharge request status
    class OkHttpHandler(
        var mainobj: JSONObject,
        var JSON: MediaType,
        var auth: String,
        var query: String,
        handler: Handler
    ) : AsyncTask<Void, Void, String>() {
        var client = OkHttpClient()
        var handler = handler
        override fun doInBackground(vararg params: Void?): String? {
            val body = RequestBody.create(JSON, mainobj.toString())
            val request = Request.Builder()
                .addHeader("Content-Type", StaticUtility.CONTENT_TYPE)
                .addHeader("app-id", StaticUtility.APP_ID)
                .addHeader("app-secret", StaticUtility.APP_SECRET)
                .addHeader("auth-token", auth)
                .url(StaticUtility.URLMAIN + StaticUtility.RECHARGEREQUESTSTATUS + query)
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            var responseobj = response.body().string()

            return responseobj
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var json = JSONObject(result)
            if (handler != null) {
                val msg = Message()
                msg.obj = result
                msg.arg1 = 0
                handler.sendMessage(msg)
            }
        }

    }
    //endregion

    //region for recharge request get from database
    fun doGetrechargeRequest() {
        //Dealer = retailer
        //FOS = FOS
        //Distributor = master
        if (rechargearray.size <= 0) {
            rechargearray = rechargeRequestModel.getRequest(realm)
        }
        if (!iscall && rechargearray.size > 0) {
            iscall = true
            if (rechargearray[0].role == "Dealer") {
                finalrechargeRetailer(rechargearray[0])
                NetworkAvailable(retailertransactionHandler).execute()
            } else if(rechargearray[0].role == "FOS"){
                finalrechargeFOS(rechargearray[0])
                NetworkAvailable(transactionFOSHandler).execute()
            } else if(rechargearray[0].role == "Distributor"){
                finalrechargeMaster(rechargearray[0])
                NetworkAvailable(mastertransactionHandler).execute()
            }
        }
    }
    //endregion

    fun finalrechargeRetailer(req: RechargeRequest) {
        retailertransactionHandler = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val current = sdf.format(Date())
                    var id = 0
                    if(logModel.getlog(realm).size > 0){
                        id = logModel.getLastid(realm)._ID + 1
                    }
                    var log = LogTable(id,req.recharge_txn_code,
                        "Recharge request send.",current)
                    logModel.addLog(realm, log)
                    var obj = JSONArray()
                    obj.put(req.from_sim_lapu_no)
                    obj.put(req.to_sim_lapu_no)
                    obj.put("")
                    obj.put(req.amount.replaceAfter(".","").replace(".",""))
                    obj.put("")
                    obj.put("MOBDESKTOP")
                    obj.put(req.recharge_token_id)


                    //this is new param
                    /*obj.put("sesstionkey")//get from login response
                    obj.put(req.to_sim_lapu_no)
                    obj.put("")
                    obj.put(req.amount.replaceAfter(".","").replace(".",""))
                    obj.put("")
                    obj.put("MOBDESKTOP")
                    obj.put(req.recharge_token_id)*/
                    var mycript = MCrypt(req.enckey, req.ivspec)
                    var str = mycript.encrypt(obj.toString())
                    var param = JSONArray()
                    param.put(str + ":" + req.id)
                    param.put("null")
                    param.put("null")
                    param.put("null")
                    param.put("null")
                    param.put("null")
                    var params = mapOf(
                        "adapter" to "HttpEVDAdapter", "procedure" to "doRecharge",
                        "compressResponse" to "true", "parameters" to param.toString(),
                        "isAjaxRequest" to "true"
                    )
                    StaticUtility.RetailerRechargecall(
                        param.toString(),
                        retailertransactionHandler)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                var status = "failed"
                var tr_id = ""
                var cur_bal = ""
                var massg = ""
                var enckey = rechargearray[0].enckey
                var ivspec = rechargearray[0].ivspec
                var amount = rechargearray[0].amount
                var txn_code = rechargearray[0].recharge_txn_code
                var lapuno = rechargearray[0].from_sim_lapu_no
                rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
                rechargearray = rechargeRequestModel.getRequest(realm)
                val jsonobj = JSONObject()
                if (msg.arg2 == 0) {
                    var json = JSONObject(msg.obj.toString())
                    jsonobj.put("msg",json.toString())
                    EventBus.getDefault().post(lapuno)
                    //sendResult(rechargearray[0].from_sim_lapu_no)
                } else {
                    val response = msg.obj as LoginResponse
                    massg = response.result.errorMessage

                    /*rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
                    rechargearray = rechargeRequestModel.getRequest(realm)*/
                    if (response.result.status == "failure") {
                        jsonobj.put("msg",response.result.errorMessage)
                        //StaticUtility.showMessage(mContext, response.result.errorMessage)
                    } else {
                        status = "success"
                        var mycript = MCrypt(enckey, ivspec)
                        var str = mycript.decrypt(response.result.payload)
                        var json = JSONObject(str)
                        jsonobj.put("msg",json.toString())
                        //tr_id = respo.data.transactionID
                        //cur_bal = respo.data.currentBalance
                    }
                }
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val current = sdf.format(Date())
                jsonobj.put("recharge_id",req.recharge_txn_code)
                jsonobj.put("txn_id",txn_code)

                jsonobj.put("amount",amount)
                jsonobj.put("current_bal",cur_bal)
                jsonobj.put("status",status)
                var id = 0
                if(logModel.getlog(realm).size > 0){
                    id = logModel.getLastid(realm)._ID + 1
                }
                var log = LogTable(id,txn_code,
                    jsonobj.toString(),current)
                logModel.addLog(realm, log)
                UpdaterechargeStatus(
                    txn_code,
                    tr_id,
                    amount,
                    cur_bal,
                    status,
                    massg
                )
                NetworkAvailable(UpdateStatusRechargeHandler).execute()
            }
            true
        })
    }

    fun finalrechargeFOS(req: RechargeRequest) {
        transactionFOSHandler = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val current = sdf.format(Date())
                    var id = 0
                    if(logModel.getlog(realm).size > 0){
                        id = logModel.getLastid(realm)._ID + 1
                    }
                    var log = LogTable(id,req.recharge_txn_code,
                        "Recharge request send.",current)
                    logModel.addLog(realm, log)
                    var obj = JSONArray()
                    obj.put(req.from_sim_lapu_no)
                    obj.put(req.to_sim_lapu_no)
                    obj.put(req.amount.replaceAfter(".","").replace(".",""))
                    obj.put("MOBWEB")
                    var mycript = MCrypt(req.enckey, req.ivspec)
                    var str = mycript.encrypt(obj.toString())
                    var param = JSONArray()
                    param.put(str + ":" + req.id)
                    param.put("null")
                    param.put("null")
                    param.put("null")
                    StaticUtility.FOSRechargecall(
                        param.toString(),
                        transactionFOSHandler
                    )
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                var status = "failed"
                var tr_id = ""
                var cur_bal = ""
                var massg = ""
                var enckey = rechargearray[0].enckey
                var ivspec = rechargearray[0].ivspec
                var amount = rechargearray[0].amount
                var txn_code = rechargearray[0].recharge_txn_code
                var lapuno = rechargearray[0].from_sim_lapu_no
                rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
                rechargearray = rechargeRequestModel.getRequest(realm)
                val jsonobj = JSONObject()
                if (msg.arg2 == 0) {
                    var json = JSONObject(msg.obj.toString())
                    jsonobj.put("msg",json.toString())
                    EventBus.getDefault().post(lapuno)
                    //sendResult(rechargearray[0].from_sim_lapu_no)
                } else {
                    val response = msg.obj as LoginResponse
                    massg = response.result.errorMessage
                    /*rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
                    rechargearray = rechargeRequestModel.getRequest(realm)*/
                    if (response.result.status == "failure") {
                        jsonobj.put("msg",response.result.errorMessage)
                        //StaticUtility.showMessage(mContext, response.result.errorMessage)
                    } else {
                        status = "success"
                        var mycript = MCrypt(enckey, ivspec)
                        var str = mycript.decrypt(response.result.payload)
                        var json = JSONObject(str)
                        jsonobj.put("msg",json.toString())
                        //tr_id = respo.data.transactionID
                        //cur_bal = respo.data.currentBalance
                    }
                }
                /*if (rechargearray.size > 0) {
                    if (rechargearray[0].role == "Dealer") {
                        finalrechargeRetailer(rechargearray[0])
                        NetworkAvailable(retailertransactionHandler).execute()
                    } else if(rechargearray[0].role == "FOS"){
                        finalrechargeFOS(rechargearray[0])
                        NetworkAvailable(transactionFOSHandler).execute()
                    } else if(rechargearray[0].role == "Distributor"){
                        finalrechargeMaster(rechargearray[0])
                        NetworkAvailable(mastertransactionHandler).execute()
                    }
                } else {
                    iscall = false
                    doGetrechargeRequest()
                }*/
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val current = sdf.format(Date())
                jsonobj.put("recharge_id",txn_code)
                jsonobj.put("txn_id",tr_id)

                jsonobj.put("amount",amount)
                jsonobj.put("current_bal",cur_bal)
                jsonobj.put("status",status)
                var id = 0
                if(logModel.getlog(realm).size > 0){
                    id = logModel.getLastid(realm)._ID + 1
                }
                var log = LogTable(id,txn_code,
                    jsonobj.toString(),current)
                logModel.addLog(realm, log)
                UpdaterechargeStatus(
                    txn_code,
                    tr_id,
                    amount,
                    cur_bal,
                    status,
                    massg
                )
                NetworkAvailable(UpdateStatusRechargeHandler).execute()
            }
            true
        })
    }

    fun finalrechargeMaster(req: RechargeRequest) {
        mastertransactionHandler = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val current = sdf.format(Date())
                    var id = 0
                    if(logModel.getlog(realm).size > 0){
                        id = logModel.getLastid(realm)._ID + 1
                    }
                    var log = LogTable(id,req.recharge_txn_code,
                        "Recharge request send.",current)
                    logModel.addLog(realm, log)
                    var obj = JSONArray()
                    obj.put(req.from_sim_lapu_no)
                    obj.put(req.to_sim_lapu_no)
                    obj.put(req.amount.replaceAfter(".","").replace(".",""))
                    obj.put("MOBWEB")
                    var mycript = MCrypt(req.enckey, req.ivspec)
                    var str = mycript.encrypt(obj.toString())
                    var param = JSONArray()
                    param.put(str + ":" + req.id)
                    param.put("null")
                    param.put("null")
                    param.put("null")
                    StaticUtility.MasterRechargecall(
                        param.toString(),
                        mastertransactionHandler
                    )
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                var status = "failed"
                var tr_id = ""
                var cur_bal = ""
                var massg = ""
                var enckey = rechargearray[0].enckey
                var ivspec = rechargearray[0].ivspec
                var amount = rechargearray[0].amount
                var txn_code = rechargearray[0].recharge_txn_code
                var lapuno = rechargearray[0].from_sim_lapu_no
                rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
                rechargearray = rechargeRequestModel.getRequest(realm)
                val jsonobj = JSONObject()
                if (msg.arg2 == 0) {
                    var json = JSONObject(msg.obj.toString())
                    jsonobj.put("msg",json.toString())
                    EventBus.getDefault().post(lapuno)
                    //sendResult(rechargearray[0].from_sim_lapu_no)
                } else {
                    val response = msg.obj as LoginResponse
                    massg = response.result.errorMessage
                    /*rechargeRequestModel.delrequest(realm, rechargearray[0].recharge_txn_code)
                    rechargearray = rechargeRequestModel.getRequest(realm)*/
                    if (response.result.status == "failure") {
                        jsonobj.put("msg",response.result.errorMessage)
                        //StaticUtility.showMessage(mContext, response.result.errorMessage)
                    } else {
                        status = "success"
                        var mycript = MCrypt(enckey, ivspec)
                        var str = mycript.decrypt(response.result.payload)
                        var json = JSONObject(str)
                        jsonobj.put("msg",json.toString())
                        //tr_id = respo.data.transactionID
                        //cur_bal = respo.data.currentBalance
                    }
                }
                /*if (rechargearray.size > 0) {
                    if (rechargearray[0].role == "Dealer") {
                        finalrechargeRetailer(rechargearray[0])
                        NetworkAvailable(retailertransactionHandler).execute()
                    } else if(rechargearray[0].role == "FOS"){
                        finalrechargeFOS(rechargearray[0])
                        NetworkAvailable(transactionFOSHandler).execute()
                    } else if(rechargearray[0].role == "Distributor"){
                        finalrechargeMaster(rechargearray[0])
                        NetworkAvailable(mastertransactionHandler).execute()
                    }
                } else {
                    iscall = false
                    doGetrechargeRequest()
                }*/
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val current = sdf.format(Date())
                jsonobj.put("recharge_id",txn_code)
                jsonobj.put("txn_id",tr_id)
                jsonobj.put("amount",amount)
                jsonobj.put("current_bal",cur_bal)
                jsonobj.put("status",status)
                var id = 0
                if(logModel.getlog(realm).size > 0){
                    id = logModel.getLastid(realm)._ID + 1
                }
                var log = LogTable(id,txn_code,
                    jsonobj.toString(),current)
                logModel.addLog(realm, log)
                UpdaterechargeStatus(
                    txn_code,
                    tr_id,
                    amount,
                    cur_bal,
                    status,
                    massg
                )
                NetworkAvailable(UpdateStatusRechargeHandler).execute()
            }
            true
        })
    }

    //region for recharge status update dealer
    fun UpdaterechargeStatus(
        txt_code: String,
        tr_id: String,
        amount: String,
        cur_bal: String,
        status: String,
        masg: String
    ) {
        UpdateStatusRechargeHandler = Handler(Handler.Callback { msg ->
            if (msg.arg1 == 1) {
                if (msg.obj as Boolean) {
                    val body = UpdateRechargeStatus(
                        txt_code, tr_id, amount, cur_bal, status, masg
                    )
                    ApiCall.UpdateRechargeRequestStatuscall(body, UpdateStatusRechargeHandler, mContext)
                } else
                    StaticUtility.showMessage(mContext, getString(R.string.network_error))
            } else if (msg.arg1 == 0) {
                var id = 0
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val current = sdf.format(Date())
                if(logModel.getlog(realm).size > 0){
                    id = logModel.getLastid(realm)._ID + 1
                }
                var log = LogTable(id,txt_code,
                    "Recharge request updated to server.",current)
                logModel.addLog(realm, log)
                if (rechargearray.size > 0) {
                    if (rechargearray[0].role == "Dealer") {
                        finalrechargeRetailer(rechargearray[0])
                        NetworkAvailable(retailertransactionHandler).execute()
                    } else if(rechargearray[0].role == "FOS"){
                        finalrechargeFOS(rechargearray[0])
                        NetworkAvailable(transactionFOSHandler).execute()
                    } else if(rechargearray[0].role == "Distributor"){
                        finalrechargeMaster(rechargearray[0])
                        NetworkAvailable(mastertransactionHandler).execute()
                    }
                } else {
                    iscall = false
                    //getRechargeList()
                    //doGetrechargeRequest()
                }
            }

            true
        })
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {

    }
    //endregion
}