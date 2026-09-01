import re

with open('app/src/main/java/com/example/presentation/home/components/HomeScreenComponents.kt', 'r') as f:
    text = f.read()

target = """fun MinimalPetSpeechCard(
    petStatus: PetStatus,
    petPrimary: Color,
    onClick: () -> Unit
) {"""
replacement = """fun MinimalPetSpeechCard(
    petStatus: PetStatus,
    petPrimary: Color,
    onClick: () -> Unit,
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics()
) {"""
text = text.replace(target, replacement)

target2 = """modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }"""
replacement2 = """modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptics.performTick()
                onClick()
            }"""
text = text.replace(target2, replacement2)

with open('app/src/main/java/com/example/presentation/home/components/HomeScreenComponents.kt', 'w') as f:
    f.write(text)
