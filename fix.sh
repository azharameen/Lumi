# Remove the bad imports at line 1
sed -i '1,4d' app/src/main/java/com/example/presentation/home/components/SeamlessRpgPlayerHud.kt
sed -i '1,4d' app/src/main/java/com/example/presentation/screens/WardrobeScreen.kt

# Insert them after the package declaration
sed -i 's/^package .*/&\nimport androidx.compose.material3.Icon\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.MonetizationOn\nimport androidx.compose.material.icons.filled.Diamond/' app/src/main/java/com/example/presentation/home/components/SeamlessRpgPlayerHud.kt
sed -i 's/^package .*/&\nimport androidx.compose.material3.Icon\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.MonetizationOn\nimport androidx.compose.material.icons.filled.Diamond/' app/src/main/java/com/example/presentation/screens/WardrobeScreen.kt
