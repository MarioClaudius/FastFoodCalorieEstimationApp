package android.marc.com.fastfoodcalorieestimationandroid.activity.main

import android.marc.com.fastfoodcalorieestimationandroid.api.ApiConfig
import android.marc.com.fastfoodcalorieestimationandroid.model.PredictResponse
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel: ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess : LiveData<Boolean> = _isSuccess

    private val _isError = MutableLiveData<Boolean>()
    val isError : LiveData<Boolean> = _isError

    private val _testString = MutableLiveData<String>()
    val testString: LiveData<String> = _testString

    private val _predictResponse = MutableLiveData<PredictResponse>()
    val predictResponse : LiveData<PredictResponse> = _predictResponse

    init {
        _isLoading.value = false
        _isError.value = false
        _isSuccess.value = false
    }

    fun startLoading() {
        _isLoading.value = true
    }
    fun endLoading() {
        _isLoading.value = false
    }

    fun predictImage(imageMultipart: MultipartBody.Part) {
        startLoading()
        val predictImageService = ApiConfig().getApiService().predictImage(imageMultipart)
        predictImageService.enqueue(object : Callback<PredictResponse>{
            override fun onResponse(call: Call<PredictResponse>, response: Response<PredictResponse>) {
                Log.d("PREDICT IMAGE1", "SUCCESS")
                if (response.isSuccessful) {
                    Log.d("PREDICT IMAGE", "SUCCESS")
                    endLoading()
                    _predictResponse.value = response.body()
                }
            }

            override fun onFailure(call: Call<PredictResponse>, t: Throwable) {
                _isError.value = true
                endLoading()
                Log.d("ERROR", t.toString())
            }

        })
    }

    fun testApi() {
        startLoading()
        val testService = ApiConfig().getApiService().test()
        testService.enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    endLoading()
                    _testString.value = response.body().toString()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                _isError.value = true
                endLoading()
                Log.d("ERROR", t.toString())
            }

        })
    }
}