with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }\n    ).flow',
    'pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }\n    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }'
)

# the sendMessage issue:
content = content.replace(
    'suspend fun sendMessage(userText: String, image: ByteArray? = null, modelId: String? = null): ChatMessageEntity',
    'suspend fun sendMessage(userText: String, image: ByteArray? = null, modelId: String? = null): ChatMessage'
)

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'w') as f:
    f.write(content)
