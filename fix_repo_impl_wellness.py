with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "r") as f:
    content = f.read()

# Add missing wellness property
if "override val pagedWellnessLogs: Flow<androidx.paging.PagingData<WellnessLogEntity>>" not in content:
    content = content.replace("override val allWellnessLogs: Flow<List<WellnessLogEntity>> = database.wellnessLogDao().getAllLogs()",
"""override val allWellnessLogs: Flow<List<WellnessLogEntity>> = database.wellnessLogDao().getAllLogs()

    override val pagedWellnessLogs: Flow<androidx.paging.PagingData<WellnessLogEntity>> = androidx.paging.Pager(
        config = androidx.paging.PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.wellnessLogDao().getPagedWellnessLogs() }
    ).flow
""")

with open("app/src/main/java/com/example/data/repository/LumiRepositoryImpl.kt", "w") as f:
    f.write(content)
