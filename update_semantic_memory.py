import re

file_path = 'app/src/main/java/com/example/domain/memory/SemanticMemoryEngine.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'val content = "${fact.predicate} ${fact.objectValue}"',
    'val content = "${fact.factKey} ${fact.factValue}"'
)

content = content.replace(
    'append("• ${fact.predicate}: ${fact.objectValue}\\n")',
    'append("• ${fact.factKey}: ${fact.factValue}\\n")'
)

with open(file_path, 'w') as f:
    f.write(content)

