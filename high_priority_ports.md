# High Priority Ports - Action Plan

## Files by Impact

Priority = deps * 1,000,000 + SymDeficit * 10,000 + SrcSymbols * 100 + (1 - function similarity) * 10

Dependency fanout is ranked first so the ladder favors ports that clear downstream compilation failures fastest.

This list is complete and includes function/type detail for every matched file. Function similarity is the required body/parameter comparison; file-level shape does not rescue a port.

| Rank | Source | Target | Function similarity | Deps | Functions | Missing functions | Types | Missing types | SymDeficit | SrcSymbols | Priority |
|------|--------|--------|------------|------|-----------|-------------------|-------|---------------|-----------|------------|----------|
| 1 | `error` | `ansitotui.Error` | 0.28 | 1 | 1/1 matched (target 4) | _none_ | 1/1 matched (target 5) | _none_ | 0 | 2 | 1000207.2 |
| 2 | `parser` | `ansitotui.Parser` | 0.46 | 0 | 16/16 matched (target 18) | _none_ | 3/3 matched (target 4) | _none_ | 0 | 19 | 1905.4 |
| 3 | `lib` | `ansitotui.IntoText` | 0.25 | 0 | 2/2 matched (target 5) | _none_ | 1/1 matched | _none_ | 0 | 3 | 307.5 |
| 4 | `code` | `ansitotui.Code` | 0.66 | 0 | 1/1 matched | _none_ | 1/1 matched (target 30) | _none_ | 0 | 2 | 203.4 |

## Cheat Detection / Scoring Failures

_None detected._

## Critical Issues (Function Similarity < 0.60 with Dependencies)

These files need immediate attention:

- **error** → `ansitotui.Error`
  - Function similarity: 0.28
  - Dependencies: 1
  - Functions: 1/1 matched (target 4)
  - Missing functions: _none_
  - Types: 1/1 matched (target 5)
  - Missing types: _none_

## Missing Files (by Dependents)

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `tests` | `Tests` | 0 | 38 | 0 | 38 | `tests.rs` | `Tests.kt` |

