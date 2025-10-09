package com.example.backend

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.ConnectException


// DOCUMENTATION
// IMPORTANT, fow now URL is hardcoded to IP address and will need to be manually set to whatever machine is currently hosting
// import com.example.backend.backend
// import com.example.backend.BErr

// setAddresss(ipa:String)
// set the IP address (ie "192.100.20.20" or "localhost") that requests will be sent to
// might be convenient to have UI to set this, but i think dani will be hosting on dedicated server in future so might not be needed

// localMode (boolean variable)
// when enabled requests will respond with dummy data, rather than making network requests, for testing without server running

// request_hello(LocalContext.current, "NAME") { resp -> ... }
// response is of type Resp_Hello (has two fields, err:BErr, message:String (string has error message if err)






// Responses
enum class BErr{
    Ok, // valid
    Not_Signed_In, // user is not logged in or no connection to backend (cannot be signed in if no connection), attempting to login will specify if cannot connect to backend
    Exception, // error that is not expected to happen : network exception or some server error, user not authorized etc
}

data class Resp_Hello(
    val err: BErr,
    val message: String,
)






// Main Thingie
object backend {
    private var ipa = "000.00.00.00"
    private var prefix = "http://${ipa}:80"
    fun setAddresss(ipa:String) { this.ipa = ipa; prefix = "http://${ipa}:80" }

    var localMode = false



    // Hello Request
    fun request_hello(ctx: Context, x:String, cb: (Resp_Hello)->Unit  ){
        if (localMode) {
            log("Request_Hello LOCAL MODE")
            cb(Resp_Hello(BErr.Ok,"Hello, Me!"))
            return }

        val url = "$prefix/v1/say_hello"
        val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
        val jsonBody = JSONObject("{\"name\":\"$x\"}")

        val req = object : JsonObjectRequest(Request.Method.POST, url, jsonBody,
            Response.Listener { response ->
                // response.has("message") // t*do validate return json
                val resp = Resp_Hello(BErr.Ok,response.getString("message"))
                log("Request_Hello Success ${resp.message}")
                cb(resp) },

            Response.ErrorListener { error ->
                var resp = Resp_Hello(BErr.Exception,error.toString())
                log("Request_Hello Error")

                // check if network issue
                if (error.cause != null){
                    try { throw (error.cause as Throwable) }
                    catch (e: ConnectException){
                        resp = Resp_Hello(BErr.Not_Signed_In,error.toString())
                        log("    NoConnectionError")
                    }
                    catch(e: Exception) { log("    Other Exception")}
                }
                log( "    localizedMessage: ${error.localizedMessage}")
                log( "    toString ${error.toString()}")
                cb(resp) },
        ) {}
        queue.add(req)
    } // request_hello end

} // backend object end



// Utils
fun log(x:String) { Log.i("BACKEND",x)}



