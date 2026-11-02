# Fake Claude fixtures

When `ANTHROPIC_API_KEY` isn't set, `FakeClaudeService` answers every Claude call with `<step>.json` from this folder, parsed into the step's record type.

| Step | File | Added in |
| :--- | :--- | :--- |
| Job analysis | `job-analysis.json` | Phase 3 |
| Gap analysis | `gap-report.json` | Phase 4 |
| Documents | `documents.json` | Phase 5 |

Write fixtures for the fictional `demo-profile`, and use its real evidence ids, so fake mode shows realistic output.
