import re

file_path = 'app/src/main/java/com/example/domain/agent/LumiAgentGraph.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.data.local.LumiDatabase',
    'import com.example.domain.repository.AgentStateRepository\nimport com.example.domain.memory.SemanticMemoryEngine'
)

content = content.replace(
    '''    fun create(
        database: LumiDatabase,''',
    '''    fun create(
        agentStateRepository: AgentStateRepository,
        semanticMemoryEngine: SemanticMemoryEngine,'''
)

content = content.replace(
    'val stateMachine = AgentStateMachine(database.agentCheckpointDao())',
    'val stateMachine = AgentStateMachine(agentStateRepository)'
)

content = content.replace(
    '.registerNode(MemoryRetrievalNode(database))',
    '.registerNode(MemoryRetrievalNode(semanticMemoryEngine))'
)

with open(file_path, 'w') as f:
    f.write(content)

