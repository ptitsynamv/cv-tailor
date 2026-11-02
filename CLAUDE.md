# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Pre-scaffold. The repo currently holds only `LICENSE` and `ai-project-plan.md`, the build plan and the source of truth for scope, phases and design. Read it before starting work, and tick its checkboxes as phases are completed. Update this file when real build commands and code exist.

**Scope rule:** the MVP is exactly three features: job analysis, gap analysis, and tailored CV + cover letter PDFs. Anything else belongs in the plan's "Later" table. Don't add it unprompted.

## Planned layout

```text
Projects/Personal-App/
├── career-ops/        PRIVATE sibling repo: real profile data; real PDFs go to applications/
└── cv-tailor/         THIS repo, PUBLIC: code only
    ├── apps/web/      Angular frontend
    ├── apps/api/      Spring Boot backend (Gradle)
    ├── demo-profile/  fictional "Alex Doe" profile, same folder layout as career-ops
    └── docs/sample-jobs/
```

## Public code, private data (hard constraint)

- This repo is public. Never copy anything from `../career-ops` into it: no real names, employers, contact details or generated PDFs. Tests and examples use `demo-profile/` only, and that profile must stay fictional.
- The backend reads the profile from `PROFILE_DIR` (default `./demo-profile`) and writes PDFs to `OUTPUT_DIR` (`../career-ops/applications/<date-company-role>/` locally; when unset, PDFs are only returned for download).
- `ANTHROPIC_API_KEY` is read from the environment on the backend only. Never put it in Angular code, `application.yml`, or any committed file. `.env` and `application-local.yml` are gitignored.

## Architecture

Three backend endpoints, one Claude call each, so the user reviews each step before the next:

```text
POST /api/job-analysis   job text           → JobAnalysis   (call 1)
POST /api/gap-analysis   JobAnalysis        → GapReport     (call 2, profile prompt-cached)
POST /api/documents      JobAnalysis + Gaps → TailoredCv + CoverLetter (call 3, profile cached)
                                            → cv.pdf + cover-letter.pdf (rendered in Java, no AI)
```

- **Backend:** Java 21/25 LTS, Spring Boot (Web, Validation), Gradle. Anthropic Java SDK (`com.anthropic:anthropic-java`), model `claude-opus-5-5`.
- **Claude features:** only **structured outputs** (every response maps to a typed Java record via `.outputConfig(X.class)`) and **prompt caching** (the profile block). Check `stopReason()` before reading content.
- **`ClaudeService`** wraps the SDK client (a `@Bean` built with `AnthropicOkHttpClient.fromEnv()`) so tests can mock it. Tests must never call the real API.
- **`ProfileLoader`** reads the Markdown files from `PROFILE_DIR` (`experience/*/data.md`, `projects/registry.md`, `profile/skills-matrix.md`, `certificates/data.md`, `education/data.md`), gives each item a stable id (`exp-<company>-<n>`, `proj-<slug>`, `cert-<slug>`), and renders one text block with `[id]` markers. That block is what Claude reads and cites.
- **No-invention guardrail** (central to the project):
  - Gap analysis: STRONG/PARTIAL items must cite evidence ids. In code, any returned id that isn't in the loaded profile turns the item into a GAP and is logged.
  - Documents: Claude writes only the summary, bullets and letter text. Contact details and education are copied **from the profile in code**, not from the model. Prompts say: use only profile facts, reword but never inflate, CV 1–2 pages, letter ≤ 250 words, ATS-friendly section names.
- **PDF:** Thymeleaf templates (`cv.html`, `cover-letter.html`) → OpenHTMLtoPDF. One-column, ATS-friendly CSS, no tables or icons.
- **Frontend:** Angular 21+, standalone, **zoneless**, Signals, Tailwind. One page with three steps (paste job → gaps table → document preview + 2 PDF downloads), with state in a small signals-based service. Calls the API through the dev-server proxy. Each step needs loading and error states, and focus moves to each new step's heading.

Note: the global Angular rules in `~/.claude/rules/angular.md` and the `ermes-frontend` skill target a different repo (Angular 16 + NgRx + PrimeNG, `slg-` prefix, `$localize`, `markForCheck`). They don't apply here. Use the modern zoneless/Signals idioms instead.

## Commands (planned, per the plan; verify once scaffolded)

- Backend build + tests: `./gradlew build` in `apps/api`. Tests are JUnit 5 + Mockito, with Claude mocked.
- Frontend: `npm ci && npm run build` in `apps/web`.
- CI (GitHub Actions) runs both on every push. Run a secret scanner (e.g. gitleaks) before making the repo public.
