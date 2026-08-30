with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace("override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getAllMessages()", 
"""override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getAllMessages()
    
    override val pagedChatMessages: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.example.data.local.entity.ChatMessageEntity>> = androidx.paging.Pager(
        config = androidx.paging.PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }
    ).flow
""")

with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "w") as f:
    f.write(content)
