package android.marc.com.fastfoodcalorieestimationandroid.model

import com.google.gson.annotations.SerializedName

data class PredictResponse(

    @SerializedName("img_byte_enc")
    val imageByteEncoded: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("calorie")
    val calorie: Double,

    @SerializedName("error")
    val isError: Boolean,

    @SerializedName("error_message")
    val errorMessage: String
)
