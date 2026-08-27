// port-lint: source error.rs
/**
 * Errors returned by this crate.
 */
package ansitotui

/**
 * Errors returned by this crate.
 */
sealed class Error : Exception() {
    /**
     * Parsing failed.
     */
    data class NomError(
        val details: String,
    ) : Error() {
        override val message: String get() = "Parse error: $details"

        override fun toString(): String = message
    }

    /**
     * Error parsing the input as UTF-8.
     */
    data class Utf8Error(
        override val cause: Throwable,
    ) : Error() {
        override val message: String get() = "Utf8Error: ${cause.message}"

        override fun toString(): String = message
    }

    /**
     * Legacy ParseError subclass for compatibility.
     */
    data class ParseError(
        val details: String,
    ) : Error() {
        override val message: String get() = "Internal error: $details"

        override fun toString(): String = message
    }

    companion object {
        /**
         * Create an Error from a parse error message.
         */
        fun from(message: String): Error = NomError(message)
    }
}

typealias AnsiError = Error
