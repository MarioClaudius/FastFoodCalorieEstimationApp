package android.marc.com.fastfoodcalorieestimationandroid.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @GET(".")
    fun test(): Call<String>

    @Multipart
    @POST("predict")
    fun predictImage(
        @Part imageFile: MultipartBody.Part
    ) : Call<String>
}