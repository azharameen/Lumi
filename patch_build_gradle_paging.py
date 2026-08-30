import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "libs.androidx.paging.runtime.ktx" not in content:
    deps_block = """
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.paging)
"""
    content = content.replace("dependencies {", "dependencies {" + deps_block)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
