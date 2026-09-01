sed -i 's/val context = LocalContext.current/val context = LocalContext.current\n    val haptics = com.example.core.utils.rememberLumiHaptics(isEnabled = userProfile.enableHapticFeedback)/' app/src/main/java/com/example/presentation/home/HomeScreen.kt
sed -i 's/onPetTouched()/onPetTouched()\n                        haptics.performTick()/' app/src/main/java/com/example/presentation/home/HomeScreen.kt
sed -i 's/onPetPetted()/onPetPetted()\n                        haptics.performSuccess()/' app/src/main/java/com/example/presentation/home/HomeScreen.kt
sed -i 's/onFeedPet()/onFeedPet()\n                        haptics.performHeavyClick()/' app/src/main/java/com/example/presentation/home/HomeScreen.kt
sed -i 's/onDancePet()/onDancePet()\n                        haptics.performSuccess()/' app/src/main/java/com/example/presentation/home/HomeScreen.kt
sed -i 's/onPokePet()/onPokePet()\n                        haptics.performTick()/' app/src/main/java/com/example/presentation/home/HomeScreen.kt
