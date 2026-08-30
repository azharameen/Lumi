with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "r") as f:
    content = f.read()

content = content.replace("                        }\n                    }\n                }\n            }\n        }\n    }\n}\n", "                        }\n                    }\n                }\n                }\n            }\n        }\n    }\n}\n")

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "w") as f:
    f.write(content)
