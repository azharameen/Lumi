import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

# Make sure to import LazyPagingItems
if "androidx.paging.compose.LazyPagingItems" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.LazyColumn", "import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.paging.compose.LazyPagingItems\nimport androidx.paging.compose.collectAsLazyPagingItems\nimport androidx.paging.compose.itemContentType\nimport androidx.paging.compose.itemKey")

# Update signature to take LazyPagingItems
content = content.replace("chatMessages: List<com.example.data.local.entity.ChatMessageEntity>,", "chatMessages: LazyPagingItems<com.example.data.local.entity.ChatMessageEntity>,")

# Update the LazyColumn rendering
old_items = """        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            reverseLayout = true
        ) {
            items(chatMessages.size) { index ->
                val msg = chatMessages[index]"""

new_items = """        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            reverseLayout = true
        ) {
            items(
                count = chatMessages.itemCount,
                key = chatMessages.itemKey { it.id },
                contentType = chatMessages.itemContentType { "chat_message" }
            ) { index ->
                val msg = chatMessages[index]
                if (msg != null) {"""

if old_items in content:
    content = content.replace(old_items, new_items)
    content = content.replace("            }\n        }", "                }\n            }\n        }")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
