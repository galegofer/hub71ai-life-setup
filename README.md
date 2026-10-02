# Life Setup Agent

**One simple plan for starting your life in Abu Dhabi.**

Newcomers can find individual services, but understanding which apply, what order to follow and what each step unlocks is difficult. Life Setup combines a deterministic dependency engine, curated official sources and contextual guidance. The plan is the primary experience; the assistant helps explain it.

Submission repository: [galegofer/hub71ai-life-setup](https://github.com/galegofer/hub71ai-life-setup).

Java 25 / Spring Boot 4 / Maven backend; Astro 5 / TypeScript / React islands / Tailwind 4 frontend.

## Put the project on Windows

Extract the ZIP directly to `C:\Dev\workspace\life-setup-agent`. From a terminal in that folder, run:

```powershell
.\scripts\Start-Windows.cmd
```

The launcher works in PowerShell and Command Prompt without changing PowerShell execution policy. It checks Java/Node/npm and the bundled JAR, installs frontend dependencies only when missing, and waits for both servers. Run it again to reuse healthy demo processes. It refuses other listeners on ports 9000 and 4321 and never stops unrelated processes. Logs are captured in `logs/backend.log` and `logs/frontend.log`; a hidden Node supervisor keeps Java log handles valid on Windows. `scripts/Start-Windows.ps1` delegates to this launcher when scripts are permitted.

Prerequisites: Java 25 on PATH, Node.js 22.12+ (Node 24 recommended), npm, and internet access for the first dependency installation. Maven 3.9+ and a JDK 25 `JAVA_HOME` are needed only for backend source changes and tests. Use `.\scripts\Start-Windows.cmd --check` to check prerequisites and listeners without starting servers. If you extracted elsewhere, the optional install script copies to the requested folder and refuses to overwrite existing work.

## Run manually

PowerShell, backend terminal:

```powershell
cd C:\Dev\workspace\life-setup-agent
java -jar .\backend\demo\life-setup-agent.jar --server.port=9000
```

PowerShell, frontend terminal:

```powershell
cd C:\Dev\workspace\life-setup-agent\frontend
npm.cmd ci
$env:BACKEND_URL = 'http://127.0.0.1:9000'
npm.cmd run dev -- --port 4321
```

Command Prompt, backend terminal:

```bat
cd /d C:\Dev\workspace\life-setup-agent
java -jar backend\demo\life-setup-agent.jar --server.port=9000
```

Command Prompt, frontend terminal:

```bat
cd /d C:\Dev\workspace\life-setup-agent\frontend
npm.cmd ci
set "BACKEND_URL=http://127.0.0.1:9000"
npm.cmd run dev -- --port 4321
```

Run `npm.cmd ci` only for the first install or after dependency changes. Keep both servers running. Open [the demo](http://127.0.0.1:4321). Check [backend health](http://127.0.0.1:9000/api/health) and [proxy health](http://127.0.0.1:4321/api/health). Both should report `status: ok`. Both servers bind to loopback; the Astro dev server proxies `/api` to Spring on port 9000. On Linux/macOS use `bash scripts/start.sh` after installing the same tools.

If a server is already running on a conflicting port, stop that process from its own terminal only after identifying it. The launcher will not kill it or choose another port. In-memory progress resets whenever the backend restarts.

## Demo in 90 seconds

1. Click **Try the demo** on the welcome page.
2. Meet the Spanish professional moving with a spouse, child and pet.
3. Open **Check your driving licence options**; it is waiting for Emirates ID.
4. Open **Get your Emirates ID** and click **Emirates ID received**.
5. Bank and driving tasks become ready. The announcement names both steps.
6. Open **Ask Life Setup**, select the suggested question and click **Ask**.
7. Open the official driving service link.
8. Use **Reset demo profile** to repeat.

Onboarding, profile editing, task completion, undo, deferral and resuming are supported. Undoing a dependency also clears recorded completion for dependent tasks. Completed facts from onboarding are changed through **Edit your answers**.

## Assistant and OpenAI

Answers are generated from the current plan and curated task content. No API key is needed for the demo. An optional OpenAI Responses API client interprets a question into a task ID and intent using strict structured output; it does not generate government facts or change progress.

To enable interpretation, set `OPENAI_API_KEY` and `OPENAI_MODEL` in the backend terminal before starting Spring. Use a model available to your account that supports Responses structured outputs. `.env.example` is documentation; the app does not automatically load `.env` files. No key is sent to the browser. Questions are sent to OpenAI only when both environment variables are set; `store:false` is supplied. Errors fall back to deterministic interpretation.

## API

- `GET /api/health`
- `POST /api/demo`
- `POST /api/profile` with a `UserProfile` JSON object
- `PUT /api/profile/{id}`
- `GET /api/plan/{id}`
- `POST /api/plan/{id}/tasks/{taskId}/complete`
- `POST /api/plan/{id}/tasks/{taskId}` with `{ "action": "complete|undo|start|defer|resume" }`
- `GET /api/tasks/{taskId}`
- `GET /api/tasks/{taskId}/service` (explicit prototype connector)
- `POST /api/assistant` with `{ "profileId": "...", "question": "...", "taskId": null }`

Mutation responses contain the complete recalculated plan and `newlyReadyTaskIds`. The plan contains `nextBestAction` and nullable `nextBestTaskId`, selected by `LifePlanEngine`. No task is recommended when nothing is ready; saved or waiting steps are not called complete. Frontend logic never calculates dependencies.

Requests time out after ten seconds. Connection failures preserve the current plan, answers and session reference, with a manual retry. Expired profile sessions clear the stale reference and offer a new plan or demo. Chat answers clear after plan changes. The next-step card and demo reset are available on mobile too.

## Rules and factual limits

The curated dependency graph is a prototype planning model, not a verified statement of every legal or provider requirement. It models employer-sponsored professional relocation only. In the demo, ID collection can be in progress while residence is underway. Recording ID receipt also confirms the related residence milestone. Driving readiness means the next step can be checked, not that exchange eligibility is approved. Bank providers may support other routes.

Official URLs were found on 2 October 2026. The checked date records link discovery, not a full review of requirements. Most tasks deliberately omit costs, time estimates and legal document lists. Government connectors are mocked. No government status check, application submission, bank onboarding or UAE PASS login is implemented.

Utilities content follows the official ADDC connection information: most rentals in Abu Dhabi city and surrounding areas have water/electricity accounts set up through Tawtheeq. Users are asked to confirm the connection rather than submit a duplicate request.

## Rebuild and verify

On this Windows machine, Maven is installed at `C:\Dev\tools\apache-maven-3.9.16` and the JDK at `C:\Program Files\Java\jdk-25.0.4.1`. These settings apply only to the current terminal:

```powershell
cd C:\Dev\workspace\life-setup-agent
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.4.1'
cd backend
& 'C:\Dev\tools\apache-maven-3.9.16\bin\mvn.cmd' -B -ntp test
& 'C:\Dev\tools\apache-maven-3.9.16\bin\mvn.cmd' -B -ntp package
Copy-Item .\target\life-setup-agent-0.1.0.jar .\demo\life-setup-agent.jar
cd ..\frontend
npm.cmd run build
```

Command Prompt equivalents:

```bat
cd /d C:\Dev\workspace\life-setup-agent\backend
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
call C:\Dev\tools\apache-maven-3.9.16\bin\mvn.cmd -B -ntp test
call C:\Dev\tools\apache-maven-3.9.16\bin\mvn.cmd -B -ntp package
copy /y target\life-setup-agent-0.1.0.jar demo\life-setup-agent.jar
cd ..\frontend
npm.cmd run build
```

If Maven is already on PATH, use `mvn.cmd` instead of the full path. Restart the identified demo backend after refreshing its JAR.

Fifteen backend tests cover conditional tasks, completed facts, exact unlocking, premature completion, idempotency, undo propagation, deferral, malformed profiles, dependency graphs, recommendations and assistant answers. From the project root, check the running API and request/launcher failure handling:

```powershell
python scripts\smoke-test.py
python scripts\smoke-test.py --base-url http://127.0.0.1:4321
node scripts\reliability-test.mjs
```

`scripts/browser-smoke.mjs` rehearses desktop/mobile flows and recovery in isolated Edge sessions. It needs Playwright installed in the testing environment; set `PLAYWRIGHT_MODULE` to its module path if it is not on the project's module search path. It writes screenshots and JSON evidence under `docs`. `scripts/launcher-test.mjs` exercises fresh startup, port conflicts and failures; run it only after stopping the two identified demo processes. It leaves the demo running and writes temporary fixtures to ignored `.validation-launcher-*` folders.

The current npm audit reports three unresolved findings: critical in Astro, high in sharp and low in esbuild. Its suggested remedy upgrades Astro to a new major version. This pass retains Astro 5 and makes no broad dependency upgrade. See `docs/windows-npm-audit.json` for the recorded report.

## Deployment scope

The included `backend/demo/life-setup-agent.jar` is built from this source and needs Java 25. After editing backend source, run `mvn package` and use `backend/target/life-setup-agent-0.1.0.jar`, or replace the demo JAR with your new build.

This is a hackathon prototype. Local servers bind to loopback. Spring sessions live in memory and disappear on restart; the browser stores only a random session ID and offers recovery. Government connectors are mocked; no application is submitted and eligibility must be confirmed through official services. Do not enter sensitive personal information.

### Railway deployment

Connect two services to the submission repository's `main` branch. Backend root: `/backend`, config file: `/backend/railway.toml`. Frontend root: `/frontend`, config file: `/frontend/railway.toml`.

- Backend variables: `RAILPACK_JDK_VERSION=25`, `SERVER_ADDRESS=0.0.0.0`, and `CORS_ALLOWED_ORIGINS` set to the exact frontend HTTPS origin. Railway supplies `PORT`; use one replica because sessions are in memory. The service builds with Maven and starts the packaged JAR; health is `/api/health`.
- Frontend variables: `RAILPACK_NODE_VERSION=24` and build-time `PUBLIC_API_URL` set to the backend HTTPS origin without `/api`. The service builds Astro's static output and serves `dist` with `npm run start` on Railway's `PORT`.
- Generate public domains for both services, then set the API URL and CORS origin and deploy. Subsequent pushes to `main` deploy the affected services.
- Optional backend-only variables: `OPENAI_API_KEY` and `OPENAI_MODEL`. Never put the key in frontend variables or Git.

When `PUBLIC_API_URL` is unset, local requests continue through Astro's development `/api` proxy. The production static server has no API proxy. Browser validation can target the deployed frontend with `DEMO_BASE_URL`; HTTP smoke checks accept `--base-url` for the deployed backend.

## Next priorities

1. Review and expand the official rule catalogue.
2. Add real licence eligibility branches based on verified issuing-country rules.
3. Add durable progress storage if needed.
4. Add authentication only when integrating real user services.
