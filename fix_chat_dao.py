with open("app/src/main/java/com/example/data/local/dao/ChatMessageDao.kt", "r") as f:
    content = f.read()

content = content.replace("fun getAllMessages(): Flow<List<ChatMessageEntity>>", 
"""fun getAllMessages(): Flow<List<ChatMessageEntity>>
    
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getPagedMessages(): PagingSource<Int, ChatMessageEntity>
""")

with open("app/src/main/java/com/example/data/local/dao/ChatMessageDao.kt", "w") as f:
    f.write(content)
