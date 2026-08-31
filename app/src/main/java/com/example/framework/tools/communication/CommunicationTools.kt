package com.example.framework.tools.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.domain.tools.*

object CommunicationToolsModule {
    fun register(context: Context, registry: ToolRegistry = ToolRegistry.getInstance()) {
        registry.registerTool(DraftSmsTool(context))
        registry.registerTool(DialPhoneTool(context))
    }
}

class DraftSmsTool(private val context: Context) : LumiTool {
    override val id = "communication_draft_sms"
    override val displayName = "Draft SMS Message"
    override val description = "Opens SMS app pre-filled with phone number and text message"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = ToolRiskLevel.HIGH
    override val parameters = listOf(
        ToolParameter("phoneNumber", "string", "Target phone number", required = true),
        ToolParameter("message", "string", "Text message body", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val number = params["phoneNumber"]?.toString() ?: ""
            val body = params["message"]?.toString() ?: ""
            
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$number")
                putExtra("sms_body", body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Drafted SMS to $number with message: '$body'")
        } catch (e: Exception) {
            ToolExecutionResult(false, "SMS draft failed: ${e.localizedMessage}")
        }
    }
}

class DialPhoneTool(private val context: Context) : LumiTool {
    override val id = "communication_dial_number"
    override val displayName = "Dial Phone Number"
    override val description = "Opens phone dialer pre-filled with number"
    override val category = ToolCategory.COMMUNICATION
    override val riskLevel = ToolRiskLevel.HIGH
    override val parameters = listOf(
        ToolParameter("phoneNumber", "string", "Phone number to dial", required = true)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        return try {
            val number = params["phoneNumber"]?.toString() ?: ""
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolExecutionResult(true, "Opened dialer for $number")
        } catch (e: Exception) {
            ToolExecutionResult(false, "Dialer failed: ${e.localizedMessage}")
        }
    }
}
