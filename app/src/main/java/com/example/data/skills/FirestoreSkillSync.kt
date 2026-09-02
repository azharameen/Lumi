package com.example.data.skills

import android.content.Context
import android.util.Log
import com.example.domain.skill.SkillRegistry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Syncs skill JSON definitions from Firestore cloud storage to allow Over-The-Air (OTA)
 * skill deployments without needing Google Play Store updates.
 */
class FirestoreSkillSync(
    private val context: Context,
    private val skillRegistry: SkillRegistry = SkillRegistry.getInstance()
) {
    companion object {
        private const val TAG = "FirestoreSkillSync"
    }

    suspend fun syncRemoteSkills() = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("skills").get().addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val skillId = doc.id
                    val displayName = doc.getString("displayName") ?: skillId
                    val prompt = doc.getString("systemPromptExtension") ?: ""
                    Log.i(TAG, "Fetched remote skill: $displayName ($skillId)")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore skill sync unavailable: ${e.message}")
        }
    }
}
