package com.mavani.atul.setupproject.notification

import com.mavani.atul.setupproject.utility.StaticUtility
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService


class FirebaseInstanceIDService : FirebaseInstanceIdService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val refreshedToken = FirebaseInstanceId.getInstance().getToken()
        Log.d(TAG, "Refreshed token: $refreshedToken")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.


        sharedPreferences.edit().putBoolean(StaticUtility.SENT_TOKEN_TO_SERVER, true).apply()
        sharedPreferences.edit().putString(StaticUtility.FCM_TOKEN, refreshedToken).apply()
    }
    companion object {
        private val TOPICS = arrayOf("global")
        private val TAG = "MyFirebaseIIDService"
    }
    // [END subscribe_topics]
}