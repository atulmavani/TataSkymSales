package com.zplus.tataskymsales.api

import com.zplus.tataskymsales.utility.StaticUtility
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClientMain {

    companion object {

        var retofit: Retrofit? = null

        val client: Retrofit
            get() {
                if (retofit == null) {
                    retofit = Retrofit.Builder()
                        .baseUrl(StaticUtility.URLMAIN)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
                return retofit!!
            }
    }
}