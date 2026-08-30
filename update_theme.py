import re

with open("app/src/main/java/com/example/ui/theme/Theme.kt", "r") as f:
    content = f.read()

# Modify MyApplicationTheme to accept petColor
old_theme_func = """@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark companion mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme"""

new_theme_func = """import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark companion mode
    petColorPrimary: Color? = null,
    petColorSecondary: Color? = null,
    content: @Composable () -> Unit
) {
    val dynamicDark = if (petColorPrimary != null && petColorSecondary != null) {
        DarkColorScheme.copy(
            primary = petColorPrimary,
            onPrimaryContainer = petColorPrimary,
            secondary = petColorSecondary,
            onSecondaryContainer = petColorSecondary,
            tertiary = petColorPrimary
        )
    } else DarkColorScheme

    val colorScheme = if (darkTheme) dynamicDark else LightColorScheme"""

if "petColorPrimary: Color? = null" not in content:
    content = content.replace(old_theme_func, new_theme_func)

with open("app/src/main/java/com/example/ui/theme/Theme.kt", "w") as f:
    f.write(content)
