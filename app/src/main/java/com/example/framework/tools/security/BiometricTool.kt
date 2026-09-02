package com.example.framework.tools.security

import android.content.Context
import androidx.biometric.BiometricManager
import com.example.domain.tools.*

class BiometricCheckTool(private val context: Context) : LumiTool {
    override val id: String = "system_check_biometrics"
    override val displayName: String = "Biometric Hardware Check 🔐"
    override val description: String = "Verifies if biometric fingerprint or face authentication is available on device"
    override val category: ToolCategory = ToolCategory.SYSTEM
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val manager = BiometricManager.from(context)
        val canAuthenticate = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        
        val statusText = when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometric authentication is hardware-ready and enrolled."
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware available on device."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware is currently unavailable."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Biometric hardware supported, but no credentials enrolled."
            else -> "Biometric status unknown."
        }

        return ToolExecutionResult(
            success = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS,
            resultText = statusText
        )
    }
}
