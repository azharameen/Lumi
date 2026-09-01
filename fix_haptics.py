import re

with open('app/src/main/java/com/example/presentation/home/components/PetActionControls.kt', 'r') as f:
    text = f.read()

target = """fun PetMiniActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, tooltip: String) {"""
replacement = """fun PetMiniActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    onClick: () -> Unit, 
    tooltip: String,
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics()
) {"""
text = text.replace(target, replacement)

target2 = """.clickable { onClick() }"""
replacement2 = """.clickable { 
                haptics.performTick()
                onClick() 
            }"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/home/components/PetActionControls.kt', 'w') as f:
    f.write(text)
