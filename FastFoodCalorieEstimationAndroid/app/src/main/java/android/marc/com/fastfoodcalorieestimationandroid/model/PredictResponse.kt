package android.marc.com.fastfoodcalorieestimationandroid.model

import com.google.gson.annotations.SerializedName

data class PredictResponse(

    @SerializedName("img_byte_enc")
    val imageByteEncoded: String
)
