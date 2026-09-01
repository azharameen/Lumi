sed -i 's/fun ChatScreen(/fun ChatScreen(\n    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics(),/' app/src/main/java/com/example/presentation/screens/ChatScreen.kt
sed -i 's/onSendMessage(prompt)/{ haptics.performSuccess(); onSendMessage(prompt) }/' app/src/main/java/com/example/presentation/screens/ChatScreen.kt
sed -i 's/onSendMessage(text)/haptics.performSuccess()\n                            onSendMessage(text)/' app/src/main/java/com/example/presentation/screens/ChatScreen.kt
