# Code Port - Progress Report

**Generated:** 2026-08-27
**Source:** src
**Target:** src/commonMain/kotlin

## Executive Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| Function parity | 4/58 matched (target 32) | 6.9% |
| Class/type parity | 6/6 matched (target 40) | 100.0% |
| Combined symbol parity | 10/64 matched (target 72) | 15.6% |
| Average function body similarity | 0.30 | inline-code cosine |
| Average documentation similarity | 0.61 | doc text cosine |
| Missing source functions | 38 | 0% parity until ported |
| Missing source classes/types | 0 | 0% parity until ported |
| Missing source symbol files | 1 | 38 symbols |
| Cheat/scoring failures | 0 | forced to 0% |
| Total source files | 5 | 100% |
| Target units (paired) | 5 | - |
| Target files (total) | 5 | - |
| Porting progress | 4 | 80.0% (matched) |
| Missing files | 1 | 20.0% |

## Port Quality Analysis

**Average Function Similarity:** 0.30

Similarity in this report is the required function-by-function body/parameter score. Class/type parity and symbol deficits are reported beside it; whole-file shape is diagnostic only.

**Work Distribution:**
- Critical (<0.60): 3 files (75.0% of matched)
- Needs review (0.60-0.84): 1 files (25.0% of matched)

## Worst Function Scores First

Every matched file is listed from lowest function body/parameter similarity upward. Missing symbol names are not capped.

| Rank | Source | Target | Function similarity | Functions | Missing functions | Types | Missing types | Tests | Symbol deficit | Priority |
|------|--------|--------|---------------------|-----------|-------------------|-------|---------------|-------|----------------|----------|
| 1 | `parser` | `ansitotui.Parser` | 0.00 | 0/16 matched (target 22) | `from`, `text`, `text_fast`, `newline`, `line`, `line_fast`, `span`, `span_fast`, `style`, `ansi_sgr_code`, `any_escape_sequence`, `ansi_sgr_item`, `color`, `color_type`, `color_test`, `ansi_items_test` | 3/3 matched (target 4) | _none_ | 0/2 | 16 | 161910.0 |
| 2 | `lib` | `ansitotui.IntoText` | 0.25 | 2/2 matched (target 5) | _none_ | 1/1 matched | _none_ | - | 0 | 307.5 |
| 3 | `error` | `ansitotui.Error` | 0.28 | 1/1 matched (target 4) | _none_ | 1/1 matched (target 5) | _none_ | - | 0 | 1000207.2 |
| 4 | `code` | `ansitotui.Code` | 0.66 | 1/1 matched | _none_ | 1/1 matched (target 30) | _none_ | - | 0 | 203.4 |

## Cheat Detection / Scoring Failures

_None detected._

### Critical Ports (Similarity < 0.60, Worst First)

These files need significant work:

- `parser` -> `ansitotui.Parser` (0.00)
- `lib` -> `ansitotui.IntoText` (0.25)
- `error` -> `ansitotui.Error` (0.28, 1 deps)

## Incorrect Ports (Missing Types)

These files are matched (often via `// port-lint`) but appear to be missing one or more type declarations
present in the Rust source file.

| Source | Target | Missing types | Examples |
|--------|--------|---------------|----------|
| _None detected_ | | | |

## High Priority Missing Files

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `tests` | `Tests` | 0 | 38 | 0 | 38 | `tests.rs` | `Tests.kt` |

## Documentation Gaps

There is missing documentation that is hurting overall scoring.

**Documentation coverage:** 241 / 290 lines (83%)

Documentation gaps (>20%), complete list:

- `lib` - 63% gap (202 → 74 lines)
- `code` - 32% gap (68 → 46 lines)

