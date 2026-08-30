with open("app/src/main/java/com/example/domain/repository/LumiRepository.kt", "r") as f:
    content = f.read()

if "val pagedChatMessages: Flow<androidx.paging.PagingData<ChatMessageEntity>>" not in content:
    content = content.replace("val chatMessages: Flow<List<ChatMessageEntity>>", "val chatMessages: Flow<List<ChatMessageEntity>>\n    val pagedChatMessages: Flow<androidx.paging.PagingData<ChatMessageEntity>>")
    content = content.replace("val allWellnessLogs: Flow<List<WellnessLogEntity>>", "val allWellnessLogs: Flow<List<WellnessLogEntity>>\n    val pagedWellnessLogs: Flow<androidx.paging.PagingData<WellnessLogEntity>>")

with open("app/src/main/java/com/example/domain/repository/LumiRepository.kt", "w") as f:
    f.write(content)


with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "r") as f:
    content = f.read()

if "override val pagedChatMessages" not in content:
    content = content.replace("override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getRecentMessages()", 
"""override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getRecentMessages()
    
    override val pagedChatMessages: Flow<androidx.paging.PagingData<ChatMessageEntity>> = androidx.paging.Pager(
        config = androidx.paging.PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }
    ).flow
""")
    
    content = content.replace("override val allWellnessLogs: Flow<List<WellnessLogEntity>> = database.wellnessLogDao().getAllLogs()",
"""override val allWellnessLogs: Flow<List<WellnessLogEntity>> = database.wellnessLogDao().getAllLogs()

    override val pagedWellnessLogs: Flow<androidx.paging.PagingData<WellnessLogEntity>> = androidx.paging.Pager(
        config = androidx.paging.PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.wellnessLogDao().getPagedWellnessLogs() }
    ).flow
""")

with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "w") as f:
    f.write(content)
