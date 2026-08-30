import re

with open("app/src/main/java/com/example/data/remote/GeminiAgentEngine.kt", "r") as f:
    content = f.read()

fallback_func = r"private suspend fun executeLocalFallback\([\s\S]*?\}\s*\}"

new_fallback_func = """private suspend fun executeLocalFallback(
        userMessage: String,
        imageAttachment: Bitmap?
    ): AgentExecutionResult {
        return AgentExecutionResult(
            "I need a Gemini API Key to process your request and use my tools dynamically! Please add it in the Settings screen.",
            PetEmotion.CONCERNED,
            emptyList()
        )
    }"""

content = re.sub(fallback_func, new_fallback_func, content)

with open("app/src/main/java/com/example/data/remote/GeminiAgentEngine.kt", "w") as f:
    f.write(content)

