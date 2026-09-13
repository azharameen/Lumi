package com.example.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*

/**
 * Enterprise Jetpack Compose Markdown Renderer.
 * Parses and renders GitHub-flavored Markdown:
 * - Fenced code blocks with language tag and copy button
 * - Inline code with distinct monospace styling
 * - Headings (H1 - H4)
 * - Blockquotes with accent line
 * - Bullet lists and numbered lists
 * - Horizontal rules
 * - Bold, italic, strikethrough, and links
 * - Streaming cursor support
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false,
    isStreaming: Boolean = false,
    cursorAlpha: Float = 1f
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    val textColor = if (isUser) ObsidianDark else TextPrimary
    val codeBgColor = if (isUser) ObsidianDark.copy(alpha = 0.12f) else SurfaceDarkVariant
    val codeBlockBg = if (isUser) ObsidianDark.copy(alpha = 0.15f) else ObsidianDark.copy(alpha = 0.85f)
    val accentColor = if (isUser) ObsidianDark else LumiCyan

    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            val isLastBlock = index == blocks.lastIndex
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockCard(
                        code = block.code,
                        language = block.language,
                        bgColor = codeBlockBg,
                        textColor = if (isUser) ObsidianDark else TextPrimary,
                        accentColor = accentColor,
                        onCopy = {
                            clipboardManager?.setPrimaryClip(ClipData.newPlainText("Code", block.code))
                        }
                    )
                }
                is MarkdownBlock.Heading -> {
                    val (fontSize, fontWeight) = when (block.level) {
                        1 -> Pair(18.sp, FontWeight.Bold)
                        2 -> Pair(16.sp, FontWeight.Bold)
                        3 -> Pair(15.sp, FontWeight.SemiBold)
                        else -> Pair(14.sp, FontWeight.SemiBold)
                    }
                    Text(
                        text = parseInlineMarkdown(block.text, isUser, codeBgColor, accentColor),
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = textColor,
                        lineHeight = (fontSize.value + 6).sp
                    )
                }
                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(IntrinsicSize.Min)
                                .background(accentColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(block.text, isUser, codeBgColor, accentColor),
                            fontSize = 13.sp,
                            color = textColor.copy(alpha = 0.85f),
                            fontStyle = FontStyle.Italic,
                            lineHeight = 19.sp
                        )
                    }
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.depth * 12).dp, top = 1.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, isUser, codeBgColor, accentColor),
                            fontSize = 14.sp,
                            color = textColor,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 1.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            fontSize = 13.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, isUser, codeBgColor, accentColor),
                            fontSize = 14.sp,
                            color = textColor,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        color = textColor.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = parseInlineMarkdown(block.text, isUser, codeBgColor, accentColor),
                            fontSize = 14.sp,
                            color = textColor,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isStreaming && isLastBlock) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(7.dp)
                                    .height(16.dp)
                                    .background(LumiCyan.copy(alpha = cursorAlpha), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(
    code: String,
    language: String,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Header bar with language tag and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" }.lowercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onCopy()
                            copied = true
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) LumiGreen else TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (copied) "Copied" else "Copy",
                        fontSize = 10.sp,
                        color = if (copied) LumiGreen else TextTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Code content
            Text(
                text = code,
                fontSize = 12.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}

// Sealed hierarchy for parsed Markdown blocks
sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    data class BulletItem(val depth: Int, val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

/**
 * Parses raw markdown text into a sequence of structural blocks.
 */
fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var inCodeBlock = false
    var codeLang = ""
    val codeLines = mutableListOf<String>()

    for (line in lines) {
        if (line.trimStart().startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.CodeBlock(codeLang, codeLines.joinToString("\n")))
                codeLines.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLang = line.trimStart().removePrefix("```").trim()
            }
            continue
        }

        if (inCodeBlock) {
            codeLines.add(line)
            continue
        }

        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> continue
            trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                blocks.add(MarkdownBlock.Divider)
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Heading(1, trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Heading(2, trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Heading(3, trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("#### ") -> {
                blocks.add(MarkdownBlock.Heading(4, trimmed.removePrefix("#### ").trim()))
            }
            trimmed.startsWith("> ") -> {
                blocks.add(MarkdownBlock.Blockquote(trimmed.removePrefix("> ").trim()))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                val bulletText = trimmed.substring(2).trim()
                val indent = line.takeWhile { it == ' ' }.length / 2
                blocks.add(MarkdownBlock.BulletItem(indent, bulletText))
            }
            Regex("""^\d+\.\s+""").containsMatchIn(trimmed) -> {
                val match = Regex("""^(\d+)\.\s+(.*)""").find(trimmed)
                if (match != null) {
                    blocks.add(MarkdownBlock.NumberedItem(match.groupValues[1], match.groupValues[2]))
                } else {
                    blocks.add(MarkdownBlock.Paragraph(trimmed))
                }
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(trimmed))
            }
        }
    }

    if (inCodeBlock && codeLines.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLang, codeLines.joinToString("\n")))
    }

    return if (blocks.isEmpty()) listOf(MarkdownBlock.Paragraph(markdown)) else blocks
}

/**
 * Parses inline formatting: bold (**), italic (*), inline code (`), strikethrough (~~).
 */
fun parseInlineMarkdown(
    text: String,
    isUser: Boolean,
    codeBgColor: Color,
    accentColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length

        while (i < len) {
            // Inline code `...`
            if (text[i] == '`' && (i + 1 < len)) {
                val closeIndex = text.indexOf('`', i + 1)
                if (closeIndex != -1) {
                    val codeSnippet = text.substring(i + 1, closeIndex)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBgColor,
                            color = accentColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    append(" $codeSnippet ")
                    pop()
                    i = closeIndex + 1
                    continue
                }
            }

            // Bold + Italic ***...***
            if (i + 2 < len && text.substring(i, i + 3) == "***") {
                val closeIndex = text.indexOf("***", i + 3)
                if (closeIndex != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                    append(text.substring(i + 3, closeIndex))
                    pop()
                    i = closeIndex + 3
                    continue
                }
            }

            // Bold **...**
            if (i + 1 < len && text.substring(i, i + 2) == "**") {
                val closeIndex = text.indexOf("**", i + 2)
                if (closeIndex != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(i + 2, closeIndex))
                    pop()
                    i = closeIndex + 2
                    continue
                }
            }

            // Strikethrough ~~...~~
            if (i + 1 < len && text.substring(i, i + 2) == "~~") {
                val closeIndex = text.indexOf("~~", i + 2)
                if (closeIndex != -1) {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(text.substring(i + 2, closeIndex))
                    pop()
                    i = closeIndex + 2
                    continue
                }
            }

            // Italic *...*
            if (text[i] == '*') {
                val closeIndex = text.indexOf('*', i + 1)
                if (closeIndex != -1 && closeIndex > i + 1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(i + 1, closeIndex))
                    pop()
                    i = closeIndex + 1
                    continue
                }
            }

            append(text[i])
            i++
        }
    }
}
