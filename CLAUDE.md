# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Early build. `ai-project-plan.md` is the build plan and the source of truth for scope, phases and design (it is gitignored, so it exists only locally). Read it before starting work, and tick its checkboxes as steps are completed. Both apps are scaffolded and connected by `GET /api/health`. The profile loader, `ClaudeService` (fake + real) and `POST /api/job-analysis` exist; gap analysis and documents (Phase 4 onward) don't yet.

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
- **`ClaudeService`** (package `claude`) has one generic method, `<T> T generate(ClaudeRequest, Class<T>)`. `ClaudeRequest(step, instructions, profile, userMessage)` carries the step name, the system prompt, the rendered profile (nullable) and the user input. There are two implementations, chosen in `ClaudeConfig` by custom conditions, which treat an empty key as missing:
  - `AnthropicClaudeService` is created only when `ANTHROPIC_API_KEY` is set.
    - Requests use structured outputs via `.outputConfig(type)`.
    - The system blocks are the profile first, marked `cache_control`, then the step instructions. This keeps the cache prefix shared across steps.
    - Server-side refusal fallbacks are on: the `fallbacks: "default"` body property plus the `server-side-fallback-2026-07-01` beta header.
    - It throws `ClaudeException` unless `stopReason == END_TURN`, and it logs token usage, including cache reads and writes.
    - SDK `com.anthropic:anthropic-java:3.2.0`.
  - `FakeClaudeService` returns `src/main/resources/fake-claude/<step>.json` parsed into the record type. It's active whenever there's no key. Write fixtures for `demo-profile`, using its real ids.
  - The user has no API key yet, so fake mode is the default way to run and demo the app. The app must start and work end to end without a key. Plan steps marked 🔑 need the real key; don't attempt them. Tests and CI never call the real API.
- **`ProfileLoader`** (package `profile`) reads `app.profile.dir`, set from `PROFILE_DIR` with default `../../demo-profile` relative to `apps/api`. It re-reads the files on every `load()`. Ids:
  - `exp-<folder>` for the role summary, and `exp-<folder>-<n>` for each bullet.
  - `proj-<slug>` from the first table in `registry.md`.
  - `skill-<slug>` from every skills-matrix table.
  - `cert-<slug>` and `edu-<slug>` from the first table in each file.
  - Colliding slugs get a `-2`, `-3`, ... suffix.
  - Bullets under the skills-matrix "Not evidenced" heading become `Profile.doNotClaim()`. They're rendered without ids, so they can't be cited.
  - `Profile.render()` produces the `[id] text` block Claude reads. Its output must stay deterministic (sorted files, no timestamps) so the prompt cache hits.
- **Each step is one feature package.** `job` is the pattern for the others:
  - A controller validates a request record with Bean Validation.
  - A service holds the prompt as a `static final` text block and calls `ClaudeService` with a step name. The step name is also the fixture name.
  - The output record carries `@JsonPropertyDescription` (`com.fasterxml.jackson.annotation`, which both Jackson 2 and 3 read). Those descriptions go into the structured-output schema.
  - Use enums for closed sets.
- **Errors:** errors are returned as problem JSON (`spring.mvc.problemdetails.enabled`). Validation failures are 400. `ClaudeExceptionHandler` maps `ClaudeException` and SDK errors to 502.
- **Tests per step:**
  - A `@WebMvcTest` with `@MockitoBean ClaudeService`.
  - A `*SchemaTest` that builds the real `.outputConfig(Record.class)` params, so schema problems surface without a key.
  - A `*FakeModeTest` (`@SpringBootTest(properties = "ANTHROPIC_API_KEY=")`) that posts a `docs/sample-jobs` file through the whole app.
- **No-invention guardrail** (central to the project):
  - Gap analysis: STRONG/PARTIAL items must cite evidence ids. In code, any returned id that isn't in the loaded profile turns the item into a GAP and is logged.
  - Documents: Claude writes only the summary, bullets and letter text. Contact details and education are copied **from the profile in code**, not from the model. Prompts say: use only profile facts, reword but never inflate, CV 1–2 pages, letter ≤ 250 words, ATS-friendly section names.
- **PDF:** Thymeleaf templates (`cv.html`, `cover-letter.html`) → OpenHTMLtoPDF. One-column, ATS-friendly CSS, no tables or icons.
- **Frontend:** Angular 21+, standalone, **zoneless**, Signals, Tailwind. One page with three steps (paste job → gaps table → document preview + 2 PDF downloads), with state in a small signals-based service. Calls the API through the dev-server proxy. Each step needs loading and error states, and focus moves to each new step's heading.

Note: the global Angular rules in `~/.claude/rules/angular.md` and the `ermes-frontend` skill target a different repo (Angular 16 + NgRx + PrimeNG, `slg-` prefix, `$localize`, `markForCheck`). They don't apply here. Use the modern zoneless/Signals idioms instead.

## Commands

Backend (`apps/api`, Spring Boot 4.1, Java 25 toolchain, Gradle wrapper, base package `io.github.ptitsynamv.cvtailor`, one sub-package per feature):

- Build + all tests: `./gradlew build`
- Run on :8080: `./gradlew bootRun`
- Single test class / method: `./gradlew test --tests HealthControllerTest` / `--tests 'HealthControllerTest.healthReturnsUpWithServiceName'`
- Boot 4 uses split starters (`spring-boot-starter-webmvc`, `...-webmvc-test`); `@WebMvcTest` lives in `org.springframework.boot.webmvc.test.autoconfigure`.
- Tests are JUnit 5 + Mockito. Claude must always be mocked. Profile tests read `../../demo-profile`.
- Check the loader against the private profile. This prints only counts and ids, and is skipped unless the env var is set: `CAREER_OPS_DIR=../../../career-ops ./gradlew test --tests CareerOpsProfileCheck -i`. Never print or commit real profile text.
- Spring Boot 4 uses Jackson 3 (`tools.jackson.*`, inject `JsonMapper`). The Anthropic SDK brings its own Jackson 2 internally; don't mix the two in app code.

Frontend (`apps/web`, Angular 22, zoneless, Tailwind 4, Vitest):

- Dev server: `npm start` (port 4200; `proxy.conf.json` forwards `/api` to :8080, so run `bootRun` too). Port 4200 is often taken by another local project; use `npx ng serve --port 4300` then.
- Build: `npm run build`
- Tests: `npx ng test --watch=false`. Run a single file with `npx ng test --watch=false --include src/app/app.spec.ts`.
- HTTP is read with `httpResource`. In specs, use `TestBed.tick()` before `HttpTestingController.expectOne` instead of `whenStable()`, which waits on the pending request and hangs.

CI (GitHub Actions, Phase 7) will run `./gradlew build` and `npm ci && npm run build` on every push. Run a secret scanner (e.g. gitleaks) before pushing anything sensitive-looking; the repo is already public.
