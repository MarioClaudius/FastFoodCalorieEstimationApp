package android.marc.com.fastfoodcalorieestimationandroid.activity.main

import android.marc.com.fastfoodcalorieestimationandroid.api.ApiConfig
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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