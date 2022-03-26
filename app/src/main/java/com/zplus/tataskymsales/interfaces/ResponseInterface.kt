package com.zplus.tataskymsales.interfaces

import com.zplus.tataskymsales.databse.table.RechargeRequest

interface ResponseInterface{
    fun OnResponse(response : String, rechreq : RechargeRequest)

    fun OnError(response : String)
}