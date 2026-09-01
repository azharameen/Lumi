awk 'NR==297 {print "                                    }"}
NR==300 {print "                                }"}
NR!=297 && NR!=300 {print}' app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt > temp2.kt
mv temp2.kt app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt
