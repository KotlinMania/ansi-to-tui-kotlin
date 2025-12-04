package ansi_to_tui

/**
 * Error types for ANSI parsing.
 */
sealed class AnsiError : Exception() {
    /**
     * Parser error (should never happen).
     */
    data class ParseError(override val message: String) : AnsiError() {
        override fun toString(): String = "Internal error: $message"
    }

    /**
     * Error parsing the input as UTF-8.
     */
    data class Utf8Error(override val cause: Throwable) : AnsiError() {
        override fun toString(): String = "Utf8Error: ${cause.message}"
    }
}
