import re
with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'r') as f:
    text = f.read()

# Fix biometricVault
text = re.sub(
    r'onResult = \{ success, err ->\s+if \(success\) \{\s+_uiState.value = _uiState.value.copy\(isMemoryVaultUnlocked = true, vaultAuthError = null\)\s+\} else \{\s+_uiState.value = _uiState.value.copy\(vaultAuthError = err\)\s+\}\s+\}',
    r'''onSuccess = {
                _uiState.value = _uiState.value.copy(isMemoryVaultUnlocked = true, vaultAuthError = null)
            },
            onError = { err ->
                _uiState.value = _uiState.value.copy(vaultAuthError = err)
            }''',
    text
)

# Fix voiceEngine
text = text.replace('voiceEngine.startListening()', 'voiceEngine.startListening { text -> setInputText(text) }')

with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'w') as f:
    f.write(text)

