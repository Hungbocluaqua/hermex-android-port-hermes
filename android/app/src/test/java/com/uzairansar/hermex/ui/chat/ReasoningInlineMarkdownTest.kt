package com.uzairansar.hermex.ui.chat

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReasoningInlineMarkdownTest {
    @Test
    fun formatsCommonInlineMarkdownWithoutChangingVisibleWords() {
        val rendered = reasoningInlineMarkdown("Use **bold**, *italic*, `code`, and ~~old~~.")

        assertEquals("Use bold, italic, code, and old.", rendered.text)
        assertTrue(rendered.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(rendered.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
        assertTrue(rendered.spanStyles.any { it.item.fontFamily == FontFamily.Monospace })
        assertTrue(rendered.spanStyles.any { it.item.textDecoration == TextDecoration.LineThrough })
    }

    @Test
    fun leavesUnmatchedMarkersAndLineBreaksAlone() {
        val source = "unfinished **bold\nnext line"

        assertEquals(source, reasoningInlineMarkdown(source).text)
    }
}
