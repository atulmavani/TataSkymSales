package com.mavani.atul.setupproject.notification

import android.app.NotificationChannel
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v4.content.LocalBroadcastManager
import com.mavani.atul.setupproject.utility.StaticUtility
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.text.Html
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage
import com.mavani.atul.setupproject.R
import com.mavani.atul.setupproject.activity.MainActivity
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    internal var bitmap: Bitmap? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.e(TAG, "From: " + remoteMessage!!.getFrom())

        if (remoteMessage == null)
            return

        // Check if message contains a notification payload.
        if (remoteMessage!!.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage!!.getNotification()!!.getBody())
        }

        // Check if message contains a data payload.
        if (remoteMessage!!.getData().size > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage!!.getData().toString())
            try {
                /*  JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);*/
                val imageUri = remoteMessage!!.getData().get("image")
                val message = remoteMessage!!.getNotification()!!.getBody()
                //To get a Bitmap image from the URL received
                bitmap = getBitmapfromUrl(imageUri!!)
                if (message != null) {
                    sendNotification(message, bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
            }

        } else {
            /*handleNotification(remoteMessage.getNotification().getBody());*/
            showSmallNotification(remoteMessage.getNotification()!!.getBody()!!)
        }
    }

    @SuppressLint("WrongConstant")
    private fun sendNotification(messageBody: String, image: Bitmap?) {
        val pushNotification = Intent(StaticUtility.PUSH_NOTIFICATION)
        pushNotification.putExtra("message", messageBody)
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

        val intent = Intent(this, MainActivity::class.java)
        val notificationID = Random().nextInt(9999 - 1000) + 1000
        val requestCode = Random().nextInt(50) + 1
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val channelId = getString(R.string.default_notification_channel_id)

        val pendingIntent = PendingIntent.getActivity(
            this, requestCode /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        var builder: NotificationCompat.Builder? = null
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.setBigContentTitle(getResources().getString(R.string.app_name))
        bigPictureStyle.setSummaryText(Html.fromHtml(messageBody).toString())
        bigPictureStyle.bigPicture(bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel: NotificationChannel? = notificationManager!!.getNotificationChannel(channelId)
            if (mChannel == null) {
                mChannel = NotificationChannel(
                    channelId, getApplicationContext().getString(R.string.app_name),
                    importance
                )
                mChannel.enableVibration(true)
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                notificationManager.createNotificationChannel(mChannel)
            }
            builder = NotificationCompat.Builder(getApplicationContext(), channelId)
            builder!!.setContentTitle(messageBody)  // required
                .setSmallIcon(R.mipmap.ic_launcher) // required
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        getResources(),
                        R.mipmap.ic_launcher
                    )
                )
                .setContentText(getApplicationContext().getString(R.string.app_name))  // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setStyle(bigPictureStyle)
                .setContentIntent(pendingIntent)
                .setTicker(messageBody)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
        } else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder = NotificationCompat.Builder(getApplicationContext(), channelId)
            builder!!.setStyle(bigPictureStyle)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        getResources(),
                        R.mipmap.ic_launcher
                    )
                )
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 0, 1000, 0, 1000))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        }

        val notification = builder!!.build()
        notificationManager!!.notify(notificationID, notification)

        /* if (image != null) {
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image));*//*Notification with Image*//*
        }*/
    }



    @SuppressLint("WrongConstant")
    private fun showSmallNotification(message: String) {

        val intent = Intent(this, MainActivity::class.java)
        val notificationID = Random().nextInt(9999 - 1000) + 1000
        val requestCode = Random().nextInt(50) + 1
        val builder: NotificationCompat.Builder
        val channalid = getString(R.string.default_notification_channel_id)

        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel: NotificationChannel? =
                notifManager!!.getNotificationChannel(getString(R.string.default_notification_channel_id))
            if (mChannel == null) {
                mChannel = NotificationChannel(
                    getString(R.string.default_notification_channel_id),
                    getString(R.string.app_name), importance
                )
                mChannel.enableVibration(true)
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                notifManager.createNotificationChannel(mChannel)
            }
            builder = NotificationCompat.Builder(this, channalid)
            builder.setContentTitle(message)  // required
                .setSmallIcon(R.mipmap.ic_launcher) // required
                .setContentText(getString(R.string.app_name))  // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(message)
                .setChannelId(channalid)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
        } else {
            builder = NotificationCompat.Builder(this, channalid)
            builder.setContentTitle(message)                           // required
                .setSmallIcon(R.mipmap.ic_launcher) // required
                .setContentText(getString(R.string.app_name))  // required
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        getResources(),
                        R.mipmap.ic_launcher
                    )
                )
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                .setPriority(Notification.PRIORITY_HIGH)
        }
        notifManager!!.notify(notificationID, builder.build())
    }



    fun getBitmapfromUrl(imageUrl: String): Bitmap? {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream()
            return BitmapFactory.decodeStream(input)

        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return null

        }

    }

    companion object {

        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

}
