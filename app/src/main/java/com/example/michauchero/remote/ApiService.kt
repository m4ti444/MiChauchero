package com.example.michauchero.remote

import okhttp3.ResponseBody
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @Streaming
    @Headers("Accept: application/pdf")
    @POST("api/reporte/pdf")
    suspend fun generarReportePdf(
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("income") income: Double,
        @Query("expenses") expenses: Double,
        @Query("budgetAmount") budgetAmount: Double?
    ): ResponseBody
}
