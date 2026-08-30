with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

content = content.replace("LaunchedEffect(chatMessages.size) {", "LaunchedEffect(chatMessages.itemCount) {")
content = content.replace("if (chatMessages.isNotEmpty()) {", "if (chatMessages.itemCount > 0) {")
content = content.replace("listState.animateScrollToItem(chatMessages.size - 1)", "listState.animateScrollToItem(0) // Items are reversed, newest at 0")

old_items = "items(chatMessages) { msg ->"
if old_items in content:
    content = content.replace(old_items, """items(
                count = chatMessages.itemCount,
                key = chatMessages.itemKey { it.id },
                contentType = chatMessages.itemContentType { "chat_message" }
            ) { index ->
                val msg = chatMessages[index]
                if (msg != null) {""")
    content = content.replace("        }\n    }", "            }\n        }\n    }")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

if "import androidx.paging.compose.collectAsLazyPagingItems" not in content:
    content = content.replace("import androidx.compose.runtime.collectAsState", "import androidx.compose.runtime.collectAsState\nimport androidx.paging.compose.collectAsLazyPagingItems")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
