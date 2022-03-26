package com.zplus.tataskymsales.interfaces

import com.zplus.tataskymsales.model.zplusresponse.RechargeRequestResponse


interface GetRechargeRequestInterface {
    fun OnGet(rechreq : RechargeRequestResponse)
}