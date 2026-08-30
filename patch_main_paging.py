with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("chatMessages = chatViewModel.chatMessages.collectAsStateWithLifecycle().value", "chatMessages = chatViewModel.pagedChatMessages.collectAsLazyPagingItems()")
if "import androidx.paging.compose.collectAsLazyPagingItems" not in content:
    content = content.replace("import androidx.compose.runtime.collectAsState", "import androidx.compose.runtime.collectAsState\nimport androidx.paging.compose.collectAsLazyPagingItems")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
