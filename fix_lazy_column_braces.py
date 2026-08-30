with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

old_str = """                if (msg != null) {
                ChatMessageBubble(message = msg)
            }
        }

        // Realtime waveform if voice active"""

new_str = """                if (msg != null) {
                    ChatMessageBubble(message = msg)
                }
            }
        }

        // Realtime waveform if voice active"""

content = content.replace(old_str, new_str)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
