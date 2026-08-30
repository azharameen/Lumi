with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'r') as f:
    text = f.read()

import re
text = re.sub(r'onSuccess = \{.*?\n.*?\},.*?\n.*?onError = \{ err ->\n.*?\}', 
r'''onResult = { success, err ->
                if (success) {
                    _uiState.value = _uiState.value.copy(isMemoryVaultUnlocked = true, vaultAuthError = null)
                } else {
                    _uiState.value = _uiState.value.copy(vaultAuthError = err)
                }
            }''', text, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'w') as f:
    f.write(text)

