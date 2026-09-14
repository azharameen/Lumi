import re

file_path = 'app/src/main/java/com/example/data/remote/GeminiAgentEngine.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.data.local.LumiDatabase',
    'import com.example.domain.repository.AgentStateRepository\nimport com.example.domain.memory.SemanticMemoryEngine'
)

content = content.replace(
    '''class GeminiAgentEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val database: LumiDatabase,
    private val hitlApprovalManager: HitlApprovalManager? = null,
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null
) {''',
    '''class GeminiAgentEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val agentStateRepository: AgentStateRepository,
    private val semanticMemoryEngine: SemanticMemoryEngine,
    private val hitlApprovalManager: HitlApprovalManager? = null,
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null
) {'''
)

content = content.replace(
    'val stateMachine = LumiAgentGraph.create(database, toolDispatcher, onDeviceGemmaEngine, onStreamToken)',
    'val stateMachine = LumiAgentGraph.create(agentStateRepository, semanticMemoryEngine, toolDispatcher, onDeviceGemmaEngine, onStreamToken)'
)

with open(file_path, 'w') as f:
    f.write(content)

