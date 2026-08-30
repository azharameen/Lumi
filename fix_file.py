import sys

filename = sys.argv[1]
with open(filename, "r") as f:
    lines = f.readlines()

# find the last line that is not empty and not just '}' or '//...'
# Actually, the easiest way is to just delete line 305 and 308 from LifeHubScreen.kt
with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    for i, line in enumerate(lines):
        if (i+1) == 305 or (i+1) == 308:
            continue
        f.write(line)
