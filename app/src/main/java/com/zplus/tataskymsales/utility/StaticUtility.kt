package com.zplus.tataskymsales.utility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.example.atul.attendance.model.UpdateCurrentBalance
import com.example.atul.retrofit.api.ApiClient
import com.example.atul.retrofit.api.ApiInterface
import com.zplus.tataskymsales.api.ApiClientMain
import com.zplus.tataskymsales.fragment.LogFragment
import com.zplus.tataskymsales.model.zplusresponse.MainResponse
import com.zplus.tataskymsales.model.tataresponse.LoginResponse
import com.zplus.tataskymsales.model.tataresponse.response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


object StaticUtility {

    val APP_ID = "98mif739527394rer12294a1951df7dd11fa3d3123"
    val APP_SECRET = "d1ebadd478dfrer8afed3595ac93812a1dc12398mi"
    val CONTENT_TYPE = "application/json"


    //val URL = "http://192.168.0.107/crm/attendence_api/"
    val URL = "https://beta.msales.tatasky.com/mfp/api/adapters/"
    val URLMAIN = "http://18.220.66.173/me-autobots/robotic-mob-app/"
    val SOCKETURL = "http://18.220.66.173:8085/stage_roboAccount"

    //region For send mail to developer for any exception in app
    val EMAIL = "tt957621@gmail.com"
    val PASSWORD = "tt957621@gmail.com1"
    val TOEMAIL = "atul@parghiinfotech.com"
    val SUBJECT = "Initial app exception Report"
    //endregion

    //region For device information
    var devicename = ""
    var deviceos = ""
    var app_version = ""
    var device_id = ""
    val DEVICEINFOPREFERENCE = "deviceinfoPreference"
    var DeviceName = "DeviceName"
    var DeviceOs = "DeviceOs"
    var App_Version = "App_Version"
    //endregion

    //region For SharedPreferences
    val P_USER_DATA = "user_data"
    val ENCKEYPREFERENCE = "enckeypreference"
    val ENCKEY = "enckey"
    val IVKEY = "ivkey"
    val ID = "id"
    //endregion

    //region for login shared preference
    val LOGINPREFERENCE = "loginpreference"
    val AUTHTOKEN = "auth_token"
    val LOGOURL = "logo_url"
    val FIRMNAME = "firm_name"
    val HASH_ID = "hash_id"
    val FIRM_ID = "firm_id"
    val APPID = "app_id"
    val APPSECRET = "app_secret"

    //region For api list
    const val LOGIN = "app/frontend/login"
    //endregion

    //region For Fcm
    val SENT_TOKEN_TO_SERVER = "sentTokenToServer"
    val FCM_TOKEN = "fcm_token"
    val PUSH_NOTIFICATION = "pushNotification"
    //endregion

    //region for api list for our server
    const val LOGINMAIN = "login"
    const val LOGOUT = "logout"
    const val UPDATEBALANCE = "api/balance/save"
    const val GETCONECTEDSIMLIST = "api/recharge-sims/list"
    const val GETRECHARGEREQUEST = "api/recharges/list"
    const val UPDATESIMSTATUS = "api/recharge-sims/update-sim-status"
    const val RECHARGEREQUESTSTATUS = "api/recharges/update-status"
    const val UPDATERECHARGEREQUEST = "api/recharges/recharge-response"
    //endregion

    //SharedPreferences Name
    var DATA = "data"
    //end

    //SharedPreferences key name
    var DEVICE_ID = "device_id"
    var UUID_ID = "device_id"
    //end

    fun sendMail(message : String, response : String){
        val sm = SendMail(StaticUtility.TOEMAIL, StaticUtility.SUBJECT,
            message+response,
            StaticUtility.EMAIL, StaticUtility.PASSWORD)
        //Executing sendmail to send email
        sm.execute()
    }

    fun showMessage(context : Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    //region For add fragment
    fun addFragmenttoActivity(manager: FragmentManager, fragment: Fragment, frameId: Int, backstak : String) {
        val transaction = manager.beginTransaction()
        transaction.replace(frameId, fragment)
        if(backstak == ""){
            transaction.addToBackStack(null)
        }else {
            transaction.addToBackStack(backstak)
        }
        transaction.commit()
    }
    //endregion

    //region for mask number
    fun maskCardNumber(cardNumber: String, mask: String): String {

        // format the number
        var index = 0
        val maskedNumber = StringBuilder()
        for (i in 0 until mask.length) {
            val c = mask[i]
            if (c == '#') {
                maskedNumber.append(cardNumber[index])
                index++
            } else if (c == '*') {
                maskedNumber.append(c)
                index++
            } else {
                maskedNumber.append(c)
            }
        }

        // return the masked number
        return maskedNumber.toString()
    }
    //endregion


    fun call(body : Map<String, String>, uuid : String,handler : Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.apicall("6.1.0.02.20160528-1310","3723148528-1975679078-2450172318",
            "1.1.2", uuid, body
        ).enqueue(object : Callback<response> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<response>, response: Response<response>) {
                if(handler != null){
                    var header = response.headers()
                    var cookie = response.raw().request().headers().get("Set-Cookie")
                     cookie = response.headers().get("Set-Cookie")
                    if(response.code() == 401){
                        var str = convertStreamToString(response).replace("\n","").
                                replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str+"coockie"+header
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<response>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*StaticUtility.sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }

    fun GetEncKeycall(param : String, handler: Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.GetEncKeyapicall("BetaMsales-cookie=2289500426.30755.0000",param).enqueue(object : Callback<response> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<response>, response: Response<response>) {
                if(handler != null){
                    if(response.code() == 401){
                        var str = convertStreamToString(response).replace("\n","").
                            replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<response>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString())*/
            }
        })
    }

    fun Logincall(body : String, handler : Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.Loginapicall("BetaMsales-cookie=2289500426.30755.0000",body
        ).enqueue(object : Callback<LoginResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(handler != null){
                    if(response.code() == 401){
                        var str = convertStreamToString1(response).replace("\n","").
                            replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }

    fun Last10Transactioncall(body : String, handler : Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.Last10Transactionapicall("BetaMsales-cookie=2289500426.30755.0000",body
        ).enqueue(object : Callback<LoginResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(handler != null){
                    if(response.code() == 401){
                        var str = convertStreamToString1(response).replace("\n","").
                            replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }

    fun RetailerRechargecall(body : String, handler : Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.RetailerRechargeapicall("BetaMsales-cookie=2289500426.30755.0000" ,body
        ).enqueue(object : Callback<LoginResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(handler != null){
                    var header = response.headers()
                    var cookie = response.raw().request().headers().get("Set-Cookie")
                    cookie = response.headers().get("Set-Cookie")
                    if(response.code() == 401){
                        var str = convertStreamToString1(response).replace("\n","").
                            replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str
                        msg.arg1 = 0
                        msg.arg2 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        msg.arg2 = 1
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }

    fun FOSRechargecall(body : String, handler : Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.FOSRechargeapicall("BetaMsales-cookie=2289500426.30755.0000" ,body
        ).enqueue(object : Callback<LoginResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(handler != null){
                    if(response.code() == 401){
                        var str = convertStreamToString1(response).replace("\n","").
                            replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str
                        msg.arg1 = 0
                        msg.arg2 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        msg.arg2 = 1
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }

    //region for update current balance request
    fun UpdatecurrentBalancecall(body : UpdateCurrentBalance, handler: Handler, context: Context) {
        var authtoken = SharedPreference.GetPreference(context,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        val client = ApiClientMain.client.create(ApiInterface::class.java)
        client.UpdateCurrentBalancecall(
            StaticUtility.CONTENT_TYPE, StaticUtility.APP_ID, StaticUtility.APP_SECRET, authtoken, StaticUtility.queryStringUrl(context)
            ,body
        ).enqueue(object : Callback<MainResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<MainResponse>, response: Response<MainResponse>) {
                if (handler != null) {
                    val msg = Message()
                    msg.obj = response.body()
                    msg.arg1 = 0
                    handler.sendMessage(msg)
                }
            }

            override fun onFailure(call: Call<MainResponse>, t: Throwable) {
                //  StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*StaticUtility.sendMail(
                    "Getting error in Dealer login from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }
    //endregion

    fun GetBalancecall(body : String, handler : Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.GetBalanceapicall("BetaMsales-cookie=2289500426.30755.0000" ,body
        ).enqueue(object : Callback<LoginResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(handler != null){
                    if(response.code() == 401){
                        var str = convertStreamToString1(response).replace("\n","").
                            replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str
                        msg.arg1 = 0
                        msg.arg2 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        msg.arg2 = 1
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }

    fun MasterRechargecall(body : String, handler : Handler) {
        val client = ApiClient.client.create(ApiInterface::class.java)
        client.MasterRechargeapicall("BetaMsales-cookie=2289500426.30755.0000" ,body
        ).enqueue(object : Callback<LoginResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(handler != null){
                    if(response.code() == 401){
                        var str = convertStreamToString1(response).replace("\n","").
                            replace("/*-secure-","").replace("*/","")
                        val msg = Message()
                        msg.obj = str
                        msg.arg1 = 0
                        msg.arg2 = 0
                        handler.sendMessage(msg)
                    }else {
                        val msg = Message()
                        msg.obj = response.body()
                        msg.arg1 = 0
                        msg.arg2 = 1
                        handler.sendMessage(msg)
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*sendMail(
                    "Getting error in getUserdata from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }

    //region for convert response to string
    fun convertStreamToString1(response: Response<LoginResponse>): String {
        val reader = BufferedReader(InputStreamReader(response.errorBody()!!.byteStream()))
        val sb = StringBuilder()

        var line = reader.readLine()
        try {
            while (line != null) {
                sb.append(line).append('\n')
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sb.toString()
    }
    //endregion

    //region for convert response to string
    fun convertStreamToString(response: Response<response>): String {
        val reader = BufferedReader(InputStreamReader(response.errorBody()!!.byteStream()))
        val sb = StringBuilder()

        var line = reader.readLine()
        try {
            while (line != null) {
                sb.append(line).append('\n')
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sb.toString()
    }
    //endregion

    //region for convert response to string
    fun convertStreamToStringourserver(response: Response<MainResponse>): String {
        val reader = BufferedReader(InputStreamReader(response.errorBody()!!.byteStream()))
        val sb = StringBuilder()

        var line = reader.readLine()
        try {
            while (line != null) {
                sb.append(line).append('\n')
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sb.toString()
    }
    //endregion

    @SuppressLint("MissingPermission")
    fun getDeviceID(context: Context) : String{
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        var device_Id = ""
        device_Id = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager!!.imei
        }else{
            telephonyManager!!.deviceId
        }
        if(device_Id != null){
            SharedPreference.CreatePreference(context, DATA)
            SharedPreference.SavePreference(DEVICE_ID, device_Id)
            return device_Id
        }
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    //region For make static url...
    fun queryStringUrl1(context: Context): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        try {
            devicename =
                SharedPreference.GetPreference(context, DEVICEINFOPREFERENCE, DeviceName).toString()
            deviceos =
                SharedPreference.GetPreference(context, DEVICEINFOPREFERENCE, DeviceOs).toString()
            app_version =
                SharedPreference.GetPreference(context, DEVICEINFOPREFERENCE, App_Version).toString()
            device_id  = SharedPreference.GetPreference(context, DATA, DEVICE_ID).toString()
            devicename = devicename.replace(" ", "")
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return "?app_type=android&app_version=" + app_version + "&device_name=" + devicename + "&system_version=" +
                deviceos + "&device_id=" + device_id
    }
    //endregion

    //region For make static url...
    fun queryStringUrl(context: Context): HashMap<String, String> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val query = HashMap<String, String>()
        try {
            devicename =
                SharedPreference.GetPreference(context, DEVICEINFOPREFERENCE, DeviceName).toString()
            deviceos =
                SharedPreference.GetPreference(context, DEVICEINFOPREFERENCE, DeviceOs).toString()
            app_version =
                SharedPreference.GetPreference(context, DEVICEINFOPREFERENCE, App_Version).toString()
            device_id = SharedPreference.GetPreference(context, DATA, DEVICE_ID).toString()
            devicename = devicename.replace(" ", "")
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        query.put("app_type","android")
        query.put("app_version",app_version)
        query.put("device_name",devicename)
        query.put("system_version",deviceos)
        query.put("device_id",device_id)
        /*return "?app_type=Android&app_version=" + app_version + "&device_name=" + devicename + "&system_version=" +
                deviceos + "&device_id=" + device_id*/
        return query

    }
    //endregion

    fun getuuid(context: Context) : String {
        return UUID.randomUUID().toString()
    }

    fun log(text : String){
        var path = Environment.getExternalStorageDirectory().absolutePath + "/TataSky"
        var file =  File(path)

        if (!file.exists()) {
            file.mkdirs()
        }
        var filename = getcurrentdate()
        var logfile = File("$path/"+filename+".txt")
        if (!logfile.exists()) {
            try {

                logfile.createNewFile()
            } catch (ioe : IOException) {
                ioe.printStackTrace()
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            var  buf = BufferedWriter(FileWriter(logfile, true))
            buf.append("$text\n -------------------------------------------------------------------\n")
            buf.newLine()
            buf.close()
        } catch (e : FileNotFoundException) {
            e.printStackTrace()
        } catch (e : IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getcurrentdate() : String {
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        return sdf.format(Date())
    }
}
