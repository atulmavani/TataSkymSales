package com.zplus.tataskymsales.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.view.View
import com.example.atul.attendance.model.LoginBodyParam
import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.api.ApiCall
import com.zplus.tataskymsales.model.zplusresponse.MainResponse
import com.zplus.tataskymsales.utility.NetworkAvailable
import com.zplus.tataskymsales.utility.SharedPreference
import com.zplus.tataskymsales.utility.StaticUtility
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import kotlin.system.exitProcess
import android.support.v4.content.ContextCompat
import com.zplus.tataskymsales.Security.MCrypt
import org.json.JSONArray

class LoginActivity : AppCompatActivity() {
    lateinit var loginHandler: Handler
    var mContext = this@LoginActivity
    var firmid = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        DeviceInfo(mContext)
        val MyVersion = Build.VERSION.SDK_INT
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                ActivityCompat.requestPermissions(mContext,
                    arrayOf( Manifest.permission.READ_PHONE_STATE),
                    1)
            }else{
                if(SharedPreference.GetPreference(mContext, StaticUtility.DATA, StaticUtility.DEVICE_ID) == null){
                    //StaticUtility.getDeviceID(mContext)
                }
            }
        }else{
            if(SharedPreference.GetPreference(mContext, StaticUtility.DATA, StaticUtility.DEVICE_ID) == null){
                StaticUtility.getDeviceID(mContext)
            }
        }

        if(SharedPreference.GetPreference(mContext, StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN) != null){
            startActivity(Intent(mContext, DashBoardActivity::class.java))
            finish()
        }
        btn_login.setOnClickListener{
           /* //var str = "5qU3x0BAmNUX3UTEnjeMXuF0m1g54ICCRLIG9pZyVNJ5IL6WyH6PIJpuFo8y7rKguiyNSRUf+GGX2gGkLwmVDw=="

            var obj = JSONArray()
            obj.put("9178497060")
            obj.put("9178497060")
            obj.put("MOBWEB")
            obj.put("")
            obj.put("parentBalance")
            var encstr = mycript.encrypt(obj.toString())
            var param = JSONArray()
            param.put(encstr + ":" + "c4470c54-8d37-4738-be30-9b44870d8469")
            param.put("null")
            param.put("null")
            param.put("null")
            param.put("null")*/
            //["1376005839","HD","","","","9727293009","200071338241","","","","6436357","2000121321219014","","","","1-8VXSLJW","N","EVD",0]
            //dJPUFSEzJ65Rmvm9
            //var str = "rFnfCjE2sURQNK1geZq/4OY0L/3k4lvj6GyVSye40DF9X61BkM+Nl50WoeS/q52ydslqZ/z25OL1ZbZrfL+tJWx4xx9/rr4jDlwf7clWrlvQ2FtYJGC+pcoPYHAHkiNU+bkyOUhagEnK4vBntK6+NUUg77HtGbBaw4kLzq4BzfVHiQFp/1TAkj/su0nssrUK+tYACs6OA/DUn12ZY7UOlulfkvzjhdfx0HJeoc55NPlU+4SJYD5o8/ui28nQdig06x3zmyW+8YGR2teVNITl4wOW8riBgnedrl6KjKiyXa+qbjKEivujjnL4ys8npe7m"
            //var obj = JSONArray()
            //{"TataskyPacks":"Curated Packs~Add-on packs~Regional packs"}
            //obj.put("Add-on packs")
            //obj.put("HD")
            //var mycript = MCrypt("F7zumglDvZfBkMhi", "1234567890abcdef")
            //var decstr = mycript.decrypt(str)
            //StaticUtility.showMessage(mContext, decstr)
            if(edt_user_name.text.toString().isNotEmpty()) {
                if(edt_password.text.toString().isNotEmpty()) {
                    if(edt_firm_id.text.toString().isNotEmpty()) {
                        firmid = edt_firm_id.text.toString()
                        Login()
                        NetworkAvailable(loginHandler).execute()
                    }else{
                        StaticUtility.showMessage(mContext, "Enter Company id...!")
                    }
                }else{
                    StaticUtility.showMessage(mContext, "Enter Password...!")
                }
            }else{
                StaticUtility.showMessage(mContext, "Enter Username...!")
            }
        }
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    //region for login
    fun Login(){
        login_loader.visibility = View.VISIBLE
        loginHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    val body = LoginBodyParam(edt_user_name.text.toString(), edt_password.text.toString(), edt_firm_id.text.toString())
                    ApiCall.Logincall(body, loginHandler, mContext)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                login_loader.visibility = View.GONE
                if(msg.arg2 == 1){
                    var obj = JSONObject(msg.obj.toString())
                    StaticUtility.showMessage(mContext, obj.optString("message"))
                }else {
                    var respo = msg.obj as MainResponse
                    StaticUtility.showMessage(mContext, respo.message)
                    if (respo.code == "200") {
                        SharedPreference.CreatePreference(mContext, StaticUtility.LOGINPREFERENCE)
                        SharedPreference.SavePreference(StaticUtility.AUTHTOKEN, respo.payload!!.authUser.user_token)
                        SharedPreference.SavePreference(StaticUtility.LOGOURL, respo.payload!!.logo_url)
                        SharedPreference.SavePreference(StaticUtility.FIRMNAME, respo.payload!!.firm_name)
                        SharedPreference.SavePreference(StaticUtility.HASH_ID, respo.payload!!.hash_id)
                        SharedPreference.SavePreference(StaticUtility.FIRM_ID, firmid)
                        SharedPreference.SavePreference(StaticUtility.APPID, respo.payload!!.app_id)
                        SharedPreference.SavePreference(StaticUtility.APPSECRET, respo.payload!!.app_secret)
                        startActivity(Intent(mContext, DashBoardActivity::class.java))
                        finish()
                    }
                }
            }
            true
        })
    }
    //endregion

    //region DeviceInfo
    fun DeviceInfo(mContext: Context) {
        val model = Build.MODEL
        val os = Build.VERSION.RELEASE
        val manager = mContext.packageManager
        var info: PackageInfo? = null
        try {
            info = manager.getPackageInfo(
                mContext.packageName, 0
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ///Creating SendMail object
            StaticUtility.sendMail(
                "Getting error in Get device information to server in MainActivity.\n",
                e.toString()
            )
        }

        assert(info != null)
        val version = info!!.versionName
        SharedPreference.CreatePreference(mContext, StaticUtility.DEVICEINFOPREFERENCE)
        SharedPreference.SavePreference(StaticUtility.DeviceName, model)
        SharedPreference.SavePreference(StaticUtility.DeviceOs, os)
        SharedPreference.SavePreference(StaticUtility.App_Version, version)
    }//endregion

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(SharedPreference.GetPreference(mContext, StaticUtility.DATA, StaticUtility.DEVICE_ID) == null){
                        StaticUtility.getDeviceID(mContext)
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    exitProcess(0)
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }
}
