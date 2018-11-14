package com.mavani.atul.setupproject.utility

import android.content.Context
import android.widget.Toast


object StaticUtility {

    val APP_ID = "d6b2b4c0c749b307b658765d2bb89fc3"
    val APP_SECRET = "d6b2b4c0c749b307b658765d2bb89fc3"
    val CONTENT_TYPE = "application/json"


    //val URL = "http://192.168.0.107/crm/attendence_api/"
    val URL = "http://bazarbit.com/bazzarbit/"

    //region For send mail to developer for any exception in app
    val EMAIL = "tt957621@gmail.com"
    val PASSWORD = "tt957621@gmail.com1"
    val TOEMAIL = "atul@parghiinfotech.com"
    val SUBJECT = "Initial app exception Report"
    //endregion

    //region For SharedPreferences
    val P_USER_DATA = "user_data"
    //endregion

    //region For api list
    const val LOGIN = "app/frontend/login"
    //endregion

    //region For Fcm
    val SENT_TOKEN_TO_SERVER = "sentTokenToServer"
    val FCM_TOKEN = "fcm_token"
    val PUSH_NOTIFICATION = "pushNotification"
    //endregion

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
}
