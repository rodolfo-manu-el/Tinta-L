package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

private data class InlineMatch(
    val range: IntRange,
    val innerText: String,
    val isBold: Boolean
)

class MarkdownWysiwygVisualTransformation(
    private val fontFamily: FontFamily,
    private val isDarkTheme: Boolean
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val origLen = originalText.length
        val origToTrans = IntArray(origLen + 1)
        val transToOrigList = mutableListOf<Int>()

        var origIndex = 0
        var transIndex = 0

        fun copyChar(c: Char) {
            origToTrans[origIndex] = transIndex
            transToOrigList.add(origIndex)
            origIndex++
            transIndex++
        }

        fun skipOrigChar() {
            origToTrans[origIndex] = transIndex
            origIndex++
        }

        fun insertTransChar() {
            transToOrigList.add(origIndex)
            transIndex++
        }

        val headerColor = if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFBC4B1E)
        val quoteColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF4A4B4F)

        val boldRegex = Regex("\\*\\*([^*]+)\\*\\*")
        val italicRegex = Regex("(?<!\\*)\\*([^*]+)\\*(?!\\*)")

        val transformed = buildAnnotatedString {
            val lines = originalText.split("\n")
            lines.forEachIndexed { lineIdx, line ->
                val lineStartInTrans = transIndex

                var lineContent = line
                var isHeader = false
                var isQuote = false

                if (lineContent.startsWith("### ")) {
                    isHeader = true
                    skipOrigChar()
                    skipOrigChar()
                    skipOrigChar()
                    skipOrigChar()
                    lineContent = lineContent.substring(4)
                } else if (lineContent.startsWith("## ")) {
                    isHeader = true
                    skipOrigChar()
                    skipOrigChar()
                    skipOrigChar()
                    lineContent = lineContent.substring(3)
                } else if (lineContent.startsWith("# ")) {
                    isHeader = true
                    skipOrigChar()
                    skipOrigChar()
                    lineContent = lineContent.substring(2)
                } else if (lineContent.startsWith("> ")) {
                    isQuote = true
                    skipOrigChar()
                    skipOrigChar()
                    lineContent = lineContent.substring(2)
                } else if (lineContent.startsWith("- ")) {
                    skipOrigChar()
                    skipOrigChar()
                    append("• ")
                    insertTransChar()
                    insertTransChar()
                    lineContent = lineContent.substring(2)
                }

                val matches = mutableListOf<InlineMatch>()

                boldRegex.findAll(lineContent).forEach { match ->
                    matches.add(
                        InlineMatch(
                            range = match.range,
                            innerText = match.groupValues[1],
                            isBold = true
                        )
                    )
                }

                italicRegex.findAll(lineContent).forEach { match ->
                    val overlaps = matches.any { it.range.first <= match.range.last && match.range.first <= it.range.last }
                    if (!overlaps) {
                        matches.add(
                            InlineMatch(
                                range = match.range,
                                innerText = match.groupValues[1],
                                isBold = false
                            )
                        )
                    }
                }

                matches.sortBy { it.range.first }

                var currIndexInLine = 0
                for (match in matches) {
                    while (currIndexInLine < match.range.first) {
                        val c = lineContent[currIndexInLine]
                        append(c)
                        copyChar(c)
                        currIndexInLine++
                    }

                    if (match.isBold) {
                        skipOrigChar()
                        skipOrigChar()

                        val matchStartInTrans = transIndex
                        for (c in match.innerText) {
                            append(c)
                            copyChar(c)
                        }
                        val matchEndInTrans = transIndex

                        addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold),
                            matchStartInTrans,
                            matchEndInTrans
                        )

                        skipOrigChar()
                        skipOrigChar()
                    } else {
                        skipOrigChar()

                        val matchStartInTrans = transIndex
                        for (c in match.innerText) {
                            append(c)
                            copyChar(c)
                        }
                        val matchEndInTrans = transIndex

                        addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic),
                            matchStartInTrans,
                            matchEndInTrans
                        )

                        skipOrigChar()
                    }

                    currIndexInLine = match.range.last + 1
                }

                while (currIndexInLine < lineContent.length) {
                    val c = lineContent[currIndexInLine]
                    append(c)
                    copyChar(c)
                    currIndexInLine++
                }

                val lineEndInTrans = transIndex

                if (isHeader) {
                    addStyle(
                        SpanStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        ),
                        lineStartInTrans,
                        lineEndInTrans
                    )
                } else if (isQuote) {
                    addStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = quoteColor
                        ),
                        lineStartInTrans,
                        lineEndInTrans
                    )
                }

                if (lineIdx < lines.size - 1) {
                    append('\n')
                    copyChar('\n')
                }
            }
        }

        origToTrans[origLen] = transIndex
        transToOrigList.add(origLen)

        val transToOrig = transToOrigList.toIntArray()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, origLen)
                return origToTrans[clamped].coerceIn(0, transIndex)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transIndex)
                return transToOrig[clamped].coerceIn(0, origLen)
            }
        }

        return TransformedText(transformed, offsetMapping)
    }
}
