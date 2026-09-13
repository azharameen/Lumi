package com.example.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.*
import org.commonmark.parser.Parser

/**
 * Enterprise Jetpack Compose Markdown Renderer.
 * Parses and renders Markdown using the official CommonMark parser specification:
 * - Fenced & Indented code blocks with language tag and copy button
 * - Inline code with distinct monospace styling
 * - Headings (H1 - H6)
 * - Blockquotes with accent line
 * - Bullet lists and numbered (ordered) lists with nesting
 * - Horizontal rules (Thematic breaks)
 * - Bold, italic, strikethrough (GFM), and links
 * - GFM Markdown Tables
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
                        text = buildAnnotatedStringFromNode(block.node, codeBgColor, accentColor),
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
                            text = buildAnnotatedStringFromNode(block.node, codeBgColor, accentColor),
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
                            text = buildAnnotatedStringFromNode(block.node, codeBgColor, accentColor),
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
                            .padding(start = (block.depth * 12).dp, top = 1.dp, bottom = 1.dp),
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
                            text = buildAnnotatedStringFromNode(block.node, codeBgColor, accentColor),
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
                is MarkdownBlock.Table -> {
                    TableCard(
                        table = block,
                        textColor = textColor,
                        codeBgColor = codeBgColor,
                        accentColor = accentColor
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = buildAnnotatedStringFromNode(block.node, codeBgColor, accentColor),
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

@Composable
private fun TableCard(
    table: MarkdownBlock.Table,
    textColor: Color,
    codeBgColor: Color,
    accentColor: Color
) {
    val scrollState = rememberScrollState()

    Surface(
        color = ObsidianDark.copy(alpha = 0.6f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.horizontalScroll(scrollState)) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Table Header
                if (table.headers.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        table.headers.forEach { headerNode ->
                            Text(
                                text = buildAnnotatedStringFromNode(headerNode, codeBgColor, accentColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier
                                    .widthIn(min = 90.dp, max = 220.dp)
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        color = accentColor.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Table Rows
                table.rows.forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .background(
                                if (rowIndex % 2 == 1) ObsidianDark.copy(alpha = 0.3f) else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        row.forEach { cellNode ->
                            Text(
                                text = buildAnnotatedStringFromNode(cellNode, codeBgColor, accentColor),
                                fontSize = 12.sp,
                                color = textColor,
                                modifier = Modifier
                                    .widthIn(min = 90.dp, max = 220.dp)
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sealed hierarchy for parsed Markdown blocks
sealed class MarkdownBlock {
    data class Heading(val level: Int, val node: Node) : MarkdownBlock()
    data class Paragraph(val node: Node) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Blockquote(val node: Node) : MarkdownBlock()
    data class BulletItem(val depth: Int, val node: Node) : MarkdownBlock()
    data class NumberedItem(val depth: Int, val number: Int, val node: Node) : MarkdownBlock()
    data class Table(val headers: List<Node>, val rows: List<List<Node>>) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

/**
 * Singleton CommonMark parser configured with standard GFM extensions:
 * - StrikethroughExtension (~~text~~)
 * - TablesExtension (| col | col |)
 */
private val commonMarkParser: Parser by lazy {
    val extensions = listOf(
        StrikethroughExtension.create(),
        TablesExtension.create()
    )
    Parser.builder()
        .extensions(extensions)
        .build()
}

/**
 * Parses raw markdown text into a sequence of structural blocks using CommonMark AST.
 * Eliminates custom regex and manual substring slicing.
 */
fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    if (markdown.isBlank()) return emptyList()

    val document = commonMarkParser.parse(markdown)
    val blocks = mutableListOf<MarkdownBlock>()

    var child: Node? = document.firstChild
    while (child != null) {
        when (val node = child) {
            is FencedCodeBlock -> {
                blocks.add(MarkdownBlock.CodeBlock(node.info ?: "", node.literal.trimEnd('\n')))
            }
            is IndentedCodeBlock -> {
                blocks.add(MarkdownBlock.CodeBlock("", node.literal.trimEnd('\n')))
            }
            is org.commonmark.node.Heading -> {
                blocks.add(MarkdownBlock.Heading(node.level, node))
            }
            is BlockQuote -> {
                blocks.add(MarkdownBlock.Blockquote(node))
            }
            is BulletList -> {
                flattenBulletList(node, depth = 0, blocks = blocks)
            }
            is OrderedList -> {
                flattenOrderedList(node, depth = 0, blocks = blocks)
            }
            is ThematicBreak -> {
                blocks.add(MarkdownBlock.Divider)
            }
            is TableBlock -> {
                val headers = mutableListOf<Node>()
                val rows = mutableListOf<List<Node>>()

                var tableChild: Node? = node.firstChild
                while (tableChild != null) {
                    when (tableChild) {
                        is TableHead -> {
                            var headRow: Node? = tableChild.firstChild
                            while (headRow != null) {
                                if (headRow is TableRow) {
                                    var cell: Node? = headRow.firstChild
                                    while (cell != null) {
                                        if (cell is TableCell) headers.add(cell)
                                        cell = cell.next
                                    }
                                }
                                headRow = headRow.next
                            }
                        }
                        is TableBody -> {
                            var bodyRow: Node? = tableChild.firstChild
                            while (bodyRow != null) {
                                if (bodyRow is TableRow) {
                                    val rowCells = mutableListOf<Node>()
                                    var cell: Node? = bodyRow.firstChild
                                    while (cell != null) {
                                        if (cell is TableCell) rowCells.add(cell)
                                        cell = cell.next
                                    }
                                    rows.add(rowCells)
                                }
                                bodyRow = bodyRow.next
                            }
                        }
                    }
                    tableChild = tableChild.next
                }
                blocks.add(MarkdownBlock.Table(headers, rows))
            }
            is org.commonmark.node.Paragraph -> {
                blocks.add(MarkdownBlock.Paragraph(node))
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(node))
            }
        }
        child = child.next
    }

    return blocks
}

private fun flattenBulletList(listNode: BulletList, depth: Int, blocks: MutableList<MarkdownBlock>) {
    var item: Node? = listNode.firstChild
    while (item != null) {
        if (item is ListItem) {
            var itemChild: Node? = item.firstChild
            while (itemChild != null) {
                when (itemChild) {
                    is BulletList -> flattenBulletList(itemChild, depth + 1, blocks)
                    is OrderedList -> flattenOrderedList(itemChild, depth + 1, blocks)
                    else -> blocks.add(MarkdownBlock.BulletItem(depth, itemChild))
                }
                itemChild = itemChild.next
            }
        }
        item = item.next
    }
}

private fun flattenOrderedList(listNode: OrderedList, depth: Int, blocks: MutableList<MarkdownBlock>) {
    var num = listNode.markerStartNumber ?: 1
    var item: Node? = listNode.firstChild
    while (item != null) {
        if (item is ListItem) {
            var itemChild: Node? = item.firstChild
            while (itemChild != null) {
                when (itemChild) {
                    is BulletList -> flattenBulletList(itemChild, depth + 1, blocks)
                    is OrderedList -> flattenOrderedList(itemChild, depth + 1, blocks)
                    else -> blocks.add(MarkdownBlock.NumberedItem(depth, num, itemChild))
                }
                itemChild = itemChild.next
            }
            num++
        }
        item = item.next
    }
}

/**
 * Traverses a CommonMark AST node recursively and produces a Jetpack Compose AnnotatedString.
 * Uses visitor/traversal logic over the official CommonMark AST node graph.
 * Handles Bold, Italic, Strikethrough, Code, Links, LineBreaks, and Text properly
 * according to the CommonMark specification without regex or string slicing.
 */
fun buildAnnotatedStringFromNode(
    node: Node,
    codeBgColor: Color,
    accentColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        appendNode(node, codeBgColor, accentColor)
    }
}

/**
 * Extracts plain text from any Markdown string using the pre-built CommonMark parser AST.
 * Used for screen readers, TTS voice output, analytics, etc., completely eliminating
 * any regex replacements like `replace(Regex("[*#_`~]"), "")`.
 */
fun extractPlainTextFromMarkdown(markdown: String): String {
    if (markdown.isBlank()) return ""
    val document = commonMarkParser.parse(markdown)
    val sb = StringBuilder()
    document.accept(object : AbstractVisitor() {
        override fun visit(text: Text) {
            sb.append(text.literal)
        }
        override fun visit(code: org.commonmark.node.Code) {
            sb.append(" ").append(code.literal).append(" ")
        }
        override fun visit(softLineBreak: SoftLineBreak) {
            sb.append(" ")
        }
        override fun visit(hardLineBreak: HardLineBreak) {
            sb.append("\n")
        }
        override fun visit(paragraph: org.commonmark.node.Paragraph) {
            super.visit(paragraph)
            sb.append("\n")
        }
        override fun visit(blockQuote: BlockQuote) {
            super.visit(blockQuote)
            sb.append("\n")
        }
    })
    return sb.toString().trim()
}

private fun AnnotatedString.Builder.appendNode(
    node: Node,
    codeBgColor: Color,
    accentColor: Color
) {
    when (node) {
        is Text -> {
            append(node.literal)
            return
        }
        is org.commonmark.node.Code -> {
            pushStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = codeBgColor,
                    color = accentColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            append(" ${node.literal} ")
            pop()
            return
        }
        is SoftLineBreak -> {
            append(" ")
            return
        }
        is HardLineBreak -> {
            append("\n")
            return
        }
    }

    var child: Node? = node.firstChild
    while (child != null) {
        when (child) {
            is StrongEmphasis -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendNode(child, codeBgColor, accentColor)
                pop()
            }
            is Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendNode(child, codeBgColor, accentColor)
                pop()
            }
            is Strikethrough -> {
                pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                appendNode(child, codeBgColor, accentColor)
                pop()
            }
            is org.commonmark.node.Code -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBgColor,
                        color = accentColor,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                append(" ${child.literal} ")
                pop()
            }
            is Link -> {
                pushStyle(
                    SpanStyle(
                        color = accentColor,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                )
                appendNode(child, codeBgColor, accentColor)
                pop()
            }
            is Text -> {
                append(child.literal)
            }
            is SoftLineBreak -> {
                append(" ")
            }
            is HardLineBreak -> {
                append("\n")
            }
            else -> {
                appendNode(child, codeBgColor, accentColor)
            }
        }
        child = child.next
    }
}
