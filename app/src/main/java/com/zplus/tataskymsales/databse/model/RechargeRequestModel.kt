package com.zplus.tataskymsales.databse.model

import com.zplus.tataskymsales.databse.table.RechargeRequest
import io.realm.Realm
import io.realm.RealmResults

class RechargeRequestModel {
    fun addrequest(realm: Realm, sim: RechargeRequest): Boolean {
        return try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(sim)
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    fun getRequest(realm: Realm): RealmResults<RechargeRequest> {
        return realm.where(RechargeRequest::class.java).findAll()
    }

    fun getLastRequest(realm: Realm): RechargeRequest {
        return realm.where(RechargeRequest::class.java).findAll().last()
    }

    fun delrequest(realm: Realm, rech_txn_code: String): Boolean {
        return try {
            realm.beginTransaction()
            realm.where(RechargeRequest :: class.java).equalTo("recharge_txn_code", rech_txn_code).findFirst().deleteFromRealm()
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }
}