package com.mavani.atul.setupproject.activity

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import com.example.atul.attendance.model.BodyParam
import com.example.atul.attendance.model.response
import com.example.atul.retrofit.api.ApiClient
import com.example.atul.retrofit.api.ApiInterface
import com.mavani.atul.setupproject.R
import com.mavani.atul.setupproject.utility.NetworkAvailable
import com.mavani.atul.setupproject.utility.StaticUtility
import org.json.JSONException

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    val mContext = this@MainActivity

    lateinit var internetHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ApiCall()
        val isinternet = NetworkAvailable(internetHandler)
        isinternet.execute()
    }

    fun ApiCall(){
        internetHandler = android.os.Handler(Handler.Callback { msg ->
                if(msg.obj as Boolean) {
                    val body = BodyParam("test@gmail.com", "123456", "dgfdgfhyng", "")
                    val client = ApiClient.client.create(ApiInterface::class.java)

                    client.apicall(
                        StaticUtility.CONTENT_TYPE, StaticUtility.APP_ID, StaticUtility.APP_SECRET, body
                    ).enqueue(object : Callback<response> {
                        @SuppressLint("ShowToast")
                        override fun onResponse(call: Call<response>, response: Response<response>) {
                            if (response.code() == 200) {
                                val code = response.body()?.code
                                val status = response.body()?.status
                                val message = response.body()?.message
                                val user_id = response.body()?.payload?.user_id
                                val picture = response.body()?.payload?.profilepicture?.get(0)
                                val user_token = response.body()?.payload?.authUser?.user_token
                            } else {
                                StaticUtility.sendMail(
                                    "Getting error in GetUserData to server in MainActivity.\n",
                                    response.toString()
                                )
                            }
                        }

                        override fun onFailure(call: Call<response>, t: Throwable) {
                            StaticUtility.showMessage(mContext, t.toString())
                            //Creating SendMail object
                            StaticUtility.sendMail(
                                "Getting error in getUserdata from server in MainActivity.\n",
                                t.toString()
                            )
                        }
                    })
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            true
        })
    }
}
