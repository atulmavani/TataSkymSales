package com.zplus.tataskymsales.databse.model

import com.zplus.tataskymsales.databse.table.LogTable
import io.realm.Realm
import io.realm.RealmResults

class LogModel {
    fun addLog(realm: Realm, log: LogTable): Boolean {
        return try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(log)
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    fun getlog(realm: Realm): RealmResults<LogTable> {
        return realm.where(LogTable::class.java).findAll()
    }

    fun getLastid(realm: Realm): LogTable {
        return realm.where(LogTable::class.java).findAll().last()
    }
}