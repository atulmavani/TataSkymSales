package com.zplus.tataskymsales.databse.model

import com.zplus.tataskymsales.databse.table.SimList
import io.realm.Realm
import io.realm.RealmResults

class SimListModel {
    fun addSim(realm: Realm, sim: SimList): Boolean {
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

    fun getSimList(realm: Realm): RealmResults<SimList> {
        return realm.where(SimList::class.java).findAll()
    }

    fun getLastSim(realm: Realm): SimList {
        return realm.where(SimList::class.java).findAll().last()
    }

    fun getsimfromlapuno(realm: Realm, lapuno: String): SimList {
        return realm.where(SimList :: class.java).equalTo("lapu_no", lapuno).findFirst()
    }

    fun delsim(realm: Realm, _ID: Int): Boolean {
        return try {
            realm.beginTransaction()
            realm.where(SimList :: class.java).equalTo("_ID", _ID).findFirst().deleteFromRealm()
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }
}