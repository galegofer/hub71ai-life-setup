# Validation

## Hackathon delivery — 2 October 2026

### Reopened value-density pass — 15:20 Dubai

- The user explicitly reopened the submission and overrode the earlier feature cutoff. Personal context, readiness counts, biggest blocker, immediate unlocks and downstream guidance now come from LifePlanEngine without changing task-state rules or recommendation ordering.
- Drawers retain separate plan/connection labels, compact unknown estimates, attribution and actions. Existing mock progress is reused; services without existing mock data retain LINK_ONLY. No unsupported numeric estimates were introduced.
- Frontend checks/build passed with zero errors, warnings or hints. Maven package passed and bundled/packaged JAR hashes match: `E59C400445113F7964C1701F1CDB4840B0C87BA0452B1A33012B26EF9A61FFB9`.
- The user requested no further tests. Further browser/smoke/regression tests were skipped; earlier evidence below describes earlier commits, not verification of this pass. At 15:25 Dubai, both Railway services reported SUCCESS for `f2a11682efba17ed6b969758011832fc01991347`. Public backend health returned ok and the frontend returned HTTP 200. Local refreshed demo startup reported both ports ready. This documentation-only delivery commit will also be rolled out to both services.


### Source-aware refinement — 14:44 Dubai

- The user explicitly reclassified `1e93acb7e154d50a7720ffd298c30fbc344be9e9` as a checkpoint and authorized this bounded replacement. Feature implementation and local validation completed before 14:45; only deployment and delivery validation remain afterward.
- Reviewed duration/cost metadata now takes precedence over UNKNOWN defaults. Entries must cite the task's registered official source and use OFFICIAL confidence. Registered source metadata was inspected; none supports fees or durations, so production entries remain null with no invented values.
- Drawer plan state is labelled Your plan and remains separate from service mode. Unknown estimates use secondary styling, with full explanations in a keyboard-accessible Estimate basis disclosure. Missing data uses the same concise fallback.
- Maven tests/package passed 36 tests. Frontend checks/build passed with zero errors, warnings or hints. All three local task-detail browser groups, five demo/recovery groups and direct/proxy HTTP smoke tests passed. The 390px screenshot was rendered and inspected; no overflow. Exact unlock, assistant and existing actions remain intact.
- Bundled/packaged JAR SHA-256 matches: `0B96AFF91842D3F47B9CC2D7842DBD1E59A6D5544AE274A94F97FBF90F949D2A`. The refreshed local demo remains running.
- Public backend and frontend report SUCCESS for `93a19eefc22eb508b8a7744a175f37611b6ea37f`. Health returned ok, reviewed metadata is present, and the public HTTP smoke test passed. External Actions run 36997402220 passed 36 tests/package, frontend checks/build and public smoke from Ubuntu.
- All three public task-detail browser groups and five public demo/recovery groups passed after this rollout. These cover every drawer, compact unknown values, keyboard disclosure access, preserved source links, isolated LIVE/OFFICIAL fixtures, missing data, two desktop/mobile rehearsals, exact unlock/assistant, edits/actions/reset, outage recovery, expiry, focus, reduced motion and no localhost production requests. See `docs/deployed-task-detail-validation.json` and `docs/deployed-browser-validation.json`.
- The public 390px drawer screenshot is byte-identical to the locally rendered and visually inspected screenshot (SHA-256 `54400073E84B04BFCAECFD2898F5E16F2C3D467087D2ED81DF4ECCFB5DA6144E`). Both are 390x844. Public plan screenshots were refreshed. No tracked secret patterns were found.
- The latest public extraction check still returned FALLBACK with nationality/licence unknown for an origin-only description. The earlier account diagnostic reported exhausted API credits; successful live OpenAI extraction remains unverified. Existing dependency audit findings remain unresolved.
- Feature work stopped before 14:45. This evidence commit becomes the replacement final judged submission, with no subsequent code changes; both services will be deployed from its exact SHA and reported in the delivery message.

- Public repository: https://github.com/galegofer/hub71ai-life-setup (`main`).
- Public frontend: https://frontend-production-20da.up.railway.app/ . Backend: https://backend-production-a5bdb.up.railway.app/api/health . Both are Railway services with separate `/frontend` and `/backend` roots and one replica each.
- Baseline deployment passed real backend HTTP smoke checks, desktop/mobile complete-and-unlock rehearsals and exact-origin CORS checks. The unrelated origin received HTTP 403. GitHub Actions run 36986882486 passed builds and the real HTTP smoke test from an external Ubuntu runner.
- Windows source validation after enhancements: 29 Maven tests passed; `mvn test package` succeeded. Frontend checks/build succeeded with zero errors, warnings or hints and four static routes.
- Bundled and packaged JAR SHA-256 match: `B00958728AB214C642422483E96627450D730127482F660463103C866D092F97`.
- Direct and Astro-proxy HTTP smoke tests pass with the new recommendation: Emirates ID, immediately unlocking exactly bank/driving. Premature completion, updated assistant guidance and undo propagation remain covered.
- Five request/launcher reliability groups pass, including production API addressing, nullable draft validation and preventing a missing country/extraction endpoint from being mistaken for an expired profile.
- All five local Edge browser groups pass: two desktop/mobile rehearsals with confirmation/reset, conditional onboarding and edits, defer/resume/undo, outage preservation/retry, expired-session recovery, keyboard focus and reduced motion. Desktop and 390px screenshots were visually inspected; no horizontal overflow was found.
- Four country/intake browser groups pass: accent/alias search, keyboard selection, Escape, clear/null, licence-only NONE, independent persisted codes, incomplete-story confirmation, malformed extraction preservation and country-list retry. See `docs/country-intake-validation.json`.
- OpenAI implementation uses strict Structured Outputs, backend-only credentials, `store:false`, a seven-second request deadline and at most two concurrent calls. Fake-client tests cover refusal, malformed output, timeout and fallback. The backend key is securely configured. Its credential and model checks passed, but a real Responses request returned HTTP 429 (`insufficient_quota`, `credit_balance_exhausted`); successful real extraction awaits account credits. Public extraction continued in FALLBACK mode and preserved independent unknown country values.
- Enhanced public deployment: both Railway services report SUCCESS for commit `355b0272ea8332bf056a4171d70e91f8384f46d1`. Backend `/api/health`, the 249-country endpoint and the complete-and-unlock HTTP smoke test passed after rollout.
- All five deployed Edge browser groups and four deployed country/intake groups passed. These cover two full desktop/mobile demo rehearsals, reset, edits, defer/resume/undo, temporary outages, expired-session recovery, independent country codes and no localhost production requests. Public screenshots were rendered and visually inspected. Evidence: `docs/deployed-browser-validation.json`, `docs/deployed-country-intake-validation.json`, `docs/deployed-plan-desktop.png` and `docs/deployed-plan-mobile.png`.
- GitHub Actions run 36992537056 passed 29 backend tests/package, frontend checks/build and the enhanced public HTTP smoke test from an external Ubuntu network. Exact-origin CORS returned HTTP 200 with the frontend origin; an unrelated origin returned HTTP 403.
- Additional public API checks passed: independent ES demo fields; origin-only and malicious descriptions leave nationality/licence unknown and do not mutate an existing plan; unsupported countries return HTTP 400; Spanish/Spain normalize to ES; licence NONE persists; the curated catalogue exposes eleven tasks. See `docs/deployed-api-validation.json`.
- Delivery checkpoint `96dd56deed422a187b8a0c5d8a91c51de4209906` is healthy on both Railway services. External Actions run 36992988143 passed for this commit. A real pre-rollout session returned HTTP 404 after the backend restarted; the public browser removed its stale reference and showed recovery choices without response interception. See `docs/deployed-restart-validation.json`.
- GitHub autodeploy account permissions are pending confirmation. Source deployments were triggered with `railway redeploy --from-source --yes`, and deployment metadata matched the pushed commit. The final judged commit is reported in the delivery message; features are frozen.
- The three npm audit findings remain unresolved. GitHub additionally reports 13 dependency alerts across the repository (1 critical, 4 high, 5 moderate, 3 low); no major framework upgrades were made in this delivery pass.
- Astro dev dependency caching can become stale after a source build; restarting only the identified frontend process restores hydration. Tests wait for island hydration and loaded edit answers rather than relying on local-response timing.

## Windows verification — 2 October 2026

### Task-detail follow-up — 14:25 Dubai

- All eleven tasks now expose service status, connection mode, time/cost estimates, confidence, basis and the existing official action URL. Residence/ID progress is explicitly MOCK; the remaining services are LINK_ONLY. No live government connector or new fee/time claim was added.
- `mvn test package` passed 34 tests, including five focused service-model tests. Frontend checks/build passed with zero errors, warnings or hints. Bundled/packaged JAR SHA-256 matches: `11AEC348259917F3A347579A8C4565034471AE5329748DD27874AA5009503FB1`.
- Three local drawer browser groups passed: all eleven 390px drawers and existing source links; LIVE/OFFICIAL rendering through isolated test fixtures; missing status/estimates and unsupported official attribution. No horizontal overflow or browser errors. `docs/windows-id-detail-mobile.png` was rendered and visually inspected; `docs/windows-task-detail-validation.json` records checks.
- All five local demo/recovery browser groups, five reliability groups, direct HTTP smoke and Astro-proxy smoke passed again. The exact ID unlock, assistant, undo/defer/edit/reset, focus and reduced motion remain intact. The refreshed demo is running locally on 9000/4321.
- Both public services report SUCCESS for task-detail commit `cede7ebe5873a658ef191ad4e148493686cd9e47`. The public backend returned structured details for all eleven tasks and passed the HTTP smoke test. GitHub Actions run 36995682988 passed 34 backend tests/package, frontend checks/build and public HTTP smoke from the external runner.
- The user requested skipping further validation after this rollout. No additional public browser rehearsal of the new drawers was performed; the focused drawer browser results above are local. Existing deployed browser evidence remains from the earlier delivery checkpoint.
- OpenAI credential and model availability are confirmed, and the backend variable matches the supplied file without extra whitespace. The latest actual Responses request reported exhausted API credits. The user is handling account credits; successful OpenAI extraction has not been verified. Fallback remains available. No credential was placed in tracked files or printed.

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
- OpenAI interpretation remains optional. Fake-client fallback coverage passes. The current backend credential is valid, but the real API returned an exhausted credit balance at the latest check; no successful live interpretation has been verified yet.
- The original Linux validation passed 13 backend tests, build, HTTP/browser checks and visual inspection. Those results did not validate the Windows launchers; the checks above now do.
- Official link dates record discovery, not a full legal requirements review. Prototype dependencies, mocked connectors and in-memory progress still apply. Production requests use the configured HTTPS backend directly. See README.md.
