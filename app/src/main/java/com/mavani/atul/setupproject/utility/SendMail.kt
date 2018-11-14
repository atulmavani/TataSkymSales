package com.mavani.atul.setupproject.utility

import android.content.Context
import android.os.AsyncTask

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.Properties

class SendMail (
    //Declaring Variables
    //Information to send email
    private val email: String,
    private val subject: String,
    private val message: String,
    private val fromemail: String,
    private val password: String
)
    //Initializing variables
    : AsyncTask<Void, Void, Void>() {
    private var session: Session? = null

    override fun doInBackground(vararg params: Void): Void? {
        //Creating properties
        val props = Properties()

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"

        //Creating a new session
        session = Session.getDefaultInstance(props,
            object : javax.mail.Authenticator() {
                //Authenticating the password
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(fromemail, password)
                }
            })
        try {
            //Creating MimeMessage object
            val mm = MimeMessage(session)

            //Setting sender address
            mm.setFrom(InternetAddress(fromemail))
            //Adding receiver
            mm.addRecipient(Message.RecipientType.TO, InternetAddress(email))
            //Adding subject
            mm.subject = subject
            //Adding message
            mm.setText(message)

            //Sending email
            Transport.send(mm)

        } catch (e: MessagingException) {
            e.printStackTrace()
        }

        return null
    }
}