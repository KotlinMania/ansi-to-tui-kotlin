import Testing
import AnsiToTui

@Suite struct AnsiToTuiExportTests {
    @Test func convertsAnsiTextThroughExport() {
        let parsed = ansitotui.intoText("\u{001B}[31mred\u{001B}[0m\n界")
        #expect(parsed.width() == 3)
        #expect(parsed.height() == 2)
    }

    @Test func convertsPlainTextThroughExport() {
        let parsed = ansitotui.toText("plain")
        #expect(parsed.width() == 5)
        #expect(parsed.height() == 1)
    }
}
