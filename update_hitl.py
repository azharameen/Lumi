import re

file_path = 'app/src/main/java/com/example/domain/agent/hitl/HitlApprovalManager.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.data.local.LumiDatabase',
    'import com.example.domain.repository.AgentStateRepository\nimport com.example.domain.memory.SemanticMemoryEngine'
)

content = content.replace(
    '''class HitlApprovalManager(
    private val database: LumiDatabase,''',
    '''class HitlApprovalManager(
    private val agentStateRepository: AgentStateRepository,
    private val semanticMemoryEngine: SemanticMemoryEngine,'''
)

content = content.replace(
    'val stateMachine = LumiAgentGraph.create(database, toolDispatcher)',
    'val stateMachine = LumiAgentGraph.create(agentStateRepository, semanticMemoryEngine, toolDispatcher)'
)

with open(file_path, 'w') as f:
    f.write(content)

