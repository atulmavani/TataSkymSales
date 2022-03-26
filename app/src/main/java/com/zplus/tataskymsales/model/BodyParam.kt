package com.example.atul.attendance.model

import org.json.JSONArray

data class BodyParam (var email : String, var password : String, var device_id : String, var currencyCode : String)

data class LoginBodyParam(var username : String, var password : String, var firm_id : String)

data class LogOutBodyParam(var user_token : String)

data class RechargeRequestBodyParam(var hash_id : String)

data class UpdateSimStatusBodyParam(var hash_id : String, var status : String)


data class UpdateRechargeStatus(var recharge_id : String, var txn_id : String, var amount : String,
                                var current_balance : String, var status : String, var msg : String)

data class UpdateCurrentBalance(var hash_id : String, var amount : String)