with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "r") as f:
    content = f.read()

# Add missing property
if "override val pagedChatMessages: Flow<androidx.paging.PagingData<ChatMessageEntity>>" not in content:
    content = content.replace("override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getRecentMessages()",
"""override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getRecentMessages()
    
    override val pagedChatMessages: Flow<androidx.paging.PagingData<ChatMessageEntity>> = androidx.paging.Pager(
        config = androidx.paging.PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }
    ).flow
""")

with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "w") as f:
    f.write(content)
