import Testing
import AnsiToTui

@Suite struct AnsiToTuiExportTests {
    @Test func swiftModuleLoads() throws {
        #expect(Bool(true), "AnsiToTui swift module imported cleanly")
    }
}
