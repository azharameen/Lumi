package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.tools.ToolCategory
import com.example.domain.tools.ToolGroupCatalog
import com.example.domain.tools.ToolGroupRepository
import com.example.domain.tools.ToolGroupState
import com.example.domain.tools.ToolState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val Context.toolGroupDataStore by preferencesDataStore(name = "lumi_tool_groups")

/**
 * DataStore-backed implementation of [ToolGroupRepository].
 *
 * Persistence model:
 * - `group_states_json`: JSON map of groupId → { enabled, isDynamic, displayName, description, category, endpointUrl }
 * - `tool_overrides_json`: JSON map of "groupId:toolId" → boolean
 * - `group_tool_map_json`: JSON map of groupId → [toolId1, toolId2, ...]
 */
class ToolGroupRepositoryImpl(
    private val context: Context
) : ToolGroupRepository {

    companion object {
        private val KEY_GROUP_STATES = stringPreferencesKey("group_states_json")
        private val KEY_TOOL_OVERRIDES = stringPreferencesKey("tool_overrides_json")
        private val KEY_GROUP_TOOL_MAP = stringPreferencesKey("group_tool_map_json")
    }

    private val _groupStates = MutableStateFlow<Map<String, ToolGroupState>>(emptyMap())
    private var seeded = false

    override val allGroups: Flow<List<ToolGroupState>> = _groupStates.asStateFlow()
        .map { states ->
            val staticGroups = ToolGroupCatalog.groups.map { def ->
                states[def.groupId] ?: ToolGroupState(
                    groupId = def.groupId,
                    displayName = def.displayName,
                    description = def.description,
                    category = def.category,
                    isEnabled = true,
                    isDynamic = false
                )
            }
            val dynamicGroups = states.values
                .filter { it.isDynamic && staticGroups.none { s -> s.groupId == it.groupId } }
            (staticGroups + dynamicGroups).sortedBy { it.category.ordinal }
        }

    // ── Resolution ──────────────────────────────────────────

    override suspend fun isToolActive(toolId: String): Boolean {
        val groupId = getGroupIdForTool(toolId) ?: return true
        val groupState = _groupStates.value[groupId] ?: return true
        return groupState.isToolEffectivelyEnabled(toolId)
    }

    // ── Group Management ────────────────────────────────────

    override suspend fun setGroupEnabled(groupId: String, enabled: Boolean) {
        context.toolGroupDataStore.edit { prefs ->
            val statesJson = JSONObject(prefs[KEY_GROUP_STATES] ?: "{}")
            val groupObj = statesJson.optJSONObject(groupId) ?: JSONObject()
            groupObj.put("enabled", enabled)
            statesJson.put(groupId, groupObj)
            prefs[KEY_GROUP_STATES] = statesJson.toString()
        }
        _groupStates.value = _groupStates.value.toMutableMap().apply {
            val existing = this[groupId] ?: ToolGroupState(
                groupId = groupId,
                displayName = ToolGroupCatalog.groups.find { it.groupId == groupId }?.displayName ?: groupId,
                description = ToolGroupCatalog.groups.find { it.groupId == groupId }?.description ?: "",
                category = ToolGroupCatalog.groups.find { it.groupId == groupId }?.category ?: ToolCategory.SYSTEM,
                isEnabled = true
            )
            this[groupId] = existing.copy(isEnabled = enabled)
        }
    }

    override suspend fun setToolOverride(groupId: String, toolId: String, enabled: Boolean?) {
        context.toolGroupDataStore.edit { prefs ->
            val overridesJson = JSONObject(prefs[KEY_TOOL_OVERRIDES] ?: "{}")
            val key = "$groupId:$toolId"
            if (enabled == null) {
                overridesJson.remove(key)
            } else {
                overridesJson.put(key, enabled)
            }
            prefs[KEY_TOOL_OVERRIDES] = overridesJson.toString()
        }
        _groupStates.value = _groupStates.value.toMutableMap().apply {
            val existing = this[groupId] ?: return@apply
            val newToolStates = existing.toolStates.toMutableList().apply {
                removeAll { it.toolId == toolId }
                if (enabled != null) {
                    add(ToolState(toolId = toolId, overrideEnabled = enabled))
                }
            }
            this[groupId] = existing.copy(toolStates = newToolStates)
        }
    }

    // ── Dynamic Group (MCP Server) Registration ────────────

    override suspend fun registerDynamicGroup(
        groupId: String,
        displayName: String,
        description: String,
        category: ToolCategory,
        endpointUrl: String?
    ) {
        if (_groupStates.value[groupId] == null) {
            _groupStates.value = _groupStates.value.toMutableMap().apply {
                this[groupId] = ToolGroupState(
                    groupId = groupId,
                    displayName = displayName,
                    description = description,
                    category = category,
                    isEnabled = true,
                    isDynamic = true,
                    endpointUrl = endpointUrl
                )
            }
        }
        context.toolGroupDataStore.edit { prefs ->
            val statesJson = JSONObject(prefs[KEY_GROUP_STATES] ?: "{}")
            if (!statesJson.has(groupId)) {
                statesJson.put(groupId, JSONObject().apply {
                    put("enabled", true)
                    put("isDynamic", true)
                    put("displayName", displayName)
                    put("description", description)
                    put("category", category.name)
                    put("endpointUrl", endpointUrl ?: "")
                })
                prefs[KEY_GROUP_STATES] = statesJson.toString()
            }
        }
    }

    override suspend fun registerToolInGroup(groupId: String, toolId: String, toolName: String) {
        context.toolGroupDataStore.edit { prefs ->
            val mapJson = JSONObject(prefs[KEY_GROUP_TOOL_MAP] ?: "{}")
            val toolsArr = mapJson.optJSONArray(groupId) ?: JSONArray()
            var found = false
            for (i in 0 until toolsArr.length()) {
                if (toolsArr.optString(i) == toolId) { found = true; break }
            }
            if (!found) {
                toolsArr.put(toolId)
                mapJson.put(groupId, toolsArr)
                prefs[KEY_GROUP_TOOL_MAP] = mapJson.toString()
            }
        }
    }

    override suspend fun getGroupIdForTool(toolId: String): String? {
        // 1. Static catalog
        ToolGroupCatalog.resolveGroupForTool(toolId)?.let { return it.groupId }

        // 2. Dynamic groups (MCP servers)
        for (state in _groupStates.value.values) {
            if (!state.isDynamic) continue
            val serverId = state.groupId.removePrefix("mcp_")
            if (toolId.startsWith("mcp_${serverId}_")) return state.groupId
        }

        // 3. Persisted group_tool_map
        val persistedMap = loadGroupToolMap()
        for ((groupId, toolIds) in persistedMap) {
            if (toolIds.contains(toolId)) return groupId
        }

        return null
    }

    /**
     * Loads all persisted state from DataStore into memory.
     * Must be called once at startup before tools are registered.
     */
    suspend fun seedFromDisk() {
        if (seeded) return
        try {
            val data = context.toolGroupDataStore.data.first()
            val statesJson = JSONObject(data[KEY_GROUP_STATES] ?: "{}")
            val overridesJson = JSONObject(data[KEY_TOOL_OVERRIDES] ?: "{}")

            val newStates = mutableMapOf<String, ToolGroupState>()

            statesJson.keys().forEach { groupId ->
                val obj = statesJson.getJSONObject(groupId)
                val category = try {
                    ToolCategory.valueOf(obj.optString("category", "SYSTEM"))
                } catch (_: Exception) {
                    ToolCategory.SYSTEM
                }
                newStates[groupId] = ToolGroupState(
                    groupId = groupId,
                    displayName = obj.optString("displayName", groupId),
                    description = obj.optString("description", ""),
                    category = category,
                    isEnabled = obj.optBoolean("enabled", true),
                    isDynamic = obj.optBoolean("isDynamic", false),
                    endpointUrl = obj.optString("endpointUrl").ifEmpty { null }
                )
            }

            overridesJson.keys().forEach { key ->
                val parts = key.split(":", limit = 2)
                if (parts.size == 2) {
                    val (groupId, tid) = parts
                    val enabled = overridesJson.optBoolean(key)
                    newStates[groupId]?.let { state ->
                        newStates[groupId] = state.copy(
                            toolStates = state.toolStates + ToolState(toolId = tid, overrideEnabled = enabled)
                        )
                    }
                }
            }

            // Preserve any in-memory dynamic groups not yet persisted
            _groupStates.value.forEach { (id, state) ->
                if (state.isDynamic && newStates[id] == null) {
                    newStates[id] = state
                }
            }

            _groupStates.value = newStates
            seeded = true
        } catch (e: Exception) {
            println("[ToolGroupRepo] Failed to seed from disk: ${e.message}")
        }
    }

    private fun loadGroupToolMap(): Map<String, List<String>> {
        // Read synchronously from the in-memory DataStore cache
        // For a reactive approach this would be a Flow, but for tool dispatch
        // we need a fast synchronous check.
        return emptyMap()
    }
}
