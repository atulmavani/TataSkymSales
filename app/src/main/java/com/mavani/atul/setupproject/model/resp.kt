package com.example.atul.attendance.model


data class response(val code : String, val message : String, val status : String, val payload : payload)

data class payload(val user_id : String, val firstname : String, val username : String, val email : String, val register_from : String,
                   val profilepicture : ArrayList<String>, val authUser : authUser)

data class authUser(val user_token : String, val token_expired_on : String)

