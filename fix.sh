#!/bin/bash
# Remove one of the duplicate handleLifeHubAction lines
sed -i '0,/val handleLifeHubAction/!b;//d' app/src/main/java/com/example/MainActivity.kt

# Add missing imports below package com.example
sed -i '/package com.example/a import com.example.data.local.entity.*\nimport androidx.compose.ui.graphics.Color' app/src/main/java/com/example/MainActivity.kt

# Add OptIn for ExperimentalPermissionsApi above LumiApp
sed -i '/@Composable/!b;n;/fun LumiApp/i @OptIn(ExperimentalPermissionsApi::class)' app/src/main/java/com/example/MainActivity.kt

