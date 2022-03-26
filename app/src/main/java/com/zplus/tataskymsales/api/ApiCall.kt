package com.zplus.tataskymsales.api

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import com.example.atul.attendance.model.*
import com.example.atul.retrofit.api.ApiInterface
import com.zplus.tataskymsales.model.zplusresponse.MainResponse
import com.zplus.tataskymsales.utility.SharedPreference
import com.zplus.tataskymsales.utility.StaticUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ApiCall {

    //region for dealer login
    fun Logincall(body: LoginBodyParam, handler: Handler, context: Context) {
        val client = ApiClientMain.client.create(ApiInterface::class.java)
        client.Logincall(
            StaticUtility.CONTENT_TYPE, StaticUtility.APP_ID, StaticUtility.APP_SECRET,StaticUtility.queryStringUrl(context),
            body
        ).enqueue(object : Callback<MainResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<MainResponse>, response: Response<MainResponse>) {
                if (handler != null) {
                    val msg = Message()
                    if(response.code() == 200) {
                        msg.obj = response.body()
                        msg.arg2 = 0
                    }else{
                        msg.obj = StaticUtility.convertStreamToStringourserver(response).replace("\n","")
                        msg.arg2 = 1
                    }
                    msg.arg1 = 0
                    handler.sendMessage(msg)
                }
            }

            override fun onFailure(call: Call<MainResponse>, t: Throwable) {
                t.stackTrace
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

    //region for logout
    fun DoLogoutcall(handler: Handler, context: Context) {
        var authtoken = SharedPreference.GetPreference(context,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        var body = LogOutBodyParam(authtoken)
        val client = ApiClientMain.client.create(ApiInterface::class.java)
        client.DoLogoutcall(
            StaticUtility.CONTENT_TYPE, StaticUtility.APP_ID, StaticUtility.APP_SECRET,StaticUtility.queryStringUrl(context),body
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
                t.stackTrace
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

    //region for get connected sim list
    fun GetConnectedSimListcall(handler: Handler, context: Context) {
        var authtoken = SharedPreference.GetPreference(context,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        val client = ApiClientMain.client.create(ApiInterface::class.java)
        client.GetConnectedSimListcall(
            StaticUtility.CONTENT_TYPE, StaticUtility.APP_ID, StaticUtility.APP_SECRET, authtoken,StaticUtility.queryStringUrl(context)
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
                t.stackTrace
                //StaticUtility.showMessage(mContext, t.toString())
                //Creating SendMail object
                /*StaticUtility.sendMail(
                    "Getting error in Dealer login from server in MainActivity.\n",
                    t.toString()
                )*/
            }
        })
    }
    //endregion

    //region for update sim status detail
    fun UpdateSimStatuscall(body : UpdateSimStatusBodyParam, handler: Handler, context: Context) {
        var authtoken = SharedPreference.GetPreference(context,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        val client = ApiClientMain.client.create(ApiInterface::class.java)
        client.UpdateSimStatuscall(
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

    //region for get recharge request
    fun GetRechargeRequestcall(body : RechargeRequestBodyParam, handler: Handler, context: Context) {
        var authtoken = SharedPreference.GetPreference(context,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        val client = ApiClientMain.client.create(ApiInterface::class.java)
        client.GetRechargeRequestcall(
            StaticUtility.CONTENT_TYPE, StaticUtility.APP_ID, StaticUtility.APP_SECRET, authtoken, StaticUtility.queryStringUrl(context)
            ,body
        ).enqueue(object : Callback<MainResponse> {
            @SuppressLint("ShowToast")
            override fun onResponse(call: Call<MainResponse>, response: Response<MainResponse>) {
                val msg = Message()
                if(response.code() == 200) {
                    msg.obj = response.body()
                    msg.arg2 = 0
                }else{
                    msg.obj = StaticUtility.convertStreamToStringourserver(response).replace("\n","")
                    msg.arg2 = 1
                }
                msg.arg1 = 0
                handler.sendMessage(msg)
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

    //region for update recharge status request
    fun UpdateRechargeRequestStatuscall(body : UpdateRechargeStatus, handler: Handler, context: Context) {
        var authtoken = SharedPreference.GetPreference(context,StaticUtility.LOGINPREFERENCE, StaticUtility.AUTHTOKEN).toString()
        val client = ApiClientMain.client.create(ApiInterface::class.java)
        client.UpdateRechargeRequestStatuscall(
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
                t.stackTrace
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
}