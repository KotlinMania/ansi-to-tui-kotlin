// port-lint: source lib.rs
/**
 * Convert ANSI color and style codes into Ratatui [Text].
 *
 * This package parses bytes containing ANSI SGR escape sequences (like `\u001b[31m`).
 * It produces a Ratatui [Text] with equivalent foreground/background [Color] and
 * [Modifier] settings via [Style].
 *
 * Unknown or malformed escape sequences are ignored, so you can feed it real terminal output
 * without having to pre-clean it.
 *
 * ## Features
 * - UTF-8 decoding
 * - SGR styles such as bold, italic, underline, and strikethrough
 * - Colors: named (3/4-bit, 8/16-color), indexed (8-bit, 256-color), and truecolor (24-bit RGB)
 *
 * ## Supported Color Codes
 *
 * | Color Mode                  | Supported | SGR Example                   | Ratatui Color Example   |
 * | --------------------------- | :-------: | ----------------------------- | ----------------------- |
 * | Named (3/4-bit, 8/16-color) |     ✓     | `\u001b[30..37;40..47m`       | `Color.Blue`            |
 * | Indexed (8-bit, 256-color)  |     ✓     | `\u001b[38;5;<N>m`            | `Color.Indexed(1)`      |
 * | Truecolor (24-bit RGB)      |     ✓     | `\u001b[38;2;<R>;<G>;<B>m`    | `Color.Rgb(255, 0, 0)`  |
 *
 * ## Example
 *
 * ```kotlin
 * val bytes = "\u001b[38;2;225;192;203mAAAAA\u001b[0m".encodeToByteArray()
 * val text = bytes.intoText()
 * ```
 */
package ansitotui

import ratatui.style.Color
import ratatui.style.Modifier
import ratatui.style.Style
import ratatui.text.Text

/**
 * Parse ANSI SGR styled bytes into a Ratatui [Text].
 *
 * This interface is implemented for byte containers and strings, allowing
 * [intoText] and [toText] calls.
 */
interface IntoText {
    /**
     * Convert the type to a [Text].
     *
     * @return The parsed [Text] with styles applied.
     */
    fun intoText(): Text

    /**
     * Convert the type to a [Text].
     *
     * @return The parsed [Text] with styles applied.
     */
    fun toText(): Text = intoText()
}

/**
 * Convert a [ByteArray] containing ANSI escape sequences to a [Text].
 *
 * Invalid ANSI sequences are ignored.
 *
 * @return The parsed [Text] with styles applied.
 */
fun ByteArray.intoText(): Text = parseText(this)

/**
 * Convert a [ByteArray] containing ANSI escape sequences to a [Text].
 *
 * Invalid ANSI sequences are ignored.
 *
 * @return The parsed [Text] with styles applied.
 */
fun ByteArray.toText(): Text = parseText(this)

/**
 * Convert a [String] containing ANSI escape sequences to a [Text].
 *
 * Invalid ANSI sequences are ignored.
 *
 * @return The parsed [Text] with styles applied.
 */
fun String.intoText(): Text = this.encodeToByteArray().intoText()

/**
 * Convert a [String] containing ANSI escape sequences to a [Text].
 *
 * Invalid ANSI sequences are ignored.
 *
 * @return The parsed [Text] with styles applied.
 */
fun String.toText(): Text = this.encodeToByteArray().toText()
