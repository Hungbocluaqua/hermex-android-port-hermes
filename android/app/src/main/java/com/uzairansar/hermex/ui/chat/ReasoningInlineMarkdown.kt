package com.uzairansar.hermex.ui.chat

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/** Small inline-only Markdown formatter for the compact Thinking accessory card. */
internal fun reasoningInlineMarkdown(source: String): AnnotatedString = buildAnnotatedString {
    val token = Regex("\\*\\*(.+?)\\*\\*|__(.+?)__|(?<!\\*)\\*([^*\\n]+?)\\*(?!\\*)|(?<!_)_([^_\\n]+?)_(?!_)|~~(.+?)~~|`([^`]+?)`")
    var cursor = 0
    token.findAll(source).forEach { match ->
        append(source.substring(cursor, match.range.first))
        val group = (1..6).first { match.groups[it] != null }
        val content = match.groups[group]!!.value
        val style = when (group) {
            1, 2 -> SpanStyle(fontWeight = FontWeight.Bold)
            3, 4 -> SpanStyle(fontStyle = FontStyle.Italic)
            5 -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            else -> SpanStyle(fontFamily = FontFamily.Monospace)
        }
        pushStyle(style)
        append(content)
        pop()
        cursor = match.range.last + 1
    }
    append(source.substring(cursor))
}
