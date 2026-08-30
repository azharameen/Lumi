import re

with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

if "paging = " not in content:
    content = content.replace("[versions]", "[versions]\npaging = \"3.2.1\"")
    
if "androidx-paging-runtime-ktx" not in content:
    content = content.replace("[libraries]", "[libraries]\nandroidx-paging-runtime-ktx = { group = \"androidx.paging\", name = \"paging-runtime-ktx\", version.ref = \"paging\" }\nandroidx-paging-compose = { group = \"androidx.paging\", name = \"paging-compose\", version.ref = \"paging\" }\nandroidx-room-paging = { group = \"androidx.room\", name = \"room-paging\", version.ref = \"room\" }")

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

