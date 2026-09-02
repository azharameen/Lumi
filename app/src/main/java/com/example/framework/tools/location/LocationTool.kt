package com.example.framework.tools.location

import android.content.Context
import com.example.data.device.ContextLocationEngine
import com.example.domain.tools.*

class RealLocationTool(private val context: Context) : LumiTool {
    override val id: String = "system_get_current_location"
    override val displayName: String = "Get Current GPS Location 📍"
    override val description: String = "Retrieves real GPS coordinates, altitude, and city context from device sensors"
    override val category: ToolCategory = ToolCategory.UTILITY
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.MEDIUM
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val engine = ContextLocationEngine(context)
            val loc = engine.locationState.value
            
            ToolExecutionResult(
                success = true,
                resultText = "Location: ${loc.approximatePlace} (${loc.latitude}, ${loc.longitude})",
                payload = mapOf(
                    "lat" to loc.latitude,
                    "lng" to loc.longitude,
                    "place" to loc.approximatePlace
                )
            )
        } catch (e: Exception) {
            ToolExecutionResult(false, "Failed to fetch GPS location: ${e.localizedMessage}")
        }
    }
}
