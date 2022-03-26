package com.zplus.tataskymsales.model.tataresponse


data class response(val responseID : String, val isSuccessful : String, val result : Result)

data class Result(val error : String, val status : String, val payload : payload)

data class payload(val id : String, val enckey : String, val ivKey : String)

data class LoginResponse(val responseID : String, val isSuccessful : String, val result : LoginResult)

data class LoginResult(val errorMessage : String, val status : String, val payload : String)