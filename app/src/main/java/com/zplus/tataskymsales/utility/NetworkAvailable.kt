package com.zplus.tataskymsales.utility

import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import java.net.InetAddress

class NetworkAvailable(internethandler : Handler) : AsyncTask<Void, Void, Boolean>() {

    val handler  = internethandler

    override fun doInBackground(vararg params: Void?): Boolean? {
        try {
            val ipAddr = InetAddress.getByName("google.com")
            //You can replace it with your name
            return !ipAddr.equals("")

        } catch (e: Exception) {
            return false
        }
    }
    override fun onPostExecute(isrunning: Boolean?) {
        super.onPostExecute(isrunning)

        if (handler != null) {
            val msg = Message()
            msg.obj = isrunning
            msg.arg1 = 1
            handler.sendMessage(msg)
        }
    }
}