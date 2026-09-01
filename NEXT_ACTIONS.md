# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/5 (80.0%)
- **Function parity:** 20/58 matched (target 28) — 34.5%
- **Class/type parity:** 6/6 matched (target 40) — 100.0%
- **Combined symbol parity:** 26/64 matched (target 68) — 40.6%
- **Average inline-code cosine:** 0.41 (function body across 4 matched files)
- **Average documentation cosine:** 0.65 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 3 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `ansitotui.Error`
- **Similarity:** 0.28
- **Dependents:** 1
- **Priority Score:** 1000207.2
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_

### 2. parser

- **Target:** `ansitotui.Parser`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 1905.4
- **Functions:** 16/16 matched (target 18)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 3. lib

- **Target:** `ansitotui.IntoText`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 307.5
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 4. code

- **Target:** `ansitotui.Code`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 203.4
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 30)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

