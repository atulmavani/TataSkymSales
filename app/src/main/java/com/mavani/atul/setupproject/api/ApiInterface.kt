package com.example.atul.retrofit.api

import com.example.atul.attendance.model.BodyParam
import com.example.atul.attendance.model.response
import com.mavani.atul.setupproject.utility.StaticUtility
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {

    @POST(StaticUtility.LOGIN)
    fun apicall(@Header("Content-Type") Content_Type : String,
                @Header("app-id") App_Id : String,
                @Header("app-secret") app_secret : String,
                @Body body : BodyParam): Call<response>
}