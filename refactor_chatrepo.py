import re

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'r') as f:
    content = f.read()

# Add imports
content = content.replace(
    'import com.example.domain.repository.ChatRepository',
    'import com.example.domain.model.ChatMessage\nimport com.example.domain.model.AiExecutionLog\nimport com.example.data.local.mapper.toDomain\nimport com.example.domain.repository.ChatRepository'
)
content = content.replace('import kotlinx.coroutines.flow.asStateFlow', 'import kotlinx.coroutines.flow.asStateFlow\nimport kotlinx.coroutines.flow.map')
content = content.replace('import androidx.paging.map', '')
content = content.replace('import androidx.paging.PagingData', 'import androidx.paging.PagingData\nimport androidx.paging.map')

# MutableStateFlow for streaming Ai message is still ChatMessageEntity probably, but interface is ChatMessage
content = content.replace('private val _streamingAiMessage = MutableStateFlow<ChatMessageEntity?>(null)', 'private val _streamingAiMessage = MutableStateFlow<ChatMessage?>(null)')
content = content.replace('override val streamingAiMessage: Flow<ChatMessageEntity?> = _streamingAiMessage.asStateFlow()', 'override val streamingAiMessage: Flow<ChatMessage?> = _streamingAiMessage.asStateFlow()')
content = content.replace('_streamingAiMessage.value = ChatMessageEntity(', '_streamingAiMessage.value = ChatMessage(')

# Chat messages map
content = content.replace(
    'override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getAllMessages()',
    'override val chatMessages: Flow<List<ChatMessage>> = database.chatMessageDao().getAllMessages().map { list -> list.map { it.toDomain() } }'
)

# Paging map
content = content.replace(
    'override val pagedChatMessages: Flow<PagingData<ChatMessageEntity>> = Pager(',
    'override val pagedChatMessages: Flow<PagingData<ChatMessage>> = Pager('
)
content = content.replace(
    'database.chatMessageDao().getPagedMessages()\n    }.flow',
    'database.chatMessageDao().getPagedMessages()\n    }.flow.map { pagingData -> pagingData.map { it.toDomain() } }'
)

# AI execution logs map
content = content.replace(
    'override val aiExecutionLogs: Flow<List<AiExecutionLogEntity>> = database.aiExecutionLogDao().getAllLogs()',
    'override val aiExecutionLogs: Flow<List<AiExecutionLog>> = database.aiExecutionLogDao().getAllLogs().map { list -> list.map { it.toDomain() } }'
)

# sendMessage return type
content = content.replace(
    'suspend fun sendMessage(userText: String, image: ByteArray? = null, modelId: String? = null): ChatMessageEntity = withContext(Dispatchers.IO) {',
    'suspend fun sendMessage(userText: String, image: ByteArray? = null, modelId: String? = null): ChatMessage = withContext(Dispatchers.IO) {'
)
content = content.replace(
    'return@withContext responseMsg',
    'return@withContext responseMsg.toDomain()'
)
content = content.replace(
    'return@withContext fallbackMsg',
    'return@withContext fallbackMsg.toDomain()'
)

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'w') as f:
    f.write(content)

