for file in app/src/main/java/com/example/presentation/home/components/SeamlessRpgPlayerHud.kt app/src/main/java/com/example/presentation/screens/WardrobeScreen.kt; do
  sed -i '1s/^/import androidx.compose.material3.Icon\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.MonetizationOn\nimport androidx.compose.material.icons.filled.Diamond\n/' "$file"
done
