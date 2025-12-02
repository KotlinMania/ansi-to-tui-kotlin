package ansi_to_tui

import ratatui.style.Color
import ratatui.style.Modifier
import ratatui.style.Style
import ratatui.text.Line
import ratatui.text.Span
import ratatui.text.Text

/**
 * Color type for extended colors.
 */
private enum class ColorType {
    /** Eight bit color (256 color palette) */
    EightBit,
    /** 24-bit color or true color (RGB) */
    TrueColor
}

/**
 * An ANSI item with code and optional color.
 */
private data class AnsiItem(
    val code: AnsiCode,
    val color: Color? = null
)

/**
 * ANSI state accumulator.
 */
private data class AnsiStates(
    val items: MutableList<AnsiItem> = mutableListOf(),
    val style: Style = Style.default()
) {
    /**
     * Convert accumulated items to a Style.
     */
    fun toStyle(): Style {
        var result = style
        if (items.isEmpty()) {
            // [m should be treated as a reset as well
            return Style.reset()
        }
        for (item in items) {
            result = when (item.code) {
                is AnsiCode.Reset -> Style.reset()
                is AnsiCode.Bold -> result.addModifier(Modifier.BOLD)
                is AnsiCode.Faint -> result.addModifier(Modifier.DIM)
                is AnsiCode.Normal -> result.removeModifier(Modifier.BOLD or Modifier.DIM)
                is AnsiCode.Italic -> result.addModifier(Modifier.ITALIC)
                is AnsiCode.NotItalic -> result.removeModifier(Modifier.ITALIC)
                is AnsiCode.Underline -> result.addModifier(Modifier.UNDERLINED)
                is AnsiCode.UnderlineOff -> result.removeModifier(Modifier.UNDERLINED)
                is AnsiCode.SlowBlink -> result.addModifier(Modifier.SLOW_BLINK)
                is AnsiCode.RapidBlink -> result.addModifier(Modifier.RAPID_BLINK)
                is AnsiCode.BlinkOff -> result.removeModifier(Modifier.SLOW_BLINK or Modifier.RAPID_BLINK)
                is AnsiCode.Reverse -> result.addModifier(Modifier.REVERSED)
                is AnsiCode.Conceal -> result.addModifier(Modifier.HIDDEN)
                is AnsiCode.Reveal -> result.removeModifier(Modifier.HIDDEN)
                is AnsiCode.CrossedOut -> result.addModifier(Modifier.CROSSED_OUT)
                is AnsiCode.CrossedOutOff -> result.removeModifier(Modifier.CROSSED_OUT)
                is AnsiCode.DefaultForegroundColor -> result.fg(Color.Reset)
                is AnsiCode.DefaultBackgroundColor -> result.bg(Color.Reset)
                is AnsiCode.SetForegroundColor -> {
                    item.color?.let { result.fg(it) } ?: result
                }
                is AnsiCode.SetBackgroundColor -> {
                    item.color?.let { result.bg(it) } ?: result
                }
                is AnsiCode.ForegroundColor -> result.fg(item.code.color)
                is AnsiCode.BackgroundColor -> result.bg(item.code.color)
                else -> result
            }
        }
        return result
    }
}

/**
 * Parser state for tracking position in byte array.
 */
private class Parser(private val data: ByteArray) {
    var pos: Int = 0

    val remaining: Int get() = data.size - pos
    val isAtEnd: Boolean get() = pos >= data.size

    fun peek(): Byte? = if (pos < data.size) data[pos] else null
    fun peekChar(): Char? = peek()?.toInt()?.toChar()

    fun advance(): Byte? {
        return if (pos < data.size) data[pos++] else null
    }

    fun advanceChar(): Char? = advance()?.toInt()?.toChar()

    fun slice(start: Int, end: Int): ByteArray = data.sliceArray(start until end)

    fun takeWhile(predicate: (Byte) -> Boolean): ByteArray {
        val start = pos
        while (pos < data.size && predicate(data[pos])) {
            pos++
        }
        return slice(start, pos)
    }

    fun takeUntil(predicate: (Byte) -> Boolean): ByteArray {
        val start = pos
        while (pos < data.size && !predicate(data[pos])) {
            pos++
        }
        return slice(start, pos)
    }

    fun expect(b: Byte): Boolean {
        if (peek() == b) {
            advance()
            return true
        }
        return false
    }

    fun expectChar(c: Char): Boolean = expect(c.code.toByte())

    fun expectTag(tag: String): Boolean {
        if (pos + tag.length > data.size) return false
        for (i in tag.indices) {
            if (data[pos + i] != tag[i].code.toByte()) return false
        }
        pos += tag.length
        return true
    }

    fun parseU8(): UByte? {
        val start = pos
        while (pos < data.size && data[pos] in '0'.code.toByte()..'9'.code.toByte()) {
            pos++
        }
        if (start == pos) return null
        val str = slice(start, pos).decodeToString()
        return str.toUByteOrNull()
    }

    fun parseInt(): Int? {
        val start = pos
        if (pos < data.size && data[pos] == '-'.code.toByte()) pos++
        while (pos < data.size && data[pos] in '0'.code.toByte()..'9'.code.toByte()) {
            pos++
        }
        if (start == pos) return null
        val str = slice(start, pos).decodeToString()
        return str.toIntOrNull()
    }

    fun skipOptionalSemicolon() {
        if (peek() == ';'.code.toByte()) advance()
    }
}

/**
 * Parse a byte array containing ANSI escape sequences into a [Text].
 */
internal fun parseText(data: ByteArray): Text {
    val lines = mutableListOf<Line>()
    var lastStyle = Style.default()
    val parser = Parser(data)

    while (!parser.isAtEnd) {
        val (line, style) = parseLine(parser, lastStyle)
        lines.add(line)
        lastStyle = style
    }

    return Text.from(lines)
}

/**
 * Parse a single line from the parser.
 */
private fun parseLine(parser: Parser, style: Style): Pair<Line, Style> {
    val lineStart = parser.pos

    // Take until newline
    val lineData = parser.takeUntil { it == '\n'.code.toByte() }

    // Skip newline if present
    if (parser.peek() == '\n'.code.toByte()) {
        parser.advance()
    }

    val spans = mutableListOf<Span>()
    var lastStyle = style
    val lineParser = Parser(lineData)

    while (!lineParser.isAtEnd) {
        val span = parseSpan(lineParser, lastStyle)
        lastStyle = lastStyle.patch(span.style)
        if (span.content.isNotEmpty()) {
            spans.add(span)
        }
    }

    return Pair(Line.from(spans), lastStyle)
}

/**
 * Parse a single span from the parser.
 */
private fun parseSpan(parser: Parser, lastStyle: Style): Span {
    var currentStyle = lastStyle

    // Try to parse style escape sequence
    val styleResult = parseStyle(parser, currentStyle)
    if (styleResult != null) {
        currentStyle = currentStyle.patch(styleResult)
    }

    // Take text until escape or newline
    val textData = parser.takeWhile { it != ESC && it != '\n'.code.toByte() }
    val text = try {
        textData.decodeToString()
    } catch (e: Exception) {
        // Invalid UTF-8, skip
        ""
    }

    return Span.styled(text, currentStyle)
}

private const val ESC: Byte = 0x1B

/**
 * Parse a style escape sequence.
 */
private fun parseStyle(parser: Parser, style: Style): Style? {
    if (parser.peek() != ESC) return null

    val startPos = parser.pos
    parser.advance() // consume ESC

    // Check for CSI (Control Sequence Introducer) = ESC[
    if (parser.peek() != '['.code.toByte()) {
        // Try to consume other escape sequences
        consumeAnyEscapeSequence(parser)
        return null
    }
    parser.advance() // consume [

    // Parse SGR (Select Graphic Rendition) codes
    val items = mutableListOf<AnsiItem>()

    while (!parser.isAtEnd && parser.peek() != 'm'.code.toByte()) {
        val item = parseSgrItem(parser) ?: break
        items.add(item)
        parser.skipOptionalSemicolon()
    }

    // Expect 'm' terminator
    if (parser.peek() != 'm'.code.toByte()) {
        // Not a valid SGR sequence, try to recover
        parser.pos = startPos + 1 // skip just the ESC
        consumeAnyEscapeSequence(parser)
        return null
    }
    parser.advance() // consume 'm'

    return AnsiStates(items, style).toStyle()
}

/**
 * Consume any escape sequence (for sequences we don't understand).
 */
private fun consumeAnyEscapeSequence(parser: Parser) {
    val nextChar = parser.peek() ?: return

    when (nextChar.toInt().toChar()) {
        '[' -> {
            // CSI sequence: consume until alpha character
            parser.advance()
            parser.takeUntil { it.toInt().toChar().isLetter() }
            if (!parser.isAtEnd) parser.advance() // consume terminator
        }
        ']' -> {
            // OSC sequence: consume until BEL (0x07) or ST (ESC \)
            parser.advance()
            parser.takeUntil { it == 0x07.toByte() }
            if (!parser.isAtEnd) parser.advance() // consume BEL
        }
        else -> {
            // Unknown sequence, just skip one character
        }
    }
}

/**
 * Parse a single SGR item.
 */
private fun parseSgrItem(parser: Parser): AnsiItem? {
    val code = parser.parseU8() ?: return null
    val ansiCode = AnsiCode.from(code)

    val color = when (ansiCode) {
        is AnsiCode.SetForegroundColor, is AnsiCode.SetBackgroundColor -> {
            parser.skipOptionalSemicolon()
            parseColor(parser)
        }
        else -> null
    }

    return AnsiItem(ansiCode, color)
}

/**
 * Parse an extended color (8-bit or 24-bit).
 */
private fun parseColor(parser: Parser): Color? {
    val colorType = parseColorType(parser) ?: return null
    parser.skipOptionalSemicolon()

    return when (colorType) {
        ColorType.TrueColor -> {
            val r = parser.parseU8() ?: return null
            if (!parser.expectChar(';')) return null
            val g = parser.parseU8() ?: return null
            if (!parser.expectChar(';')) return null
            val b = parser.parseU8() ?: return null
            Color.Rgb(r, g, b)
        }
        ColorType.EightBit -> {
            val index = parser.parseU8() ?: return null
            Color.Indexed(index)
        }
    }
}

/**
 * Parse the color type indicator.
 */
private fun parseColorType(parser: Parser): ColorType? {
    val t = parser.parseInt() ?: return null
    if (!parser.expectChar(';')) return null

    return when (t) {
        2 -> ColorType.TrueColor
        5 -> ColorType.EightBit
        else -> null
    }
}
