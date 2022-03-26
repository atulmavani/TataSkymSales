package com.example.atul.retrofit.api

import com.example.atul.attendance.model.*
import com.zplus.tataskymsales.model.zplusresponse.MainResponse
import com.zplus.tataskymsales.model.tataresponse.LoginResponse
import com.zplus.tataskymsales.model.tataresponse.response
import com.zplus.tataskymsales.utility.StaticUtility
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @POST(StaticUtility.LOGINMAIN)
    fun Logincall(
        @Header("Content-Type") Content_Type: String,
        @Header("App-Id") app_id: String,
        @Header("App-Secret") app_secret: String,
        @QueryMap(encoded = true) query: Map<String, String>,
        @Body body: LoginBodyParam
    ): Call<MainResponse>

    @POST(StaticUtility.LOGOUT)
    fun DoLogoutcall(
        @Header("Content-Type") Content_Type: String,
        @Header("App-Id") app_id: String,
        @Header("App-Secret") app_secret: String,
        @QueryMap(encoded = true) query: Map<String, String>,
        @Body body: LogOutBodyParam
    ): Call<MainResponse>

    @POST(StaticUtility.UPDATEBALANCE)
    fun UpdateCurrentBalancecall(
        @Header("Content-Type") Content_Type: String,
        @Header("App-Id") app_id: String,
        @Header("App-Secret") app_secret: String,
        @Header("Auth-Token") auth_token: String,
        @QueryMap(encoded = true) query: Map<String, String>,
        @Body body : UpdateCurrentBalance
    ): Call<MainResponse>

    @POST(StaticUtility.UPDATESIMSTATUS)
    fun UpdateSimStatuscall(
        @Header("Content-Type") Content_Type: String,
        @Header("App-Id") app_id: String,
        @Header("App-Secret") app_secret: String,
        @Header("Auth-Token") auth_token: String,
        @QueryMap(encoded = true) query: Map<String, String>,
        @Body bdy : UpdateSimStatusBodyParam
    ): Call<MainResponse>

    @POST(StaticUtility.GETCONECTEDSIMLIST)
    fun GetConnectedSimListcall(
        @Header("Content-Type") Content_Type: String,
        @Header("App-Id") app_id: String,
        @Header("App-Secret") app_secret: String,
        @Header("Auth-Token") auth_token: String,
        @QueryMap(encoded = true) query: Map<String, String>
    ): Call<MainResponse>

    @POST(StaticUtility.GETRECHARGEREQUEST)
    fun GetRechargeRequestcall(
        @Header("Content-Type") Content_Type: String,
        @Header("App-Id") app_id: String,
        @Header("App-Secret") app_secret: String,
        @Header("Auth-Token") auth_token: String,
        @QueryMap(encoded = true) query: Map<String, String>,
        @Body body : RechargeRequestBodyParam
    ): Call<MainResponse>

    @POST(StaticUtility.UPDATERECHARGEREQUEST)
    fun UpdateRechargeRequestStatuscall(
        @Header("Content-Type") Content_Type: String,
        @Header("App-Id") app_id: String,
        @Header("App-Secret") app_secret: String,
        @Header("Auth-Token") auth_token: String,
        @QueryMap(encoded = true) query: Map<String, String>,
        @Body body : UpdateRechargeStatus
    ): Call<MainResponse>

    @POST("query")
    @FormUrlEncoded
    fun apicall(@Header("x-wl-platform-version") x_wl_platform_version : String,
                @Header("x-wl-native-version") x_wl_native_version : String,
                @Header("x-wl-app-version") x_wl_app_version : String,
                @Header("x-wl-device-id") x_wl_device_id : String,
                @FieldMap body : Map<String, String>): Call<response>

    @GET("HttpEncGetKey/getkey")
    fun GetEncKeyapicall(@Header("Cookie") cookie : String,
                         @Query("params") param : String): Call<response>

    @GET("HttpEVDAdapter/doEVDLogin")
    fun Loginapicall(@Header("Cookie") cookie : String,
                     @Query("params") param : String): Call<LoginResponse>

    @GET("HttpEVDServiceAdapter/retrieveTransactionsDetails")
    fun Last10Transactionapicall(@Header("Cookie") cookie : String,
                     @Query("params") param : String): Call<LoginResponse>

    @GET("HttpEVDAdapter/doRecharge")
    fun RetailerRechargeapicall(@Header("Cookie") cookie : String,
                                @Query("params") param : String): Call<LoginResponse>

    @GET("HttpEVDAdapter/doEVDTransfer")
    fun FOSRechargeapicall(@Header("Cookie") cookie : String,
                           @Query("params") param : String): Call<LoginResponse>

    @GET("HttpEVDAdapter/doEVDTransfer")
    fun MasterRechargeapicall(@Header("Cookie") cookie : String,
                           @Query("params") param : String): Call<LoginResponse>

    @GET("HttpEVDAdapter/doBalanceEnquiry")
    fun GetBalanceapicall(@Header("Cookie") cookie : String,
                           @Query("params") param : String): Call<LoginResponse>
}