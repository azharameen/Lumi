import re

with open('app/src/main/java/com/example/presentation/home/HomeScreen.kt', 'r') as f:
    text = f.read()

text = text.replace(".clickable { onNavigateToLifeHub(0) }", ".clickable { \n                            haptics.performTick()\n                            onNavigateToLifeHub(0) \n                        }")
text = text.replace(".clickable { onNavigateToWellness() }", ".clickable { \n                            haptics.performTick()\n                            onNavigateToWellness() \n                        }")
text = text.replace(".clickable { onNavigateToLifeHub(1) }", ".clickable { \n                            haptics.performTick()\n                            onNavigateToLifeHub(1) \n                        }")

with open('app/src/main/java/com/example/presentation/home/HomeScreen.kt', 'w') as f:
    f.write(text)
