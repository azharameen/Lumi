import re

with open('app/src/main/java/com/example/presentation/home/components/HomeScreenComponents.kt', 'r') as f:
    text = f.read()

target = """fun RemoteConfigSeasonalBanner(
    bannerText: String,
    seasonalThemeName: String = "",
    petPrimary: Color = LumiCyan,
    onClick: () -> Unit = {}
) {"""

replacement = """fun RemoteConfigSeasonalBanner(
    bannerText: String,
    seasonalThemeName: String = "",
    petPrimary: Color = LumiCyan,
    onClick: () -> Unit = {},
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics()
) {"""

text = text.replace(target, replacement)
with open('app/src/main/java/com/example/presentation/home/components/HomeScreenComponents.kt', 'w') as f:
    f.write(text)
