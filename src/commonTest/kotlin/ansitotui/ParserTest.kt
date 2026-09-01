// port-lint: tests parser.rs
package ansitotui

import ratatui.style.Color
import ratatui.style.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class ParserTest {
    @Test
    fun colorTest() {
        val c1 = color("2;255;255;255".encodeToByteArray(), 0)
        assertEquals(Color.Rgb(255u, 255u, 255u), c1.first)

        val c2 = color("5;255".encodeToByteArray(), 0)
        assertEquals(Color.Indexed(255u), c2.first)

        val err = color("10;255".encodeToByteArray(), 0)
        assertNotEquals(c2.first, err.first)
    }

    @Test
    fun ansiItemsTest() {
        val sc = Style.default()
        val t1 = style(sc)("\u001b[38;2;3;3;3m".encodeToByteArray(), 0).first
        assertNotNull(t1)
        assertEquals(
            t1,
            AnsiStates(
                listOf(
                    AnsiItem(
                        code = AnsiCode.SetForegroundColor,
                        color = Color.Rgb(3u, 3u, 3u),
                    ),
                ),
                style = sc,
            ).toStyle(),
        )

        val t2 = style(sc)("\u001b[38;5;3m".encodeToByteArray(), 0).first
        assertNotNull(t2)
        assertEquals(
            t2,
            AnsiStates(
                listOf(
                    AnsiItem(
                        code = AnsiCode.SetForegroundColor,
                        color = Color.Indexed(3u),
                    ),
                ),
                style = sc,
            ).toStyle(),
        )

        val t3 = style(sc)("\u001b[38;5;3;48;5;3m".encodeToByteArray(), 0).first
        assertNotNull(t3)
        assertEquals(
            t3,
            AnsiStates(
                listOf(
                    AnsiItem(
                        code = AnsiCode.SetForegroundColor,
                        color = Color.Indexed(3u),
                    ),
                    AnsiItem(
                        code = AnsiCode.SetBackgroundColor,
                        color = Color.Indexed(3u),
                    ),
                ),
                style = sc,
            ).toStyle(),
        )

        val t4 = style(sc)("\u001b[38;5;3;48;5;3;1m".encodeToByteArray(), 0).first
        assertNotNull(t4)
        assertEquals(
            t4,
            AnsiStates(
                listOf(
                    AnsiItem(
                        code = AnsiCode.SetForegroundColor,
                        color = Color.Indexed(3u),
                    ),
                    AnsiItem(
                        code = AnsiCode.SetBackgroundColor,
                        color = Color.Indexed(3u),
                    ),
                    AnsiItem(
                        code = AnsiCode.Bold,
                        color = null,
                    ),
                ),
                style = sc,
            ).toStyle(),
        )
    }
}
