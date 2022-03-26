package com.zplus.tataskymsales.databse.table

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SimList(
    @PrimaryKey open var _ID: Int = 0,
    open var lapu_no: String = "",
    open var lapu_name: String = "",
    open var sim_no: String = "",
    open var pin_no: String = "",
    open var sim_type: String = "",
    open var recharge_type_name: String = "",
    open var recharge_type_code: String = "",
    open var hash_id: String = "",
    open var has_credentials: String = "",
    open var user_type: String = "",
    open var instanceid: String = "",
    open var token: String = "",
    open var cookie: String = "",
    open var uuid: String = "",
    open var id: String = "",
    open var enckey: String = "",
    open var ivspec: String = "",
    open var role: String = "",
    open var recharge_token_id: String = "",
    open var status: String = "",
    open var circle: String = ""
)
    : RealmObject()