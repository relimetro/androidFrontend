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

// send_lifestyle(LocalContext.current, LifestyleData) { resp -> ... }
// response is of type BErr
// NOTE: not implemented in backend yet








// Send data
//
// Lifestyle questionare
// cathal is using this dataset for LLM fine-tuning https://www.kaggle.com/api/v1/datasets/download/timothyadeyemi/dementia-patient-health-dataset
// features: Diabetic:Bool, AlcoholLevel:Float, HeartRate:int, BloodOxygenLevel: float, BodyTemperature: float, Weight:float, MRI_Delay: float, Presecription: String?, DosageMg: int? , Age: int, EducationLevel: [Primary,Secondary,No,Deploma/Degree], DominantHand: [left,right], Gender: [male,female], FamilyHistory:bool, SmokingStatus: [Current,Former,Never], APOE_e4:[positive,Negative], PhysicalActivity:[sedentary,moderate,mild], DepressionStatus:bool, MedicationHistory:bool,NutritionDiet:[LowCarb,Mediterranean,Balanced],SleepQuality:[Poor,Good],ChronicHealthConditions:[Diabetes,HearthDisease,Hypertension,None]
data class Prescription ( val name: String, val DosageMg: Int )
enum class EducationLevel { No,Primary,Secondary,DeplomaDegree }
enum class DomHand { Left,Right }
enum class Gender { Male, Female }
enum class SmokingStatus { Current,Former,Never}
enum class PhysicalActivity { Sedentary,Moderate,Mild}
enum class NutritionDiet { LowCarb, Mediterranean, Balanced }
enum class SleepQuality { Poor,Good }
enum class ChronicHealthConditions { Diabetes, HearthDisease, Hypertension, None }

data class LifestyleData ( // not sure if all are relevant so we might decide to remove some, but i wanted to just get bulk write all this code
    val Diabetic: Boolean,
    val AlcoholLevel: Float,
    val HeartRate: Int,
    val BloodOxygenLevel: Float,
    val BodyTemperature: Float,
    val Weight: Float,
    val MRI_Delay: Float,
    val Prescription: Prescription?,
    val Age: Int,
    val EducationLevel: EducationLevel,
    val DominantHand: DomHand,
    val gender: Gender,
    val FamilyHistory: Boolean,
    val SmokingStatus: SmokingStatus,
    val APOEE4: Boolean,
    val PhysicalActivity: PhysicalActivity,
    val DepressionStatus: Boolean,
    val MedicationHistory: Boolean,
    val NutrientDiet: NutritionDiet,
    val SleepQuality: SleepQuality,
    val ChronicHealthConditions: ChronicHealthConditions,
)



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
	// if localmode
        if (localMode) {
            log("Request_Hello LOCAL MODE")
            cb(Resp_Hello(BErr.Ok,"Hello, Me!"))
            return }

	// state request
        val url = "$prefix/v1/say_hello"
        val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
        val jsonBody = JSONObject("{\"name\":\"$x\"}")

	// handle response
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


    // Send Healtcare
    fun send_lifestyle(ctx: Context, data: LifestyleData, cb: (BErr)->Unit  ){
	// if localmode
        if (localMode) {
            log("send_lifestyle LOCAL MODE")
            cb(BErr.Ok)
            return }

	    // state request
        val url = "$prefix/v1/send_lifestyle"
        val queue = Volley.newRequestQueue(ctx)
	
    	// t*do generate json body, ensure consistent field/value names, lowercase boolean, cammelCase enum
        // val DiabeticStr = ...
        // val AlcoholLevelStr = ...
        // val HeartRateStr = ...
        // val BloodOxygenLevelStr = ...
        // val BodyTemperatureStr = ...
        // val WeightStr = ...
        // val MRI_DelayStr = ...
        // val PrescriptionStr = ...
        // val AgeStr = ...
        // val EducationLevelStr = ...
        // val DominantHandStr = ...
        // val genderStr = ...
        // val FamilyHistoryStr = ...
        // val SmokingStatusStr = ...
        // val APOEE4Str = ...
        // val PhysicalActivityStr = ...
        // val DepressionStatusStr = ...
        // val MedicationHistoryStr = ...
        // val NutrientDietStr = ...
        // val SleepQualityStr = ...
        // val ChronicHealthConditionsStr = ...
	
    	// val str = "\"Diabetic\":\"$DiabeticStr\", \"AlcoholLevel\":\"$AlcoholLevelStr\", \"HeartRate\":\"$HeartRateStr\", \"BloodOxygenLevel\":\"$BloodOxygenLevelStr\", \"BodyTemperature\":\"$BodyTemperatureStr\", \"Weight\":\"$WeightStr\", \"MRI_Delay\":\"$MRI_DelayStr\", \"Prescription\":\"$PrescriptionStr\", \"Age\":\"$AgeStr\", \"EducationLevel\":\"$EducationLevelStr\", \"DominantHand\":\"$DominantHandStr\", \"gender\":\"$genderStr\", \"FamilyHistory\":\"$FamilyHistoryStr\", \"SmokingStatus\":\"$SmokingStatusStr\", \"APOEE4\":\"$APOEE4Str\", \"PhysicalActivity\":\"$PhysicalActivityStr\", \"DepressionStatus\":\"$DepressionStatusStr\", \"MedicationHistory\":\"$MedicationHistoryStr\", \"NutrientDiet\":\"$NutrientDietStr\", \"SleepQuality\":\"$SleepQualityStr\", \"ChronicHealthConditions\":\"$ChronicHealthConditionsStr\""
        // val jsonBody = JSONObject("{$str}")
        val jsonBody = JSONObject("{\"name\":\"$data.Age\"}")
    	// note will need to include user ID when login/sessions are implemented (stored in this module, not somehting passed into function), will need to check for login/credentials before sending and early return BErr.Not_Signed_In

	    // handle response
        val req = object : JsonObjectRequest(Request.Method.POST, url, jsonBody,
            Response.Listener { response -> cb(BErr.Ok) },

            Response.ErrorListener { error ->
		        var resp = BErr.Exception
                log("send_lifestyle Error")

                // check if network issue
                if (error.cause != null){
                    try { throw (error.cause as Throwable) }
                    catch (e: ConnectException){
                        resp = BErr.Not_Signed_In
                        log("    NoConnectionError")
                    }
                    catch(e: Exception) { log("    Other Exception")}
                }
                log( "    localizedMessage: ${error.localizedMessage}")
                log( "    toString ${error.toString()}")
                cb(resp) },
        ) {}
        queue.add(req)
    } // send_lifestyle end



} // backend object end



// Utils
fun log(x:String) { Log.i("BACKEND",x)}



