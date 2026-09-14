import re
with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'r') as f:
    text = f.read()

text = text.replace('return@withContext responseMsg\n', 'return@withContext responseMsg.toDomain()\n')
text = text.replace('return@withContext responseMsg}', 'return@withContext responseMsg.toDomain()}')

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'w') as f:
    f.write(text)
