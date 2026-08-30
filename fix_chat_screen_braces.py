with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

content = content.replace("""    LaunchedEffect(chatMessages.itemCount) {
        if (chatMessages.itemCount > 0) {
            listState.animateScrollToItem(0) // Items are reversed, newest at 0
            }
        }
    }""", """    LaunchedEffect(chatMessages.itemCount) {
        if (chatMessages.itemCount > 0) {
            listState.animateScrollToItem(0) // Items are reversed, newest at 0
        }
    }""")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
