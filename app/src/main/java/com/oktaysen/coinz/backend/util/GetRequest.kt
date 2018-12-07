package com.oktaysen.coinz.backend.util

import android.os.AsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

class GetRequest(private val http: OkHttpClient, private val callback: (String?) -> Unit) : AsyncTask<String, Unit, String>() {
    override fun doInBackground(vararg params: String?): String? {
        if (params.isEmpty() || params[0] == null) return null;
        val request = Request.Builder().url(params[0]!!).build()
        val response = http.newCall(request).execute()
        if (!response.isSuccessful) {
            Timber.e("Get Request failed: (${response.code()}) ${response.body().toString()}")
            return null
        }
        return response.body()?.string()
    }

    override fun onPostExecute(result: String?) {
        callback(result)
    }
}