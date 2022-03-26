package com.zplus.tataskymsales.databse.table

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

open class LogTable (
    @PrimaryKey
    open var _ID : Int = 0,
    open var tr_id : String = "",
    open var response : String = "",
    open var date_time : String = ""
    ) : RealmObject()