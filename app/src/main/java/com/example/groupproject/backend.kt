package com.example.backend

import android.R
import android.content.Context
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.groupproject.RiskResult
import org.json.JSONObject
import java.net.ConnectException


// DOCUMENTATION
// import com.example.backend.backend
// import com.example.backend.BErr

// setAddresss(ipa:String)
// set the IP address (ie "192.100.20.20" or "localhost") that requests will be sent to
// might be convenient to have UI to set this, but i think dani will be hosting on dedicated server in future so might not be needed

// localMode (boolean variable)
// when enabled requests will respond with dummy data, rather than making network requests, for testing without server running






data class Prescription (val name: String, val DosageMg: Int )
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
    val Gender: Gender,
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

enum class DementiaEnum { Unknown, Positive, Negative }
data class PatientInfo (
    val Name: String,
    val HasDementia: DementiaEnum,
    val DoctorID: String?,
    val RiskScore: String?, // is a float
)

val dummyPatient = PatientInfo("Conor", DementiaEnum.Unknown, null, "0.5")



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

data class Resp_Patient(
    val err: BErr,
    val message: PatientInfo,
)

data class Resp_test(
    val err: BErr,
    val result: String,
)






// Main Thingie
object backend {
    // private var ipa = "000.00.00.00"
    private var ipa = "dementica.danigoes.online"
    // private var ipa = "localhost"
    private var prefix = "http://${ipa}:80"
    fun setAddresss(ipa:String) { this.ipa = ipa; prefix = "http://${ipa}:80" }

    var localMode = false

    var backend_id_token: String? = null
    var backend_uid: String? = null



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
        val req = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
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






    // Login Request
    fun login(ctx: Context, myemail:String, mypassword:String, cb: (Resp_Hello)->Unit  ){
        // if localmode
        if (localMode) {
            log("login LOCAL MODE")
            cb(Resp_Hello(BErr.Ok,"Ok"))
            return }

        // state request
        val url = "$prefix/v1/login"
        val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
        val jsonBody = JSONObject("{\"Email\":\"$myemail\",\"Password\":\"$mypassword\",\"UserType\":\"Patient\"}")

        // handle response
        val req = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                val uid = response.getString("UserID")
                val idToken = response.getString("IdToken")
                val res = response.getString("Result")
                log(response.toString())
                log("login Success $res, $uid, $idToken")
                // set backend variables (used for other functions)
                if (res == "Ok") {
                    backend_uid = uid
                    backend_id_token = idToken
                    cb(Resp_Hello(BErr.Ok,res))
                } else {
                    cb(Resp_Hello(BErr.Exception,res)) }
            },

    Response.ErrorListener { error ->
        var resp = Resp_Hello(BErr.Exception,error.toString())
        log("login Error")

        // check if network issue
        if (error.cause != null){
            try { throw (error.cause as Throwable) }
            catch (e: ConnectException){
                resp = Resp_Hello(BErr.Not_Signed_In,error.toString())
                log("    NoConnectionError")
            }
            catch(e: Exception) { log("    Other Exception")}
        } else { log("    else auth") }
        log( "    localizedMessage: ${error.localizedMessage}")
        log( "    toString ${error.toString()}")
        cb(resp) },
        ) {}
        queue.add(req)
    } // login end



    // Signup Request
    fun signUp(ctx: Context, myname:String, myemail:String, mypassword:String, cb: (Resp_Hello)->Unit  ){
        // if localmode
        if (localMode) {
            log("signUp LOCAL MODE")
            cb(Resp_Hello(BErr.Ok,"Ok"))
            return }

        // state request
        val url = "$prefix/v1/register"
        val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
        val jsonBody = JSONObject("{\"Name\":\"$myname\",\"UserType\":\"Patient\",\"RegType\":\"Email\",\"RegisterWith\":\"$myemail\",\"Password\":\"$mypassword\"}")
        log("url $url")

        // handle response
        val req = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                val res = response.getString("Result")
                log("signup Success $res")

                if (res == "Ok") {
                    cb(Resp_Hello(BErr.Ok,res))
                } else { cb(Resp_Hello(BErr.Exception,res)) }
            },


            Response.ErrorListener { error ->
                var resp = Resp_Hello(BErr.Exception,error.toString())
                log("signup Error")

                // check if network issue
                if (error.cause != null){
                    try { throw (error.cause as Throwable) }
                    catch (e: ConnectException){
                        resp = Resp_Hello(BErr.Not_Signed_In,error.toString())
                        log("    NoConnectionError")
                        log("    $e")
                    }
                    catch(e: Exception) { log("    Other Exception"); log(e.toString())}
                } else { log("    else auth") }
                log( "    localizedMessage: ${error.localizedMessage}")
                log( "    toString ${error.toString()}")
                cb(resp) },
        ) {}
        queue.add(req)
    } // Signup end






    // Risk Request
    fun request_risk(ctx: Context, cb: (Resp_Hello)->Unit  ){
		// if localmode
        if (localMode) {
            log("Request_risk LOCAL MODE")
            cb(Resp_Hello(BErr.Ok,"0.42"))
			return }


		if (backend_uid == null) { cb(Resp_Hello(BErr.Not_Signed_In, "0.5"))}
		else {
			// state request
			val url = "$prefix/v1/get_risk"
			val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
			val jsonBody = JSONObject("{\"UserID\":\"${backend_uid}\"}")

			val req = object : JsonObjectRequest(
				Method.POST, url, jsonBody,
				Response.Listener { response ->
					val resp = Resp_Hello(BErr.Ok,response.getString("RiskScore"))
					log("Risk-Risk Success ${resp.message}")
					cb(resp) },

				Response.ErrorListener { error ->
					var resp = Resp_Hello(BErr.Exception,error.toString())
					log("Request_Risk Error")

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
		}
    } // request_risk end



    // Data Request
    fun request_data(ctx: Context, cb: (Resp_Patient)->Unit ){
        // if localmode
        if (localMode) {
            log("Request_data LOCAL MODE")
            cb(Resp_Patient(BErr.Ok,dummyPatient))
            return }

        // state request
        val url = "$prefix/v1/patient_info"
        val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
        val jsonBody = JSONObject("{\"UserID\":\"$backend_uid\"}")

        // handle response
        val req = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                val res = response.getString("Result")
                val name = response.getString("Name")
                val hasDementia = response.getString("HasDementia")
                val doctorId = response.getString("DoctorID")
                val riskScore = response.getString("RiskScore")

                val dementia = when(hasDementia){
                    "Unknown" -> DementiaEnum.Unknown
                    "Positive" -> DementiaEnum.Positive
                    "Negative" -> DementiaEnum.Negative
                    else -> null
                }

                log("Request_data $res")
                if (res == "Ok" && dementia != null ) {
                    val patient = PatientInfo(
                        Name = name,
                        HasDementia = dementia,
                        DoctorID = if (doctorId == "") null else doctorId,
                        RiskScore = if (riskScore == "") null else riskScore,
                    )
                    cb(Resp_Patient(BErr.Ok, patient))
                } else {
                    cb(Resp_Patient(BErr.Exception, dummyPatient))
                } },

            Response.ErrorListener { error ->
                var resp = Resp_Patient(BErr.Exception,dummyPatient)
                log("Request_data Error")

                // check if network issue
                if (error.cause != null){
                    try { throw (error.cause as Throwable) }
                    catch (e: ConnectException){
                        resp = Resp_Patient(BErr.Not_Signed_In,dummyPatient)
                        log("    NoConnectionError")
                    }
                    catch(e: Exception) { log("    Other Exception")}
                }
                log( "    localizedMessage: ${error.localizedMessage}")
                log( "    toString ${error.toString()}")
                cb(resp) },
        ) {}
        queue.add(req)
    } // request_data end





    // News Request
    fun request_news(ctx: Context, cb: (Resp_Hello)->Unit  ){
	// if localmode
        if (localMode) {
            log("Request_News LOCAL MODE")
            cb(Resp_Hello(BErr.Ok,"You are all alone"))
            return }

	// state request
        val url = "$prefix/v1/get_news"
        val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
        val jsonBody = JSONObject("{\"Type\":\"Patient\"}")

	// handle response
        val req = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                val resp = Resp_Hello(BErr.Ok,response.getString("Content"))
                log("Request_News Success ${resp.message}")
                cb(resp) },

            Response.ErrorListener { error ->
                var resp = Resp_Hello(BErr.Exception,error.toString())
                log("Request_News Error")

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
    } // request_news end







    fun send_transcription(ctx: Context, transcript:String, cb: (Resp_test)->Unit  ){
        // if localmode
        if (localMode) {
            log("Send_transcription LOCAL MODE")
            cb(Resp_test(BErr.Ok,"0.5"))
            return }


        if (backend_uid == null) { cb(Resp_test(BErr.Not_Signed_In, "NaN"))}
        else {
            // state request
            val url = "$prefix/v1/send_transcript"
            val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
            val jsonBody = JSONObject("{\"UserID\":\"${backend_uid}\",\"data\":\"$transcript\"}")

            val req = object : JsonObjectRequest(
                Method.POST, url, jsonBody,
                Response.Listener { response ->
                    val resp = response.getString("Result")
					val score = response.getString("RiskScore")
                    log("send transcript Success $resp, $score")
					if resp == "Ok" {
						cb(Resp_test(BErr.Ok,score))
					} else {
						cb(Resp_test(BErr.Exception,"NaN"))
				},

                Response.ErrorListener { error ->
                    var resp = Resp_test(BErr.Exception,error.toString())
                    log("send transcript Error")

                    // check if network issue
                    if (error.cause != null){
                        try { throw (error.cause as Throwable) }
                        catch (e: ConnectException){
                            resp = Resp_test(BErr.Not_Signed_In,error.toString())
                            log("    NoConnectionError")
                        }
                        catch(e: Exception) { log("    Other Exception")}
                    }
                    log( "    localizedMessage: ${error.localizedMessage}")
                    log( "    toString ${error.toString()}")
                    cb(resp) },
            ) {}
            queue.add(req)
        }
    } // send_transcription end

    fun send_minimental(ctx: Context, data:String, timeDate:String, cb: (BErr)->Unit  ){
        // if localmode
        if (localMode) {
            log("send_minimental LOCAL MODE")
            cb(BErr.Ok)
            return }


        if (backend_uid == null) { cb(BErr.Not_Signed_In)}
        else {
            // state request
            val url = "$prefix/v1/send_minimental"
            val queue = Volley.newRequestQueue(ctx) // needs to be local bc context (iirc may be per component)
            val jsonBody = JSONObject("{\"UserID\":\"${backend_uid}\",\"data\":\"$data\",\"timeDate\":\"$timeDate\"}")

            val req = object : JsonObjectRequest(
                Method.POST, url, jsonBody,
                Response.Listener { response ->
                    val resp = BErr.Ok
                    log("send minimental Success")
                    cb(resp)},

                Response.ErrorListener { error ->
                    var resp = BErr.Exception
                    log("send minimental Error")

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
        }
    } // send_minimental end

    // Send Healtcare
    fun send_lifestyle(ctx: Context, data: LifestyleData, timeDate:String, cb: (Resp_test)->Unit  ){
        try {
            // if localmode
            if (localMode) {
                log("send_lifestyle LOCAL MODE")
                cb(Resp_test(BErr.Ok,"0.5"))
                return
            }

            // check signed in
            if (backend_uid == null) { cb(Resp_test(BErr.Not_Signed_In,""))}

            // state request
            val url = "$prefix/v1/send_lifestyle"
            val queue = Volley.newRequestQueue(ctx)

            // toString stuff
            fun trueFalse(b: Boolean): String {
                return if (b) "true" else "false"
            }

            val PrescriptionStr = if (data.Prescription != null) data.Prescription.name else "None"
            val DosageStr = if (data.Prescription != null) data.Prescription.DosageMg else "0"
            fun eduLevelStr(x: EducationLevel): String {
                return when (x) {
                    EducationLevel.No -> "No School"
                    EducationLevel.Primary -> "Primary School"
                    EducationLevel.Secondary -> "Secondary School"
                    EducationLevel.DeplomaDegree -> "Diploma/Degree"
                }
            }

            fun DomHandStr(x: DomHand): String {
                return when (x) {
                    DomHand.Left -> "Left"
                    DomHand.Right -> "Right"
                }
            }

            fun GenderStr(x: Gender): String {
                return when (x) {
                    Gender.Male -> "Male"
                    Gender.Female -> "Female"
                }
            }

            fun SmokingStr(x: SmokingStatus): String {
                return when (x) {
                    SmokingStatus.Current -> "Current Smoker"
                    SmokingStatus.Former -> "Former Smoker"
                    SmokingStatus.Never -> "Never Smoked"
                }
            }

            fun DietStr(x: NutritionDiet): String {
                return when (x) {
                    NutritionDiet.LowCarb -> "Low-Carb"
                    NutritionDiet.Mediterranean -> "Mediterranean"
                    NutritionDiet.Balanced -> "Balanced Diet"
                }
            }

            fun SleepStr(x: SleepQuality): String {
                return when (x) {
                    SleepQuality.Poor -> "Poor"
                    SleepQuality.Good -> "Good"
                }
            }

            fun ChronicStr(x: ChronicHealthConditions): String {
                return when (x) {
                    ChronicHealthConditions.Diabetes -> "Diabetes"
                    ChronicHealthConditions.HearthDisease -> "Heart Disease"
                    ChronicHealthConditions.Hypertension -> "Hypertension"
                    ChronicHealthConditions.None -> "None"
                }
            }

            fun ActivityStr(x: PhysicalActivity): String {
                return when (x) {
                    PhysicalActivity.Sedentary -> "Sedentary"
                    PhysicalActivity.Moderate -> "Moderate Activity"
                    PhysicalActivity.Mild -> "Mild Activity"
                }
            }


            val str =
                "Diabetic:${trueFalse(data.Diabetic)},AlcoholLevel:${data.AlcoholLevel}, HeartRate:${data.HeartRate}, BloodOxygenLevel:${data.BloodOxygenLevel}, BodyTemperature:${data.BodyTemperature}, Weight:${data.Weight}, MRI_Delay:${data.MRI_Delay}, Prescription:${PrescriptionStr}, DosageMg:${DosageStr}, Age:${data.Age}, EducationLevel:${eduLevelStr(data.EducationLevel)
                }, DominantHand:${DomHandStr(data.DominantHand)}, Gender:${GenderStr(data.Gender)}, FamilyHistory:${
                    trueFalse(
                        data.FamilyHistory
                    )
                }, SmokingStatus:${SmokingStr(data.SmokingStatus)}, APOE_e19:${trueFalse(data.APOEE4)}, PhysicalActivity:${
                    ActivityStr(
                        data.PhysicalActivity
                    )
                }, DepressionStatus:${trueFalse(data.DepressionStatus)}, MedicationHistory:${
                    trueFalse(
                        data.MedicationHistory
                    )
                }, NutrientDiet:${DietStr(data.NutrientDiet)}, SleepQuality:${SleepStr(data.SleepQuality)}, ChronicHealthConditions${
                    ChronicStr(
                        data.ChronicHealthConditions
                    )
                }"

            log(str)

            // val jsonBody = JSONObject("{\"UserID\":\"$str\"}")
             val jsonBody = JSONObject("{\"UserID\":\"${backend_uid}\",\"data\":\"$str\"}")
            log(jsonBody.toString())


            // handle response
            val req = object : JsonObjectRequest(
                Method.POST, url, jsonBody,
                Response.Listener { response ->
                    val resp = response.getString("Result")
					val score = response.getString("RiskScore")
                    log("send lifestyle Success $resp, $score")
					if resp == "Ok" {
						cb(Resp_test(BErr.Ok,score))
					} else {
						cb(Resp_test(BErr.Exception,"NaN"))
				},

                Response.ErrorListener { error ->
                    var resp = BErr.Exception
                    log("send_lifestyle Error")

                    // check if network issue
                    if (error.cause != null) {
                        try {
                            throw (error.cause as Throwable)
                        } catch (e: ConnectException) {
                            resp = BErr.Not_Signed_In
                            log("    NoConnectionError")
                        } catch (e: Exception) {
                            log("    Other Exception")
                        }
                    }
                    log("    localizedMessage: ${error.localizedMessage}")
                    log("    toString ${error.toString()}")
                    cb(Resp_test(resp,""))
                },
            ) {}
            queue.add(req)
        } catch (e: java.lang.Exception) { log(e.toString())}
    } // send_lifestyle end

    val ExampleLifestyle: LifestyleData = LifestyleData(
        Diabetic = true,
        AlcoholLevel = .084f,
        HeartRate = 98,
        BloodOxygenLevel = 96.23f,
        BodyTemperature = 36.224f,
        Weight = 57.56f,
        MRI_Delay = 36.42f,
        Prescription = null,
        Age = 60,
        EducationLevel = EducationLevel.Primary,
        DominantHand = DomHand.Left,
        Gender = Gender.Female,
        FamilyHistory = false,
        SmokingStatus = SmokingStatus.Current,
        APOEE4 = false,
        PhysicalActivity = PhysicalActivity.Sedentary,
        DepressionStatus = false,
        MedicationHistory = false,
        NutrientDiet = NutritionDiet.LowCarb,
        SleepQuality = SleepQuality.Poor,
        ChronicHealthConditions = ChronicHealthConditions.Diabetes
    ) // send lifestyle end






} // backend object end



// Utils
fun log(x:String) { Log.i("BACKEND",x)}



