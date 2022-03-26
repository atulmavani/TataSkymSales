package com.zplus.tataskymsales.databse.migration

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration

class RealmMigrations : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema

        if (oldVersion < newVersion) {
            /*val userSchema = schema.get("Student")?.apply {
                addField("std", String::class.java, FieldAttribute.REQUIRED)
            }*/
        }
    }

    override fun hashCode(): Int {
        return RealmMigration::class.java.hashCode()
    }


    override fun equals(`object`: Any?): Boolean {
        return if (`object` == null) {
            false
        } else `object` is RealmMigrations
    }
}