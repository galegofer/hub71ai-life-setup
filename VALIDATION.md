# Validation

## Hackathon delivery — 2 October 2026

- Public repository: https://github.com/galegofer/hub71ai-life-setup (`main`).
- Public frontend: https://frontend-production-20da.up.railway.app/ . Backend: https://backend-production-a5bdb.up.railway.app/api/health . Both are Railway services with separate `/frontend` and `/backend` roots and one replica each.
- Baseline deployment passed real backend HTTP smoke checks, desktop/mobile complete-and-unlock rehearsals and exact-origin CORS checks. The unrelated origin received HTTP 403. GitHub Actions run 36986882486 passed builds and the real HTTP smoke test from an external Ubuntu runner.
- Windows source validation after enhancements: 29 Maven tests passed; `mvn test package` succeeded. Frontend checks/build succeeded with zero errors, warnings or hints and four static routes.
- Bundled and packaged JAR SHA-256 match: `B00958728AB214C642422483E96627450D730127482F660463103C866D092F97`.
- Direct and Astro-proxy HTTP smoke tests pass with the new recommendation: Emirates ID, immediately unlocking exactly bank/driving. Premature completion, updated assistant guidance and undo propagation remain covered.
- Five request/launcher reliability groups pass, including production API addressing, nullable draft validation and preventing a missing country/extraction endpoint from being mistaken for an expired profile.
- All five local Edge browser groups pass: two desktop/mobile rehearsals with confirmation/reset, conditional onboarding and edits, defer/resume/undo, outage preservation/retry, expired-session recovery, keyboard focus and reduced motion. Desktop and 390px screenshots were visually inspected; no horizontal overflow was found.
- Four country/intake browser groups pass: accent/alias search, keyboard selection, Escape, clear/null, licence-only NONE, independent persisted codes, incomplete-story confirmation, malformed extraction preservation and country-list retry. See `docs/country-intake-validation.json`.
- OpenAI implementation uses strict Structured Outputs, backend-only credentials, `store:false`, a seven-second request deadline and at most two concurrent calls. Fake-client tests cover refusal, malformed output, timeout and fallback. The Railway backend key was still absent at the latest check; real OpenAI extraction remains pending secure key configuration.
- GitHub autodeploy account permissions are pending confirmation. Source deployments can be triggered with `railway redeploy --from-source --yes`; deployment metadata must match the pushed commit. Final deployment evidence will be appended after the enhanced build is verified.
- The three npm audit findings remain unresolved. GitHub additionally reports 13 dependency alerts across the repository (1 critical, 4 high, 5 moderate, 3 low); no major framework upgrades were made in this delivery pass.
- Astro dev dependency caching can become stale after a source build; restarting only the identified frontend process restores hydration. Tests wait for island hydration and loaded edit answers rather than relying on local-response timing.

## Windows verification — 2 October 2026

Verified in `C:\Dev\workspace\life-setup-agent` with Java 25.0.4.1, Maven 3.9.16, Node 24.21.0, npm 11.19.0, Spring Boot 4.0.0, Astro 5.18.2 and installed Microsoft Edge.

- `mvn -B -ntp test package` succeeded: 15 backend tests passed. The new cases cover recommendation IDs and deferred/blocked plans versus completed plans. The compiler reports an existing deprecated API usage in `ResponsesOpenAiClient`; it is not a test failure.
- `npm.cmd run build` succeeded: Astro check reported zero errors, warnings and hints, and three static routes built.
- The bundled and packaged JARs have identical SHA-256: `F67DD06720C364AF4180080674167B89DEF5CA650FB4E7ED35E01021A11D0034`.
- `python scripts/smoke-test.py` passed against port 9000. The same script with `--base-url http://127.0.0.1:4321` passed through the actual Astro proxy. Both check blocked driving, rejected premature completion, exactly bank/driving unlocking, repeated completion, the updated assistant qualification wording and undo propagation.
- `node scripts/reliability-test.mjs` passed all three groups: expiry/server/network/malformed replies, the ten-second request timeout, and launcher identity/exit/readiness failure handling.
- The Windows launcher integration checks passed for missing Node, Java and JAR; an unrelated listener on 4321 was preserved and no backend was started. A deliberately corrupt test JAR produced a clear exit-code error and its exact Java error was captured in the log.
- Fresh `Start-Windows.cmd` startup succeeded on 9000/4321. Repeat invocation reused both servers. Hidden Node supervisors keep valid log handles for Java on Windows; direct detached Java spawning was found to suppress stdout/stderr and was corrected.
- Final health and proxy checks returned HTTP 200 with `status: ok` and `mode: prototype`. The two final servers remain bound to `127.0.0.1` on 9000 and 4321. No unrelated process on 8080 was stopped.

## Browser and visual evidence

`scripts/browser-smoke.mjs` passed in fresh isolated Edge contexts:

- Two complete rehearsals, desktop and 390px mobile: blocked driving, ID received, exactly two highlighted cards and the announcement, updated assistant answer with official link, reset and restored blocking.
- Keyboard drawer dismissal restored the original trigger; completion focused the next-step button. Each onboarding question received focus.
- Conditional onboarding produced seven tasks for a solo profile; editing family/children/driving/pet answers produced eleven. Saved answers loaded correctly. Deferral, resume, completion and undo worked.
- Simulated request failures preserved the plan, existing assistant answer and edited answers. Action, chat, edit submission and initial-load retries worked without clearing the session reference.
- Expired sessions recovered during initial plan loading, edit loading, task actions, chat and edit submission. The stale browser reference was removed and recovery choices appeared.
- Mobile showed the next step above task groups, reset and prototype guidance below them, no horizontal overflow, and disabled transitions with reduced motion. No uncaught browser errors occurred.

Screenshots `docs/windows-plan-desktop.png` and `docs/windows-plan-mobile.png` were rendered and visually inspected. Structured results are recorded in `docs/windows-browser-validation.json`. The in-app browser runtime failed during sandbox setup; the available Playwright runtime with preinstalled Edge provided the browser checks. The test waits for Astro island hydration before interacting.

## Limits and unresolved findings

- `docs/windows-npm-audit.json` records three unresolved findings: critical Astro, high sharp and low esbuild. The suggested fix requires a major Astro upgrade. Astro 5 and the dependency lockfile were preserved for this pass.
- Browser outage/expiry scenarios use intercepted responses in isolated test contexts; the direct/proxy smoke tests use the real backend. The tests do not validate live government integrations.
- OpenAI interpretation remains optional. Prior fake-client fallback coverage still passes; no live OpenAI call was made.
- The original Linux validation passed 13 backend tests, build, HTTP/browser checks and visual inspection. Those results did not validate the Windows launchers; the checks above now do.
- Official link dates record discovery, not a full legal requirements review. Prototype dependencies, mocked connectors, in-memory progress and static-production proxy limits still apply. See README.md.
