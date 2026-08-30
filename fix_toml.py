with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

content = content.replace('version.ref = "room"', 'version.ref = "roomRuntime"')

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
