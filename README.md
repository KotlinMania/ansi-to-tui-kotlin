# ansi-to-tui-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fansi--to--tui--kotlin-blue.svg)](https://github.com/KotlinMania/ansi-to-tui-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/ansi-to-tui-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/ansi-to-tui-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/ansi-to-tui-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/ansi-to-tui-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`ratatui/ansi-to-tu`](https://github.com/ratatui/ansi-to-tui).

**Original Project:** This port is based on [`ratatui/ansi-to-tu`](https://github.com/ratatui/ansi-to-tui). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:ansi-to-tui-kotlin:0.1.4")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`ratatui/ansi-to-tu`](https://github.com/ratatui/ansi-to-tui). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the ansi-to-tu authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`ratatui/ansi-to-tu`](https://github.com/ratatui/ansi-to-tui) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
