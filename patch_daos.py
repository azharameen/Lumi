with open("app/src/main/java/com/example/data/local/dao/ChatMessageDao.kt", "r") as f:
    content = f.read()

if "fun getPagedMessages()" not in content:
    content = content.replace("import androidx.room.Query", "import androidx.room.Query\nimport androidx.paging.PagingSource")
    content = content.replace("fun getRecentMessages(): Flow<List<ChatMessageEntity>>", "fun getRecentMessages(): Flow<List<ChatMessageEntity>>\n\n    @Query(\"SELECT * FROM chat_messages ORDER BY timestamp DESC\")\n    fun getPagedMessages(): PagingSource<Int, ChatMessageEntity>")

with open("app/src/main/java/com/example/data/local/dao/ChatMessageDao.kt", "w") as f:
    f.write(content)


with open("app/src/main/java/com/example/data/local/dao/WellnessLogDao.kt", "r") as f:
    content = f.read()

if "fun getPagedWellnessLogs()" not in content:
    content = content.replace("import androidx.room.Query", "import androidx.room.Query\nimport androidx.paging.PagingSource")
    content = content.replace("fun getAllLogs(): Flow<List<WellnessLogEntity>>", "fun getAllLogs(): Flow<List<WellnessLogEntity>>\n\n    @Query(\"SELECT * FROM wellness_logs ORDER BY timestamp DESC\")\n    fun getPagedWellnessLogs(): PagingSource<Int, WellnessLogEntity>")

with open("app/src/main/java/com/example/data/local/dao/WellnessLogDao.kt", "w") as f:
    f.write(content)
