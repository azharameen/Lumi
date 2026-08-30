with open('app/src/main/java/com/example/ui/screens/WardrobeScreen.kt', 'r') as f:
    text = f.read()

import re

# Add header back
text = re.sub(
    r'// Header\s*item \{',
    r'''// Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.IconButton(onClick = onClose) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Morphing Studio & Evolution",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Customize 3D shapes, clay skin palettes & review Lumi's memory bank",
                color = LumiViolet,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 56.dp)
            )
        }
        
        // Live Pet Showcase
        item {''',
    text, flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/WardrobeScreen.kt', 'w') as f:
    f.write(text)

