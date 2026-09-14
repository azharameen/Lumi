import re

file_path = 'app/src/main/java/com/example/data/remote/HybridAiEngine.kt'
with open(file_path, 'r') as f:
    content = f.read()

# I noticed there was still a LumiDatabase import to remove
content = content.replace('import com.example.data.local.LumiDatabase\n', '')

content = content.replace(
    '''class HybridAiEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val aiAnalyticsDao: AiExecutionLogDao,
    private val database: LumiDatabase,
    private val context: Context? = null,
    private val toolRetriever: ToolRetriever? = null,
    val onDeviceGemmaEngine: OnDeviceGemmaEngine,
    private val modelSelectionEngine: ModelSelectionEngine? = null
) {
    val hitlApprovalManager = HitlApprovalManager(database, toolDispatcher)
    private val geminiEngine = GeminiAgentEngine(toolDispatcher, database, hitlApprovalManager, onDeviceGemmaEngine)
    private val semanticMemoryEngine = com.example.domain.memory.SemanticMemoryEngine(database)''',
    '''class HybridAiEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val aiAnalyticsDao: AiExecutionLogDao,
    private val agentStateRepository: AgentStateRepository,
    private val semanticMemoryEngine: SemanticMemoryEngine,
    private val context: Context? = null,
    private val toolRetriever: ToolRetriever? = null,
    val onDeviceGemmaEngine: OnDeviceGemmaEngine,
    private val modelSelectionEngine: ModelSelectionEngine? = null
) {
    val hitlApprovalManager = HitlApprovalManager(agentStateRepository, semanticMemoryEngine, toolDispatcher)
    private val geminiEngine = GeminiAgentEngine(toolDispatcher, agentStateRepository, semanticMemoryEngine, hitlApprovalManager, onDeviceGemmaEngine)'''
)

with open(file_path, 'w') as f:
    f.write(content)

