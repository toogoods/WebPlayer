package com.puxin.webplayer.logic

import androidx.lifecycle.liveData
import com.puxin.webplayer.logic.model.Data
import com.puxin.webplayer.logic.network.ProgramNetWork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

object Repository {

    @Suppress("ThrowableNotThrown")
    fun records(url: String, id: Int, number: Int, startTime: String, endTime: String) = liveData(Dispatchers.IO) {
        val result = try {
            val response = ProgramNetWork.records(url, id, number, startTime, endTime)
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body)
            } else {
                Result.failure(RuntimeException("response status is ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Data>>(e)
        }
        emit(result)
    }
}