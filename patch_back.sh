sed -i 's/val context = LocalContext.current/val context = LocalContext.current\n    val haptics = com.example.core.utils.rememberLumiHaptics(isEnabled = userProfile.enableHapticFeedback)/' app/src/main/java/com/example/MainActivity.kt
sed -i 's/        ) {/        ) {\n            haptics.performTick()/' app/src/main/java/com/example/MainActivity.kt
