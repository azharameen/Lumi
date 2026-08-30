with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

if "profileinstaller = " not in content:
    content = content.replace("[versions]", "[versions]\nprofileinstaller = \"1.4.1\"")
    content = content.replace("[libraries]", "[libraries]\nandroidx-profileinstaller = { group = \"androidx.profileinstaller\", name = \"profileinstaller\", version.ref = \"profileinstaller\" }")
    
    with open("gradle/libs.versions.toml", "w") as f:
        f.write(content)

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "libs.androidx.profileinstaller" not in content:
    content = content.replace("dependencies {", "dependencies {\n    implementation(libs.androidx.profileinstaller)")
    with open("app/build.gradle.kts", "w") as f:
        f.write(content)
