// port-lint: source parser.rs
package ansitotui

import ratatui.style.Color
import ratatui.style.Modifier
import ratatui.style.Style
import ratatui.text.Line
import ratatui.text.Span
import ratatui.text.Text

/**
 * Color type indicator for extended colors (8-bit and 24-bit).
 */
internal enum class ColorType {
    /** Eight Bit color */
    EightBit,

    /** 24-bit color or true color */
    TrueColor,
}

/**
 * An ANSI item with code and optional color.
 */
internal data class AnsiItem(
    val code: AnsiCode,
    val color: Color? = null,
)

/**
 * ANSI state accumulator.
 */
internal data class AnsiStates(
    val items: List<AnsiItem> = emptyList(),
    val style: Style = Style.default(),
) {
    fun toStyle(): Style = from(this)
}

/**
 * Convert AnsiStates to Style.
 */
internal fun from(states: AnsiStates): Style {
    var styleResult = states.style
    if (states.items.isEmpty()) {
        // [m should be treated as a reset as well
        return Style.reset()
    }
    for (item in states.items) {
        styleResult =
            when (item.code) {
                is AnsiCode.Reset -> Style.reset()
                is AnsiCode.Bold -> styleResult.addModifier(Modifier.BOLD)
                is AnsiCode.Faint -> styleResult.addModifier(Modifier.DIM)
                is AnsiCode.Normal -> styleResult.removeModifier(Modifier.BOLD or Modifier.DIM)
                is AnsiCode.Italic -> styleResult.addModifier(Modifier.ITALIC)
                is AnsiCode.NotItalic -> styleResult.removeModifier(Modifier.ITALIC)
                is AnsiCode.Underline -> styleResult.addModifier(Modifier.UNDERLINED)
                is AnsiCode.UnderlineOff -> styleResult.removeModifier(Modifier.UNDERLINED)
                is AnsiCode.SlowBlink -> styleResult.addModifier(Modifier.SLOW_BLINK)
                is AnsiCode.RapidBlink -> styleResult.addModifier(Modifier.RAPID_BLINK)
                is AnsiCode.BlinkOff -> styleResult.removeModifier(Modifier.SLOW_BLINK or Modifier.RAPID_BLINK)
                is AnsiCode.Reverse -> styleResult.addModifier(Modifier.REVERSED)
                is AnsiCode.Conceal -> styleResult.addModifier(Modifier.HIDDEN)
                is AnsiCode.Reveal -> styleResult.removeModifier(Modifier.HIDDEN)
                is AnsiCode.CrossedOut -> styleResult.addModifier(Modifier.CROSSED_OUT)
                is AnsiCode.CrossedOutOff -> styleResult.removeModifier(Modifier.CROSSED_OUT)
                is AnsiCode.DefaultForegroundColor -> styleResult.fg(Color.Reset)
                is AnsiCode.DefaultBackgroundColor -> styleResult.bg(Color.Reset)
                is AnsiCode.SetForegroundColor -> {
                    item.color?.let { styleResult.fg(it) } ?: styleResult
                }
                is AnsiCode.SetBackgroundColor -> {
                    item.color?.let { styleResult.bg(it) } ?: styleResult
                }
                is AnsiCode.ForegroundColor -> styleResult.fg(item.code.color)
                is AnsiCode.BackgroundColor -> styleResult.bg(item.code.color)
                else -> styleResult
            }
    }
    return styleResult
}

/**
 * Parse ANSI bytes into a Text.
 */
internal fun text(s: ByteArray): Text {
    val lines = mutableListOf<Line>()
    var last = Style.default()
    var offset = 0
    while (offset < s.size) {
        val (l, nextStyle, nextOffset) = line(last)(s, offset)
        lines.add(l)
        last = nextStyle
        if (nextOffset == offset) {
            break
        }
        offset = nextOffset
    }
    return Text.from(lines)
}

/**
 * Parse ANSI bytes into a Text (fast / zero-copy equivalent).
 */
internal fun textFast(s: ByteArray): Text = text(s)

/**
 * Parse a newline.
 */
internal fun newline(s: ByteArray, pos: Int): Pair<Int, Boolean> {
    if (pos >= s.size) return Pair(pos, false)
    if (s[pos] == '\r'.code.toByte() && pos + 1 < s.size && s[pos + 1] == '\n'.code.toByte()) {
        return Pair(pos + 2, true)
    }
    if (s[pos] == '\n'.code.toByte() || s[pos] == '\r'.code.toByte()) {
        return Pair(pos + 1, true)
    }
    return Pair(pos, false)
}

/**
 * Parse a line given a starting style.
 */
internal fun line(style: Style): (ByteArray, Int) -> Triple<Line, Style, Int> = { s, startPos ->
    var pos = startPos
    // take until newline
    val lineStart = pos
    while (pos < s.size && s[pos] != '\n'.code.toByte() && s[pos] != '\r'.code.toByte()) {
        pos++
    }
    val lineEnd = pos
    val (afterNewline, _) = newline(s, pos)

    val spans = mutableListOf<Span>()
    var last = style
    var textPos = lineStart
    while (textPos < lineEnd) {
        val (sp, nextStyle, nextPos) = span(last)(s, textPos, lineEnd)
        last = last.patch(sp.style)
        if (sp.content.isNotEmpty()) {
            spans.add(sp)
        }
        if (nextPos == textPos) {
            break
        }
        textPos = nextPos
    }

    Triple(Line.from(spans), last, afterNewline)
}

/**
 * Parse a line (fast equivalent).
 */
internal fun lineFast(style: Style): (ByteArray, Int) -> Triple<Line, Style, Int> = line(style)

/**
 * Parse a single styled span.
 */
internal fun span(last: Style): (ByteArray, Int, Int) -> Triple<Span, Style, Int> = { s, startPos, endPos ->
    var currentLast = last
    var pos = startPos

    val (parsedStyle, styleEndPos) = style(currentLast)(s, pos)
    pos = styleEndPos
    if (parsedStyle != null) {
        currentLast = currentLast.patch(parsedStyle)
    }

    val textStart = pos
    while (pos < endPos && s[pos] != 0x1B.toByte() && s[pos] != '\n'.code.toByte() && s[pos] != '\r'.code.toByte()) {
        pos++
    }
    val content = if (pos > textStart) s.decodeToString(textStart, pos) else ""

    Triple(Span.styled(content, currentLast), currentLast, pos)
}

/**
 * Parse a span (fast equivalent).
 */
internal fun spanFast(last: Style): (ByteArray, Int, Int) -> Triple<Span, Style, Int> = { s, startPos, endPos ->
    span(last)(s, startPos, endPos)
}

/**
 * Parse optional style escape sequence.
 */
internal fun style(style: Style): (ByteArray, Int) -> Pair<Style?, Int> = { s, pos ->
    if (pos >= s.size || s[pos] != 0x1B.toByte()) {
        Pair(null, pos)
    } else {
        val (items, afterSgr) = ansiSgrCode(s, pos)
        if (items != null) {
            Pair(AnsiStates(items, style).toStyle(), afterSgr)
        } else {
            val (_, afterEsc) = anyEscapeSequence(s, pos)
            Pair(null, afterEsc)
        }
    }
}

/**
 * Parse a complete ANSI SGR code (ESC [ ... m).
 */
internal fun ansiSgrCode(s: ByteArray, pos: Int): Pair<List<AnsiItem>?, Int> {
    if (pos >= s.size || s[pos] != 0x1B.toByte()) return Pair(null, pos)
    if (pos + 1 >= s.size || s[pos + 1] != '['.code.toByte()) return Pair(null, pos)

    var cur = pos + 2
    val items = mutableListOf<AnsiItem>()

    while (cur < s.size && s[cur] != 'm'.code.toByte()) {
        val (item, nextCur) = ansiSgrItem(s, cur)
        if (item == null) {
            return Pair(null, pos)
        }
        items.add(item)
        cur = nextCur
    }

    if (cur < s.size && s[cur] == 'm'.code.toByte()) {
        return Pair(items, cur + 1)
    }
    return Pair(null, pos)
}

/**
 * Consume any escape sequence.
 */
internal fun anyEscapeSequence(s: ByteArray, pos: Int): Pair<ByteArray?, Int> {
    if (pos >= s.size || s[pos] != 0x1B.toByte()) return Pair(null, pos)
    var cur = pos + 1
    if (cur < s.size) {
        when (s[cur].toInt().toChar()) {
            '[' -> {
                cur++
                while (cur < s.size && !s[cur].toInt().toChar().isLetter()) {
                    cur++
                }
                if (cur < s.size) cur++ // consume alpha
                return Pair(s.sliceArray((pos + 1) until cur), cur)
            }
            ']' -> {
                cur++
                while (cur < s.size && s[cur] != 0x07.toByte()) {
                    cur++
                }
                if (cur < s.size) cur++ // consume BEL
                return Pair(s.sliceArray((pos + 1) until cur), cur)
            }
            else -> {
                return Pair(null, cur)
            }
        }
    }
    return Pair(null, cur)
}

/**
 * Parse a single ANSI SGR item.
 */
internal fun ansiSgrItem(s: ByteArray, pos: Int): Pair<AnsiItem?, Int> {
    var cur = pos
    // parse u8
    val numStart = cur
    while (cur < s.size && s[cur] in '0'.code.toByte()..'9'.code.toByte()) {
        cur++
    }
    if (cur == numStart) {
        return Pair(null, pos)
    }
    val codeVal = s.decodeToString(numStart, cur).toUByteOrNull() ?: return Pair(null, pos)
    val code = AnsiCode.from(codeVal)

    var colorVal: Color? = null
    when (code) {
        is AnsiCode.SetForegroundColor, is AnsiCode.SetBackgroundColor -> {
            if (cur < s.size && s[cur] == ';'.code.toByte()) {
                cur++
            }
            val (c, nextCur) = color(s, cur)
            if (c == null) {
                return Pair(null, pos)
            }
            colorVal = c
            cur = nextCur
        }
        else -> {}
    }

    if (cur < s.size && s[cur] == ';'.code.toByte()) {
        cur++
    }

    return Pair(AnsiItem(code, colorVal), cur)
}

/**
 * Parse an extended color.
 */
internal fun color(s: ByteArray, pos: Int): Pair<Color?, Int> {
    val (cType, nextPos) = colorType(s, pos)
    if (cType == null) return Pair(null, pos)
    var cur = nextPos
    if (cur < s.size && s[cur] == ';'.code.toByte()) {
        cur++
    }

    return when (cType) {
        ColorType.TrueColor -> {
            val rStart = cur
            while (cur < s.size && s[cur] in '0'.code.toByte()..'9'.code.toByte()) cur++
            val r = s.decodeToString(rStart, cur).toUByteOrNull() ?: return Pair(null, pos)
            if (cur >= s.size || s[cur] != ';'.code.toByte()) return Pair(null, pos)
            cur++

            val gStart = cur
            while (cur < s.size && s[cur] in '0'.code.toByte()..'9'.code.toByte()) cur++
            val g = s.decodeToString(gStart, cur).toUByteOrNull() ?: return Pair(null, pos)
            if (cur >= s.size || s[cur] != ';'.code.toByte()) return Pair(null, pos)
            cur++

            val bStart = cur
            while (cur < s.size && s[cur] in '0'.code.toByte()..'9'.code.toByte()) cur++
            val b = s.decodeToString(bStart, cur).toUByteOrNull() ?: return Pair(null, pos)

            Pair(Color.Rgb(r, g, b), cur)
        }
        ColorType.EightBit -> {
            val idxStart = cur
            while (cur < s.size && s[cur] in '0'.code.toByte()..'9'.code.toByte()) cur++
            val idx = s.decodeToString(idxStart, cur).toUByteOrNull() ?: return Pair(null, pos)
            Pair(Color.Indexed(idx), cur)
        }
    }
}

/**
 * Parse the color type indicator (2 for RGB, 5 for indexed).
 */
internal fun colorType(s: ByteArray, pos: Int): Pair<ColorType?, Int> {
    var cur = pos
    val numStart = cur
    if (cur < s.size && s[cur] == '-'.code.toByte()) cur++
    while (cur < s.size && s[cur] in '0'.code.toByte()..'9'.code.toByte()) cur++
    if (cur == numStart) return Pair(null, pos)
    val num = s.decodeToString(numStart, cur).toLongOrNull() ?: return Pair(null, pos)
    if (cur >= s.size || s[cur] != ';'.code.toByte()) return Pair(null, pos)
    cur++ // skip ';'

    val type =
        when (num) {
            2L -> ColorType.TrueColor
            5L -> ColorType.EightBit
            else -> null
        } ?: return Pair(null, pos)

    return Pair(type, cur)
}

/**
 * Backward compatibility helper.
 */
internal fun parseText(data: ByteArray): Text = text(data)
