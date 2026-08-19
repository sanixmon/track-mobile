package com.evrenhouse.trackscooter.data

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiService {

    @GET("api/scooters")
    suspend fun getScooters(): List<Scooter>

    @POST("api/scooters")
    suspend fun addScooter(@Body body: AddScooterRequest): Scooter

    @DELETE("api/scooters/{id}")
    suspend fun deleteScooter(@Path("id") id: String): Map<String, Boolean>

    @PATCH("api/scooters/{id}")
    suspend fun updateScooter(@Path("id") id: String, @Body body: UpdateScooterRequest): Scooter

    @PUT("api/scooters/{id}/device-condition")
    suspend fun saveDeviceCondition(@Path("id") id: String, @Body body: SaveDeviceConditionRequest): SaveDeviceConditionResponse

    @POST("api/scooters/{id}/toggle")
    suspend fun toggleScooter(@Path("id") id: String, @Body body: ToggleRequest): ToggleResponse

    @GET("api/activity-log")
    suspend fun getActivityLog(): List<ActivityLogEntry>

    @GET("api/maintenance-records")
    suspend fun getMaintenanceRecords(): List<MaintenanceRecord>

    @POST("api/maintenance-records/{id}/complete")
    suspend fun completeMaintenance(@Path("id") id: String): CompleteMaintenanceResponse

    @GET("api/backup/download")
    @Streaming
    suspend fun downloadBackup(): ResponseBody

    @GET("api/export")
    suspend fun exportData(): Map<String, Any?>
}
