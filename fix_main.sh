sed -i 's/            haptics.performTick() innerPadding ->/            innerPadding ->/' app/src/main/java/com/example/MainActivity.kt
sed -i 's/            haptics.performTick()//' app/src/main/java/com/example/MainActivity.kt
sed -i 's/                targetState = uiState.selectedTab/                targetState = uiState.selectedTab/' app/src/main/java/com/example/MainActivity.kt
sed -i 's/            haptics.performTick() tab ->/            tab ->/' app/src/main/java/com/example/MainActivity.kt
