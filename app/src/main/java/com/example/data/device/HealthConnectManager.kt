package com.example.data.device

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {
    
    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun getWellnessInsights(): String {
        if (!isAvailable()) {
            return "Health Connect is not available on this device."
        }

        try {
            val healthConnectClient = HealthConnectClient.getOrCreate(context)
            
            val now = Instant.now()
            val startOfDay = now.truncatedTo(ChronoUnit.DAYS)

            val stepsRequest = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
            )

            val stepsResponse = healthConnectClient.readRecords(stepsRequest)
            val totalSteps = stepsResponse.records.sumOf { it.count }

            val sleepRequest = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now)
            )

            val sleepResponse = healthConnectClient.readRecords(sleepRequest)
            val sleepHours = if (sleepResponse.records.isNotEmpty()) {
                val latestSleep = sleepResponse.records.last()
                val duration = java.time.Duration.between(latestSleep.startTime, latestSleep.endTime)
                duration.toHours().toDouble()
            } else {
                0.0
            }

            return "Total steps today: $totalSteps. Sleep last night: $sleepHours hours."

        } catch (e: Exception) {
            return "Unable to fetch Health Connect data. Have you granted permissions?"
        }
    }
}
