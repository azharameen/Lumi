with open('app/src/main/java/com/example/domain/prompt/DynamicPromptSuggester.kt', 'r') as f:
    text = f.read()

text = text.replace('import com.example.data.local.entity.ChatMessageEntity', 'import com.example.domain.model.ChatMessage')
text = text.replace('ChatMessageEntity', 'ChatMessage')

with open('app/src/main/java/com/example/domain/prompt/DynamicPromptSuggester.kt', 'w') as f:
    f.write(text)
