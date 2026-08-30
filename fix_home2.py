import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Remove the .align(Alignment.BottomCenter) from the Context & Bottom Dock Column, since it's already inside a Box or Column
# Wait, if it's inside the Pet Column, we should just let it flow naturally, or we should break it out into the main Box.
# Let's break it out into the main Box so it stays anchored at the bottom.

old_dock = r"// Context & Bottom Dock(.*?)Column\(\s*modifier = Modifier\s*\.align\(Alignment\.BottomCenter\)\s*\.fillMaxWidth\(\)\s*\.padding\(bottom = 24\.dp, start = 16\.dp, end = 16\.dp\),(.*?)\) \{"

# The Pet Column ends after `Spacer(modifier = Modifier.height(24.dp))`
# So we need to close the Pet Box, then start the Bottom Dock Box.

old_block = r"""                Spacer\(modifier = Modifier\.height\(24\.dp\)\)
                
                // Context & Bottom Dock"""

new_block = """                Spacer(modifier = Modifier.height(24.dp))
            }
        } // End of Center Pet Box
        
        // Context & Bottom Dock"""

content = re.sub(old_block, new_block, content)

# Check braces. By adding "} }", we've closed the Pet Box.
# Now the Dock is at the root Box, so `.align(Alignment.BottomCenter)` WILL work!

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
