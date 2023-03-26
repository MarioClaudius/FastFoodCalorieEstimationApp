package android.marc.com.fastfoodcalorieestimationandroid.api

import android.marc.com.fastfoodcalorieestimationandroid.model.PredictResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET(".")
    fun test(): Call<String>

    @Multipart
//    @Headers("Content-Type: multipart/form-data", "Accept-Charset: utf-8")
//    @Headers("Accept-Charset: utf-8")
    @POST("predict")
    fun predictImage(
        @Part file: MultipartBody.Part
    ) : Call<PredictResponse>
}