package com.puxin.webplayer.logic.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ProgramService {

    @POST
    fun record(
        @Url url: String,
        @Field("id") id: Int,
        @Field("num_str") number: Int,
        @Field("start_time") startTime: String,
        @Field("end_time") endTime: String): Call<Response<ResponseBody>>
}