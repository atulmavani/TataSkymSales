package com.zplus.tataskymsales.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.zplus.tataskymsales.R
import com.zplus.tataskymsales.Security.MCrypt
import com.zplus.tataskymsales.model.tataresponse.LoginResponse
import com.zplus.tataskymsales.model.tataresponse.response
import com.zplus.tataskymsales.utility.NetworkAvailable
import com.zplus.tataskymsales.utility.SharedPreference
import com.zplus.tataskymsales.utility.StaticUtility
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    val mContext = this@MainActivity

    lateinit var internetHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //below all are for new api
        //master
        //for login api param
        //var mycript = MCrypt("YDI33rCxQcDDwPvB","1234567890abcdef")
        //var cipherText = mycript.decrypt("LYmHjG50RaLCuNSA3CgezgTkD6jctCRpaIRB8mUkOxOJm95tPF2179mmbBIr/vEd")
        //["7077668877","zp2020@2021","MOBWEB"]

        //for login api response
        //var mycript = MCrypt("YDI33rCxQcDDwPvB","1234567890abcdef")
        //var cipherText = mycript.decrypt("w3IHg79OWg6wFBK0Jx0QMzrOKp+tFkiNnIRnkB9uPmMFZn7lmAP4+0HFT4NMo7OEV/FlkdedlfyVracRWorxj9CmkEMnusUyetuD8ftK9t6ngl9k7eb9MThjE5OoU4fjnA7y67aAv6s1t1+wBaaX3N316ZcxlZwn+0uBqck1sJZIrFHPvu1so05XDRA/p4FqhHqSiw+tbbxvkDee+dPRgcjpSDkYH6P2j6bj+73BAjN17+iaGv1OEho1dsEhNXsQwL0/QURXMayKC/DkrAhu0ttkTnEYc8eG64WSxmzww2maVN4M3Qia8VSR8QLx9NdkRY3PFUXK8Rr5PDkHLW7I35K3WeVvYT2s+dXYz/sPFsbeNM/Z8Db9TK7Z+UHco7svFZfSsVUEL7p3K6es/zKB6JrnmVHbx+jkbXmC1QVsSlYRODhq1RrM8voPbLbeeWj3Y4AqwFtUE1UYwpDnkVTy6slGX0T4EDS6WsqNg4wYF9QhKmmL2DGtkvRyjU+reoackCZWHmirxB0iPGcyytiIto2vxxm5MVEjI1ygFpCGxV6B0tBhLwKO3aBQLhppRdzTFsso3fKH7a6Dnbq1uOHZkQieK6dvYkZoBcqLAiYAQyR32rPr0j+Qi5SpfPO4oeAMbfkpYMSpd7vzdrbsAHJcW2PttInHbGoErjTvk75E7yULSHanfVkymUZpCjGxXCORz3mnPU3UG+ZJe3/SWOsuoc54GiSrUY3xFkqUAB50LTIFCZ98r2ULxBL2l9PUkMviMdQ+9HfT08MNY01hbQ2ojVUOTz4KXEWEXa7MgzXoYgIk3M7JK4mXFF2kFuQbtHvG")
        //{"MobileNumber":"7077668877","role":"Distributor","timestamp":"30/10/2019 13:42:47","message":"Login authentication Success.","recharge_token_id":"9f322ab8-8022-42cb-9440-a9824e8d4e60","iconToDisable":"packageAdd~accountSetup~rePushOrder~packageAddTrai","appSignature":"0qcQdwjN32IsGIqFqpuiJkor//E","ForASIASM":[{"ASI_NAME":"Sandeep Bhadoria","OU_NUM":"12071","ASI_PH_NUM":"9727754623","ASM_PH_NUM":"9687600022","ASM_NAME":"Pradeep Maurya"}],"imgPath":["https://www.tatasky.com/msales/SlotSetB/Slot1.jpg~ ","https://www.tatasky.com/msales/SlotSetB/Slot2.jpg~ ","https://www.tatasky.com/msales/SlotSetB/Slot3.jpg~"]}

        //for evd transfer validate mobile number api param
        //var mycript = MCrypt("Jxk6q2EXatbtpau4","1234567890abcdef")
        //var cipherText = mycript.decrypt("RJnMrj26IPK1cJZ2Ed9GqoMOZallnJTiwShXkhQ3YE1SYmJovttIzAhh9eqFYYKYrZjfEgSOnZIujIk3POAH7w")
        //["7077668877","9727293009","MOBWEB","","validateName"]

        //for evd transfer validate mobile number api response
        //var mycript1 = MCrypt("Jxk6q2EXatbtpau4","1234567890abcdef")
        //var cipherText1 = mycript1.decrypt("ji981uAywe9yTfsx7BEsCXVEjqSgW32iVOR20bm4uwUiuj3yPPVotFq+5xWpxeG4Gjy4fDAvMA5joZAm/0ljUbySrbz8RzU9yOZJtmkHMBRtK+DXNrZ3Nmip0LscesRA/msoPtNW5IDf5psKfd8ZjpWS5QO123jrw8u/5ZhJNl6I0vSvH/QdtzkdBYrVyORZvY1AKaxfhKRZ0F0T09Iu2D6wqK+gSZhk3Yc2TU2J+dynhWyS8/N3U1akY6VOW4IB")
        //{"currentBalance":"0.0","donarRMN":"7077668877","recipientRMN":"9727293009","childName":"ATUL","type":"validateName","errorCode":"600","errorMessage":"Balance Inquiry Successful."}

        //for evd transfer confirm api param
        //var mycript = MCrypt("Jxk6q2EXatbtpau4","1234567890abcdef")
        //var cipherText = mycript.decrypt("RJnMrj26IPK1cJZ2Ed9Gqr5FsFRRTKBGEHwA2vJ+ywr2Sq0P8vIO1uZViufchL0Q")
        //["7077668877","9727293009","1","MOBWEB"] // here 1 is amount which is you want to recharge

        //get balance
        //var mycript = MCrypt("U0Glcv11im3kcDIL","1234567890abcdef")
        //var cipherText = mycript.decrypt("v5eBKaL5d7IGmc/2Y5vllJdUgIzDDhXUpWO1t1i1gqcVhOScdia3yLgJtjv6l8rzhUQIBG915OJhofFkyqkGtg")
        //["7077668877","7077668877","MOBWEB","","parentBalance"]


        //retailer
        //for login api param
        //["9178497060","zplus@2021","MOBWEB"]

        //for login api response
        //{"MobileNumber":"9178497060","role":"Dealer","timestamp":"30/10/2019 13:42:47","message":"Login authentication Success.","recharge_token_id":"9f322ab8-8022-42cb-9440-a9824e8d4e60","iconToDisable":"packageAdd~accountSetup~rePushOrder~packageAddTrai","appSignature":"0qcQdwjN32IsGIqFqpuiJkor//E","ForASIASM":[{"ASI_NAME":"Sandeep Bhadoria","OU_NUM":"12071","ASI_PH_NUM":"9727754623","ASM_PH_NUM":"9687600022","ASM_NAME":"Pradeep Maurya"}],"imgPath":["https://www.tatasky.com/msales/SlotSetB/Slot1.jpg~ ","https://www.tatasky.com/msales/SlotSetB/Slot2.jpg~ ","https://www.tatasky.com/msales/SlotSetB/Slot3.jpg~"]}

        //for customer recharge api param
        //var mycript = MCrypt("dF9DwYJ2acGXXLpV","1234567890abcdef")
        //var cipherText = mycript.decrypt("ZQfQQq38X2XpFdgOCgJ1Ir0PLGGN+zaFrPtMo3yahDwYfTMCu+dussiKk/ZXOCRIhy8b+WW0Bn9TNl2LwzSfdW3sfUuVGjEYwPOBwBwyrxCIb+dh2WU4Y/SqRPPTHsUM")
        //["9178497060","","9727293009","1","","MOBDESKTOP","f5018082-72e1-4d4f-a6e6-094296483feb"] // here 1 is amount which is you want to recharge and last is get from login response recharge_token_id
        // 1.from no 2. subscriberid 3. to number 4. amount


        //for get balance
        //var mycript = MCrypt("3iF6j70L3AuxqdMS","1234567890abcdef")
        //var cipherText = mycript.decrypt("5qU3x0BAmNUX3UTEnjeMXuF0m1g54ICCRLIG9pZyVNJ5IL6WyH6PIJpuFo8y7rKguiyNSRUf+GGX2gGkLwmVDw")
        //["9178497060","9178497060","MOBWEB","","parentBalance"]

        //Fos
        //get balance
        //var mycript = MCrypt("iAtuqywZPoc4KzMx","1234567890abcdef")
        //var cipherText = mycript.decrypt("qfQRzZF9wLsSWwQJ6dbDY3vMvgaiizIo9S1Zww8KN58/jGWiKeszp4HlZiqWncH1ihSaTWXDGGO+dde5bYbFpA")
        //["9337336349","9337336349","MOBWEB","","parentBalance"]

        //end for new api

        //var mycript = MCrypt("ZeQkWFnRdF91xeyU","1234567890abcdef")
        //var cipherText = mycript.decrypt("cCSdg4SOZqzwa2L1ttFRIlSI+u7oHTxSAQr+1TrIDAeA+hukb3DBwLjgguHamFWB74g0l9aAlzTfbNdr1jDSKLkzC5IFNGLH5w69vLAZYRcEZfMghj8+YSD9YnT5zu4ctRdP0/6qokFIXsoznzeEH6RQ6syDDL5KmHff1p/BRHfnXx3UnOKM2S51QNxk0oStcZnndOPsJ+71NP1OTxJxoUPyuRdP0FnHFN3mIeuWDe4N+CLyrq3AWsNU3gaf+6iyX+LuIIwumhM3f+06Vzwc3Q==")
        //{"currentBalance":"1499.0","donarRMN":"9337336349","recipientRMN":"9337336349","childName":"Supriyo jena","type":"parentBalance","errorCode":"600","errorMessage":"Balance Inquiry Successful."}


        //var mycript = MCrypt("z2nIvwSD0bAXukz9","1234567890abcdef")
        //var cipherText = mycript.decrypt("[\"MTGbHftsFarpeMnJOmstARetpoJuZd7UlA6dsQ9oT2SMwV442ixpyZ/foJjXB8FtM/3M/FHt83aasCxU4imSYxuHLCs7S2RLvmLNFSHlHRBE8aHpX5ZB3rkLw1wv64Cc")

        //fos
        //login
        //var mycript = MCrypt("qUEX18Mx2lRIDQcf","1234567890abcdef")
        //var cipherText = mycript.decrypt("KS8ESZIw3wLNuuNFhAudNz2APkLyV6aK/oVEitjJD9KHAcg+lR0iEm3D/nbzEJ4/")
        //["9337336349","zolta2020","MOBWEB"]

        //StaticUtility.showMessage(mContext, cipherText)
        //StaticUtility.showMessage(mContext, cipherText1)

        //check geo location recharge param
        //var mycript = MCrypt("qUEX18Mx2lRIDQcf","1234567890abcdef")
        //var cipherText = mycript.decrypt("TPcWLQ9WbJ1UwND1ldTmL0ipRGJWJzFgmHqgpAad4HQ=")
        //["9337336349","EVD","Recharge"]

        //get account information param with sbscriber id
        //var mycript = MCrypt("qUEX18Mx2lRIDQcf","1234567890abcdef")
        //var cipherText = mycript.decrypt("SInYQHPPoK4eak4ncX2nILWTxVwVW0ZdTpNVrGrahYU=")
        //["9114050441","","MOBDATA"]

        //get account information param with sbscriber mobile number
        //var mycript = MCrypt("qUEX18Mx2lRIDQcf","1234567890abcdef")
        //var cipherText = mycript.decrypt("RMhXGgDfzr2QAr3LzrHzaUd8TzzFLFhihUPUmB5VFVE=")
        //["","9114050441","MOBDATA"]

        //get param evd transfer validateobile number
        //var mycript = MCrypt("qUEX18Mx2lRIDQcf","1234567890abcdef")
        //var cipherText = mycript.decrypt("4GZG+UC/YfYcWfhPr188Gx6dqSbot4c+h0lx614ZSgv+lFIByn5WPIrBla4H04vzDTxLRot8y84aM6NTXjLBAw==")
        //["9337336349","9114050441","MOBWEB","","validateName"]

        //get param evd transfer final process submit
       // var mycript = MCrypt("QtVcSR6GgVBEsf8I","1234567890abcdef")
        //var cipherText = mycript.decrypt("GB/xf6skwjPiVKewOXuB6H+X29Jw0IjF5rg7nBOkQhNY2feo1BmPXntbM+nuxYzH")
        //["9337336349","9178497060","100","MOBWEB"]

        //get param for check current balance retailer(evd balance information
        //var mycript = MCrypt("np1cgZigHSqxf1iJ","1234567890abcdef")
        //var cipherText = mycript.decrypt("wGB7a02NFrdO9GIpmK1mUObUKrFKy74hQxisdjZz3I9Ayzst3RVeJJ0doxVKVeI+NJJ4j8DM5lp6qstfEX6Q0Q==")
        //["9178497060","9178497060","MOBWEB","","parentBalance"]

        //master
        //login master param
        //var mycript = MCrypt("M1tlm3LvxReJpRLA","1234567890abcdef")
        //var cipherText = mycript.decrypt("Icf5XQI+vCv+kMOJPssrJIyCUTPv5cQv3NSyY00JAVyuGN6xdVO4MS5fqLXdpHds")
        //["7077668877","Tatasky2010","MOBWEB"]

        //get param evd transfer
        //var mycript = MCrypt("5EDYXdqMhtPIP68K","1234567890abcdef")
        //var cipherText = mycript.decrypt("bpnmlPRW4qOIyda7vO32jeu+hcQcx+ILpgyZ5KjIJbL3E/9iQ2tYMCpMaERsXTxx7Tpy1ODxX4lsHn2w0XPchg==")
        //param is below for validate mobile number in evd transfer
        //["7077668877","9337336349","MOBWEB","","validateName"]

        //get param evd transfer final process submit
        //var mycript = MCrypt("5EDYXdqMhtPIP68K","1234567890abcdef")
        //var cipherText = mycript.decrypt("bpnmlPRW4qOIyda7vO32jfr+Or0xN56ECUgCxBq1FEM0ckOFjfMyR8MMpP4854Vr")
        //param is below for validate mobile number in evd transfer
        //["7077668877","9337336349","100","MOBWEB"] // here 100 is amount which is you want to recharge


        //StaticUtility.showMessage(mContext, cipherText)
//

        //StaticUtility.showMessage(mContext, str)
        //GetInstanceIdandTokenApiCall()
        //val isinternet = NetworkAvailable(internetHandler)
        //isinternet.execute()
    }

    fun ApiCall( instanceid : String, cookies : String){
        internetHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var obj = JSONArray()
                    obj.put("9178497060")
                    obj.put("Zplus@2019")
                    obj.put("MOBWEB")

                    var str = obj.toString()


                    var mycript = MCrypt(SharedPreference.GetPreference(mContext, StaticUtility.ENCKEYPREFERENCE,StaticUtility.ENCKEY),
                        SharedPreference.GetPreference(mContext, StaticUtility.ENCKEYPREFERENCE,StaticUtility.IVKEY))
                    str = mycript.encrypt(obj.toString())

                    var param = JSONArray()
                    //param.put("ASJQTCjW/3evbDZmW021uY0q6N2Wsm5umHpdbAUZKF4NzJjBNS1TVPunyvgM55FJ"+":"+"3fe31ea5-d2b2-4fbd-a190-9ca06bc36cd6")
                    param.put(str+":"+SharedPreference.GetPreference(mContext, StaticUtility.ENCKEYPREFERENCE, StaticUtility.ID))
                    param.put("phase3")
                    param.put("null")
                    StaticUtility.Logincall(param.toString(), internetHandler)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                val response = msg.obj as LoginResponse
                //tVeucllx8OVcVdmFTc5IefuCWZrfGK//ytrJ4b+LPsmYRoQ5//eKWlZ2cYHXDv+syWHAvftNgL2icZ1UjmNp9RgI4hiWJyGtbEN9RZRw34EuAVHIiKL8FN8l/CWahlWXbwI1f7pL0KmyCk4NMsi0YKU7zsqT4TXOgsX8GeOKsyFE77ZlFZUrQfa25MRvyVHezZe50Ug+K/tt7sYUjZII0lk8GJuXEGMM0VxVK8Oxbsu1qUrGIxch1L5srOTJn/443IOs6Vm/P9B22rJK3BqDIe37oog47RPjbu6/FCmM6BwT48yeAE/FYkv+rYMObFsUme1KwbveqDKl+fKxmjWO9mF2fXFamJ4wMz1ub+/E4MIlcOEGiS1wnxgW0YqJhdolXvsRN9iKzn3EW78T+BqKBp5fCAksfCdVsk6iiT8nKcsHfoOAPHl9nIZN2czAwu37Xach76AYzHV3E65HQpqpzYGwp4XcOuT5w4jpNreJfmcb9cjpGYaOOpunjeJS6SEQwgoisHVwYEZNJz0rx/23D3+Z47DR4L6Bfci0RxiiM1fkbhuDIxz72h9z1m27ZlItQYL7gnMUjf9n5klw3ixIIykAfi5BMmyGxgDZUSfkcOOh1ilYYjvrKqB4Tf1hVFMAYet7RsbPgwRlbOGKUG1dvrRoIV+Y1A+LVbKA3S/bzY7P+tWn+DjVqbxG9E6CN9Q1vtWnkBrcpgWRH5JHKpnb3ovBoVD8AM1M+szw7XkdL4h/oykI8w7iRmnojV5OTfsO
                var mycript = MCrypt(SharedPreference.GetPreference(mContext, StaticUtility.ENCKEYPREFERENCE,StaticUtility.ENCKEY),
                    SharedPreference.GetPreference(mContext, StaticUtility.ENCKEYPREFERENCE,StaticUtility.IVKEY))
                var str = mycript.decrypt(response.result.payload)
                StaticUtility.showMessage(mContext, str)
                /*if (response.code == "200") {
                    val code = response.code
                    val status = response.status
                    val message = response.message
                    val user_id = response.payload?.user_id
                    val picture = response.payload?.profilepicture?.get(0)
                    val user_token = response.payload?.authUser?.user_token
                } else {
                    StaticUtility.sendMail(
                        "Getting error in GetUserData to server in MainActivity.\n",
                        response.toString()
                    )
                }*/
            }
            true
        })


    }

    fun GetInstanceIdandTokenApiCall(){
        internetHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var param = JSONArray()
                    //param.put("ASJQTCjW/3evbDZmW021uY0q6N2Wsm5umHpdbAUZKF4NzJjBNS1TVPunyvgM55FJ"+":"+"3fe31ea5-d2b2-4fbd-a190-9ca06bc36cd6")
                    param.put("")
                    var params = mapOf("adapter" to "HttpEncGetKey", "procedure" to "getkey",
                        "compressResponse" to "true", "parameters" to param.toString(),
                        "isAjaxRequest" to "true", "x" to "0.05143110301586007c")
                    StaticUtility.call(params, "",internetHandler)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                var res = msg.obj.toString().split("coockie")
                var header = res[1]
                var headers = header.split("\n")
                var cookie = ""
                for((i, head) in headers.withIndex()){
                    if(head.contains("Set-Cookie"))
                    {
                        var headers = head.split(";")
                        if(cookie == "") {
                            cookie = headers[0].replace(" ","")
                        }else{
                            cookie = cookie+"; "+ headers[0].replace(" ","").replace(";","")
                        }
                    }
                }
                cookie = cookie.replace("Set-Cookie:","").replace("Path=/","").
                        replace("HttpOnly","").replace("path=/","")
                val response = JSONObject(res[0])
                var challenges = response.getJSONObject("challenges")
                var wl_antiXSRFRealm = challenges.getJSONObject("wl_antiXSRFRealm")
                var wl_deviceNoProvisioningRealm = challenges.getJSONObject("wl_deviceNoProvisioningRealm")
                var WL_Instance_Id = wl_antiXSRFRealm.getString("WL-Instance-Id")
                var token = wl_deviceNoProvisioningRealm.getString("token")
                StaticUtility.showMessage(mContext, token)
                StaticUtility.showMessage(mContext, WL_Instance_Id)
                GetEncKeyApiCall(token, WL_Instance_Id, cookie)
                val isinternet = NetworkAvailable(internetHandler)
                isinternet.execute()
            }
            true
        })


    }

    fun GetEncKeyApiCall(token : String, instanceid : String, cookies : String){
        internetHandler = Handler(Handler.Callback { msg ->
            if(msg.arg1 == 1){
                if(msg.obj as Boolean) {
                    var main = JSONObject()
                    var wl_deviceNoProvisioningRealm = JSONObject()
                    var ID = JSONObject()
                    ID.put("token",token)
                    var app = JSONObject()
                    app.put("id","SalesAndPartner")
                    app.put("version","1.1.2")
                    ID.put("app",app)
                    var device = JSONObject()
                    device.put("id","6d76b959-ea66-37fd-9128-754d492ca478")
                    device.put("os","5.1.1")
                    device.put("model","SM-E700H")
                    device.put("environment","android")
                    ID.put("device",device)
                    var custom = JSONObject()
                    ID.put("custom", custom)
                    wl_deviceNoProvisioningRealm.put("ID",ID)
                    main.put("wl_deviceNoProvisioningRealm", wl_deviceNoProvisioningRealm)
                    var authorization = main.toString()
                    var param = JSONArray()
                    //param.put("ASJQTCjW/3evbDZmW021uY0q6N2Wsm5umHpdbAUZKF4NzJjBNS1TVPunyvgM55FJ"+":"+"3fe31ea5-d2b2-4fbd-a190-9ca06bc36cd6")
                    param.put("")
                    /*var params = mapOf("adapter" to "HttpEncGetKey", "procedure" to "getkey",
                        "compressResponse" to "true", "parameters" to param.toString(), "__wl_deviceCtx" to "AHYb0n6yps1mtBAA",
                        "isAjaxRequest" to "true", "x" to "0.7359216556226398")*/
                    StaticUtility.GetEncKeycall(param.toString(), internetHandler)
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            }else if(msg.arg1 == 0){
                val response = msg.obj as response

                SharedPreference.CreatePreference(mContext, StaticUtility.ENCKEYPREFERENCE)
                SharedPreference.SavePreference(StaticUtility.ID, response.result.payload.id)
                SharedPreference.SavePreference(StaticUtility.IVKEY, response.result.payload.ivKey)
                SharedPreference.SavePreference(StaticUtility.ENCKEY, response.result.payload.enckey)

                ApiCall(instanceid, cookies)
                val isinternet = NetworkAvailable(internetHandler)
                isinternet.execute()
            }
            true
        })
    }
    /*fun ApiCall(){
        internetHandler = android.os.Handler(Handler.Callback { msg ->
                if(msg.obj as Boolean) {
                    val body = BodyParam("test@gmail.com", "123456", "dgfdgfhyng", "")
                    val client = ApiClient.client.create(ApiInterface::class.java)

                    client.apicall(
                        StaticUtility.CONTENT_TYPE, StaticUtility.APP_ID, StaticUtility.APP_SECRET, body
                    ).enqueue(object : Callback<response> {
                        @SuppressLint("ShowToast")
                        override fun onResponse(call: Call<response>, response: Response<response>) {
                            if (response.code() == 200) {
                                val code = response.body()?.code
                                val status = response.body()?.status
                                val message = response.body()?.message
                                val user_id = response.body()?.payload?.user_id
                                val picture = response.body()?.payload?.profilepicture?.get(0)
                                val user_token = response.body()?.payload?.authUser?.user_token
                            } else {
                                StaticUtility.sendMail(
                                    "Getting error in GetUserData to server in MainActivity.\n",
                                    response.toString()
                                )
                            }
                        }

                        override fun onFailure(call: Call<response>, t: Throwable) {
                            StaticUtility.showMessage(mContext, t.toString())
                            //Creating SendMail object
                            StaticUtility.sendMail(
                                "Getting error in getUserdata from server in MainActivity.\n",
                                t.toString()
                            )
                        }
                    })
                }else
                    StaticUtility.showMessage(mContext,getString(R.string.network_error))
            true
        })
    }*/
}
