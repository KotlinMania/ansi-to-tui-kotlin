// port-lint: tests tests.rs
package ansitotui

import io.github.kotlinmania.prettyassertions.assertEq
import ratatui.style.Color
import ratatui.style.Style
import ratatui.style.bg
import ratatui.style.black
import ratatui.style.blue
import ratatui.style.bold
import ratatui.style.crossedOut
import ratatui.style.cyan
import ratatui.style.darkGray
import ratatui.style.dim
import ratatui.style.fg
import ratatui.style.gray
import ratatui.style.green
import ratatui.style.hidden
import ratatui.style.italic
import ratatui.style.lightBlue
import ratatui.style.lightCyan
import ratatui.style.lightGreen
import ratatui.style.lightMagenta
import ratatui.style.lightRed
import ratatui.style.lightYellow
import ratatui.style.magenta
import ratatui.style.notBold
import ratatui.style.notCrossedOut
import ratatui.style.notDim
import ratatui.style.notHidden
import ratatui.style.notItalic
import ratatui.style.notRapidBlink
import ratatui.style.notReversed
import ratatui.style.notSlowBlink
import ratatui.style.notUnderlined
import ratatui.style.onBlack
import ratatui.style.onBlue
import ratatui.style.onCyan
import ratatui.style.onDarkGray
import ratatui.style.onGray
import ratatui.style.onGreen
import ratatui.style.onLightBlue
import ratatui.style.onLightCyan
import ratatui.style.onLightGreen
import ratatui.style.onLightMagenta
import ratatui.style.onLightRed
import ratatui.style.onLightYellow
import ratatui.style.onMagenta
import ratatui.style.onRed
import ratatui.style.onWhite
import ratatui.style.onYellow
import ratatui.style.rapidBlink
import ratatui.style.red
import ratatui.style.reversed
import ratatui.style.slowBlink
import ratatui.style.underlined
import ratatui.style.white
import ratatui.style.yellow
import ratatui.text.Line
import ratatui.text.Span
import ratatui.text.Text
import kotlin.test.Test

/**
 * Tests for the ANSI to TUI parser, ported from the upstream Rust test suite.
 */
class AnsiToTuiTest {
    @Test
    fun parsesPlainTextWithoutStyles() {
        val string = "FOO".encodeToByteArray()
        testBoth(string, Text.raw("FOO"))
    }

    @Test
    fun parsesUnicodeText() {
        // These are 8 byte unicode characters.
        // First 4 bytes are for the unicode and the last 4 bytes are for the color / variant.
        val bytes = "AAA🅱️🅱️🅱️".encodeToByteArray()
        val output = Text.raw("AAA🅱️🅱️🅱️")
        testBoth(bytes, output)
    }

    @Test
    fun preservesEmptyLinesWhenSplittingOnNewlines() {
        val bytes = "LINE_1\n\n\n\n\n\n\nLINE_8".encodeToByteArray()
        val output =
            Text.from(
                listOf(
                    Line.from("LINE_1"),
                    Line.from(""),
                    Line.from(""),
                    Line.from(""),
                    Line.from(""),
                    Line.from(""),
                    Line.from(""),
                    Line.from("LINE_8"),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun mixedCrAndLfSequencesAreAllNewlines() {
        val bytes = "A\r\n\rB\n\nC\r\r\nD".encodeToByteArray()
        val output =
            Text.from(
                listOf(
                    Line.from("A"),
                    Line.from(""),
                    Line.from("B"),
                    Line.from(""),
                    Line.from("C"),
                    Line.from(""),
                    Line.from("D"),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun treatsCrlfAsSingleNewline() {
        val bytes = "LINE_1\r\nLINE_2\r\nLINE_3".encodeToByteArray()
        val output =
            Text.from(
                listOf(
                    Line.from("LINE_1"),
                    Line.from("LINE_2"),
                    Line.from("LINE_3"),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun treatsBareCrAsNewline() {
        val bytes = "ABC\rDEF".encodeToByteArray()
        val output = Text.from(listOf(Line.from("ABC"), Line.from("DEF")))
        testBoth(bytes, output)
    }

    @Test
    fun mixedLfAndCrlfLineEndingsAreNormalized() {
        val bytes = "A\nB\r\nC\nD\r\nE".encodeToByteArray()
        val output =
            Text.from(
                listOf(
                    Line.from("A"),
                    Line.from("B"),
                    Line.from("C"),
                    Line.from("D"),
                    Line.from("E"),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun crlfOnlyInputIsEmptyLine() {
        val bytes = "\r\n".encodeToByteArray()
        val output = Text.raw("")
        testBoth(bytes, output)
    }

    @Test
    fun crBeforeNonSgrEscapeSequenceStartsNewLine() {
        val bytes = "\r\u001b[KOVERWRITE".encodeToByteArray()
        val output = Text.from(listOf(Line.from(""), Line.from("OVERWRITE")))
        testBoth(bytes, output)
    }

    @Test
    fun crlfSplitsLinesAndCarriesStyleAcrossLines() {
        val bytes = "A\u001b[31mB\r\nC".encodeToByteArray()
        val output =
            Text.from(
                listOf(
                    Line.from(listOf(Span.raw("A"), "B".red())),
                    Line.from("C".red()),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun ignoresTruncatedEscapeSequence() {
        val bytes = "\u001b[".encodeToByteArray()
        val output = Text.raw("")
        testBoth(bytes, output)
    }

    @Test
    fun ignoresGarbageEscapeSequences() {
        val bytes = "\u001b\u001b[0\u001b[m\u001b".encodeToByteArray()
        val output = Text.raw("")
        testBoth(bytes, output)
    }

    @Test
    fun ignoresNonSgrEscapeSequences() {
        val bytes = "\u001b[?25hAAABBB".encodeToByteArray()
        val output = Text.raw("AAABBB")
        testBoth(bytes, output)
    }

    @Test
    fun ignoresOscAndOtherNonSgrSequences() {
        // Malformed -> malformed -> empty
        val bytes = "\u001b[4 q\u001b]12;#fab1ed\u0007".encodeToByteArray()
        val output = Text.raw("")
        testBoth(bytes, output)
    }

    @Test
    fun unknownSgrCodesAreIgnoredAndChainedItemsStillApply() {
        val bytes = "\u001b[200;31mred".encodeToByteArray()
        val output = Text.from("red".red())
        testBoth(bytes, output)
    }

    @Test
    fun emptySgrSequenceIsTreatedAsReset() {
        val string = "\u001b[32mGREEN\u001b[mFOO\nFOO".encodeToByteArray()
        val output =
            Text.from(
                listOf(
                    Line.from(listOf("GREEN".green(), Span.styled("FOO", Style.reset()))),
                    Line.from(Span.styled("FOO", Style.reset())),
                ),
            )
        testBoth(string, output)
    }

    @Test
    fun chainedSgrItemsInSingleEscapeSequenceAreAppliedInOrder() {
        val bytes = "\u001b[31;44;1mX".encodeToByteArray()
        val output = Text.from("X".red().onBlue().bold())
        testBoth(bytes, output)
    }

    @Test
    fun doesNotEmitEmptySpansForStyleOnlyChanges() {
        // Yellow -> Red -> Green -> "Hello" -> Reset -> "World"
        val bytes = "\u001b[33m\u001b[31m\u001b[32mHello\u001b[0mWorld".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        "Hello".green(),
                        Span.styled("World", Style.reset()),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr0ResetsStyle() {
        val string = "\u001b[33mA\u001b[0mB".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        "A".yellow(),
                        Span.styled("B", Style.reset()),
                    ),
                ),
            )
        testBoth(string, output)
    }

    @Test
    fun sgr1And22ToggleBold() {
        val bytes = "not, \u001b[1mbold\u001b[22m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "bold".bold(),
                        ", not anymore".notBold().notDim(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr2And22ToggleFaint() {
        val bytes = "not, \u001b[2mfaint\u001b[22m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "faint".dim(),
                        ", not anymore".notBold().notDim(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr3And23ToggleItalic() {
        val bytes = "not, \u001b[3mitalic\u001b[23m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "italic".italic(),
                        ", not anymore".notItalic(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr4And24ToggleUnderline() {
        val bytes = "not, \u001b[4munderlined\u001b[24m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "underlined".underlined(),
                        ", not anymore".notUnderlined(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr5And25ToggleSlowBlink() {
        val bytes = "not, \u001b[5mblinking\u001b[25m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "blinking".slowBlink(),
                        ", not anymore".notSlowBlink().notRapidBlink(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr6And25ToggleRapidBlink() {
        val bytes = "not, \u001b[6mrapid\u001b[25m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "rapid".rapidBlink(),
                        ", not anymore".notSlowBlink().notRapidBlink(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr7And27ToggleReverseVideo() {
        val bytes = "not, \u001b[7mreversed\u001b[27m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "reversed".reversed(),
                        ", not anymore".notReversed(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr8And28ToggleConceal() {
        val bytes = "not, \u001b[8mconcealed\u001b[28m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "concealed".hidden(),
                        ", not anymore".notHidden(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr9And29ToggleCrossedOut() {
        val bytes = "not, \u001b[9mcrossed\u001b[29m, not anymore".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        Span.raw("not, "),
                        "crossed".crossedOut(),
                        ", not anymore".notCrossedOut(),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun parses4bitNamedColorsAndBackgrounds() {
        val black = "\u001b[30m"
        val red = "\u001b[31m"
        val green = "\u001b[32m"
        val yellow = "\u001b[33m"
        val blue = "\u001b[34m"
        val magenta = "\u001b[35m"
        val cyan = "\u001b[36m"
        val gray = "\u001b[37m"

        val blackBg = "\u001b[40m"
        val redBg = "\u001b[41m"
        val greenBg = "\u001b[42m"
        val yellowBg = "\u001b[43m"
        val blueBg = "\u001b[44m"
        val magentaBg = "\u001b[45m"
        val cyanBg = "\u001b[46m"
        val grayBg = "\u001b[47m"

        val bytes =
            (
                "${black}black\n" +
                    "${red}red\n" +
                    "${green}green\n" +
                    "${yellow}yellow\n" +
                    "${blue}blue\n" +
                    "${magenta}magenta\n" +
                    "${cyan}cyan\n" +
                    "${gray}gray\n" +
                    "${black}${blackBg}black-bg\n" +
                    "${redBg}red-bg\n" +
                    "${greenBg}green-bg\n" +
                    "${yellowBg}yellow-bg\n" +
                    "${blueBg}blue-bg\n" +
                    "${magentaBg}magenta-bg\n" +
                    "${cyanBg}cyan-bg\n" +
                    "${grayBg}gray-bg"
            ).encodeToByteArray()

        val output =
            Text.from(
                listOf(
                    Line.from("black".black()),
                    Line.from("red".red()),
                    Line.from("green".green()),
                    Line.from("yellow".yellow()),
                    Line.from("blue".blue()),
                    Line.from("magenta".magenta()),
                    Line.from("cyan".cyan()),
                    Line.from("gray".gray()),
                    Line.from("black-bg".black().onBlack()),
                    Line.from("red-bg".black().onRed()),
                    Line.from("green-bg".black().onGreen()),
                    Line.from("yellow-bg".black().onYellow()),
                    Line.from("blue-bg".black().onBlue()),
                    Line.from("magenta-bg".black().onMagenta()),
                    Line.from("cyan-bg".black().onCyan()),
                    Line.from("gray-bg".black().onGray()),
                ),
            )

        testBoth(bytes, output)
    }

    @Test
    fun parses4bitBrightColorsAndBackgrounds() {
        val darkGray = "\u001b[90m"
        val lightRed = "\u001b[91m"
        val lightGreen = "\u001b[92m"
        val lightYellow = "\u001b[93m"
        val lightBlue = "\u001b[94m"
        val lightMagenta = "\u001b[95m"
        val lightCyan = "\u001b[96m"
        val white = "\u001b[97m"

        val darkGrayBg = "\u001b[100m"
        val lightRedBg = "\u001b[101m"
        val lightGreenBg = "\u001b[102m"
        val lightYellowBg = "\u001b[103m"
        val lightBlueBg = "\u001b[104m"
        val lightMagentaBg = "\u001b[105m"
        val lightCyanBg = "\u001b[106m"
        val whiteBg = "\u001b[107m"

        val bytes =
            (
                "${darkGray}dark-gray\n" +
                    "${lightRed}light-red\n" +
                    "${lightGreen}light-green\n" +
                    "${lightYellow}light-yellow\n" +
                    "${lightBlue}light-blue\n" +
                    "${lightMagenta}light-magenta\n" +
                    "${lightCyan}light-cyan\n" +
                    "${white}white\n" +
                    "\u001b[30m${darkGrayBg}dark-gray-bg\n" +
                    "${lightRedBg}light-red-bg\n" +
                    "${lightGreenBg}light-green-bg\n" +
                    "${lightYellowBg}light-yellow-bg\n" +
                    "${lightBlueBg}light-blue-bg\n" +
                    "${lightMagentaBg}light-magenta-bg\n" +
                    "${lightCyanBg}light-cyan-bg\n" +
                    "${whiteBg}white-bg"
            ).encodeToByteArray()

        val output =
            Text.from(
                listOf(
                    Line.from("dark-gray".darkGray()),
                    Line.from("light-red".lightRed()),
                    Line.from("light-green".lightGreen()),
                    Line.from("light-yellow".lightYellow()),
                    Line.from("light-blue".lightBlue()),
                    Line.from("light-magenta".lightMagenta()),
                    Line.from("light-cyan".lightCyan()),
                    Line.from("white".white()),
                    Line.from("dark-gray-bg".black().onDarkGray()),
                    Line.from("light-red-bg".black().onLightRed()),
                    Line.from("light-green-bg".black().onLightGreen()),
                    Line.from("light-yellow-bg".black().onLightYellow()),
                    Line.from("light-blue-bg".black().onLightBlue()),
                    Line.from("light-magenta-bg".black().onLightMagenta()),
                    Line.from("light-cyan-bg".black().onLightCyan()),
                    Line.from("white-bg".black().onWhite()),
                ),
            )

        testBoth(bytes, output)
    }

    @Test
    fun sgr31And39ToggleForegroundColor() {
        val bytes = "\u001b[31;1mred\u001b[39mdefault".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        "red".red().bold(),
                        "default".bold().fg(Color.Reset),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun sgr44And49ToggleBackgroundColor() {
        val bytes = "\u001b[44;1mblue-bg\u001b[49mdefault".encodeToByteArray()
        val output =
            Text.from(
                Line.from(
                    listOf(
                        "blue-bg".onBlue().bold(),
                        "default".bold().bg(Color.Reset),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    @Test
    fun parses256colorForegroundPalette() {
        for (i in 0..255) {
            val bytes = "\u001b[38;5;${i}mHELLO".encodeToByteArray()
            val output = Text.from("HELLO".fg(Color.Indexed(i.toUByte())))
            testBoth(bytes, output)
        }
    }

    @Test
    fun parses256colorBackgroundPalette() {
        for (i in 0..255) {
            val bytes = "\u001b[48;5;${i}mHELLO".encodeToByteArray()
            val output = Text.from("HELLO".bg(Color.Indexed(i.toUByte())))
            testBoth(bytes, output)
        }
    }

    @Test
    fun parsesTruecolorForeground() {
        val bytes = "\u001b[38;2;100;100;100mAAABBB".encodeToByteArray()
        val output = Text.from("AAABBB".fg(Color.Rgb(100u, 100u, 100u)))
        testBoth(bytes, output)
    }

    @Test
    fun parsesTruecolorForegroundAndBackground() {
        data class ColorRgb(
            val r: UByte,
            val g: UByte,
            val b: UByte,
        )
        val testCases =
            listOf(
                Pair(ColorRgb(1u, 2u, 3u), ColorRgb(4u, 5u, 6u)),
                Pair(ColorRgb(255u, 0u, 128u), ColorRgb(0u, 64u, 255u)),
                Pair(ColorRgb(17u, 34u, 51u), ColorRgb(68u, 85u, 102u)),
            )

        for ((f, b) in testCases) {
            val bytes =
                "\u001b[38;2;${f.r};${f.g};${f.b};48;2;${b.r};${b.g};${b.b}mHELLO".encodeToByteArray()
            val output =
                Text.from(
                    "HELLO"
                        .fg(Color.Rgb(f.r, f.g, f.b))
                        .bg(Color.Rgb(b.r, b.g, b.b)),
                )
            testBoth(bytes, output)
        }
    }

    @Test
    fun carriesStyleAcrossLinesAndHandlesResets() {
        val bytes =
            (
                "\u001b[32m* \u001b[0mRunning before-startup command \u001b[1mcommand\u001b[0m=make my-simple-package.cabal\n" +
                    "\u001b[32m* \u001b[0m\$ make my-simple-package.cabal\n" +
                    "Build profile: -w ghc-9.0.2 -O1\n"
            ).encodeToByteArray()
        val output =
            Text.from(
                listOf(
                    Line.from(
                        listOf(
                            "* ".green(),
                            Span.styled("Running before-startup command ", Style.reset()),
                            Span.styled("command", Style.reset()).bold(),
                            Span.styled("=make my-simple-package.cabal", Style.reset()),
                        ),
                    ),
                    Line.from(
                        listOf(
                            Span.styled("* ", Style.reset()).green(),
                            Span.styled("\$ make my-simple-package.cabal", Style.reset()),
                        ),
                    ),
                    Line.from(
                        listOf(
                            Span.styled(
                                "Build profile: -w ghc-9.0.2 -O1",
                                Style.reset(),
                            ),
                        ),
                    ),
                ),
            )
        testBoth(bytes, output)
    }

    fun testBoth(bytes: ByteArray, other: Text) {
        val zeroCopy = bytes.toText()
        val owned = bytes.intoText()

        assertEq(
            zeroCopy,
            owned,
            "zero-copy and owned version of the methods have diverged; this is a bug in the library",
        )

        assertEq(
            owned,
            other,
            "owned and other have diverged; this might be a bug in the library or a ratatui update",
        )

        assertEq(zeroCopy, other)
    }
}
