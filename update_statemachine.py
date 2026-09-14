import re

file_path = 'app/src/main/java/com/example/domain/agent/AgentStateMachine.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Replace import
content = content.replace(
    'import com.example.data.local.dao.AgentCheckpointDao\nimport com.example.data.local.entity.AgentCheckpointEntity',
    'import com.example.domain.repository.AgentStateRepository'
)

# Replace constructor param
content = content.replace(
    'private val checkpointDao: AgentCheckpointDao? = null,',
    'private val agentStateRepository: AgentStateRepository? = null,'
)

# Replace handleCheckpointing logic
old_checkpoint = '''        if (state.status == AgentStatus.WAITING_FOR_HITL) {
            checkpointDao?.saveCheckpoint(
                AgentCheckpointEntity(
                    stateId = state.id,
                    userQuery = state.userQuery,
                    currentNodeName = state.currentNodeName,
                    status = state.status.name,
                    pendingToolName = state.pendingToolName,
                    pendingToolArgsJson = state.pendingToolArgs?.toString(),
                    serializedStateJson = ""
                )
            )
        } else {
            checkpointDao?.deleteCheckpoint(state.id)
        }'''
new_checkpoint = '''        if (state.status == AgentStatus.WAITING_FOR_HITL) {
            agentStateRepository?.saveCheckpoint(state)
        } else {
            agentStateRepository?.deleteCheckpoint(state.id)
        }'''
content = content.replace(old_checkpoint, new_checkpoint)

with open(file_path, 'w') as f:
    f.write(content)

