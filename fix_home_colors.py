import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace the radial gradient colors
old_radial = """                Brush.radialGradient(
                    colors = listOf(
                        when (petStatus.currentEmotion) {
                            PetEmotion.HAPPY, PetEmotion.ENERGETIC -> LumiCyan.copy(alpha = 0.2f)
                            PetEmotion.LOVING -> LumiPink.copy(alpha = 0.2f)
                            PetEmotion.CALM, PetEmotion.SLEEPY -> LumiMint.copy(alpha = 0.2f)
                            PetEmotion.CONCERNED -> LumiGold.copy(alpha = 0.2f)
                            PetEmotion.THINKING -> LumiViolet.copy(alpha = 0.2f)
                            else -> LumiCyan.copy(alpha = 0.2f)
                        },
                        Color(0xFF16161E),
                        ObsidianDark
                    ),
                    radius = 1200f
                )"""

new_radial = """                Brush.radialGradient(
                    colors = listOf(
                        Color(petStatus.bloubSkinColor.primaryHex).copy(alpha = 0.25f),
                        Color(0xFF16161E),
                        ObsidianDark
                    ),
                    radius = 1200f
                )"""
content = content.replace(old_radial, new_radial)

# Level color
old_level = """                            Text(
                                text = "Lvl ${petStatus.level}", 
                                color = LumiCyan, 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Black
                            )"""
new_level = """                            Text(
                                text = "Lvl ${petStatus.level}", 
                                color = Color(petStatus.bloubSkinColor.primaryHex), 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Black
                            )"""
content = content.replace(old_level, new_level)

# Chat button gradient
old_chat = "brush = Brush.linearGradient(listOf(LumiCyan, LumiViolet))"
new_chat = "brush = Brush.linearGradient(listOf(Color(petStatus.bloubSkinColor.primaryHex), Color(petStatus.bloubSkinColor.endHex)))"
content = content.replace(old_chat, new_chat)

# Icon color in speech bubble
old_speech = """                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LumiPink,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )"""
new_speech = """                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(petStatus.bloubSkinColor.primaryHex),
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )"""
content = content.replace(old_speech, new_speech)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
