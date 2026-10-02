# Life Setup Agent

This is a five-hour hackathon prototype. Keep the complete-and-unlock demo working.

- Backend: Java 25, Spring Boot 4, Maven, immutable records and in-memory sessions.
- Frontend: Astro 5, TypeScript, Tailwind 4; React islands only for stateful features.
- Government requirements and dependencies must come from reviewed rules, never an LLM. Label prototype assumptions. Do not invent URLs, costs or eligibility decisions.
- Keep all task-state logic in `LifePlanEngine`.
- Use plain English, mobile layouts and the existing visual styles.
- Run `mvn test` in `backend` and `npm run build` in `frontend` after relevant changes.
- Update the bundled demo JAR after backend changes using `mvn package` and copy `backend/target/life-setup-agent-0.1.0.jar` to `backend/demo/life-setup-agent.jar`.
- Leave UAE PASS, databases, PWA, payment processing and live government connectors out of scope unless requested.
- Read README.md for the demo sequence and factual limits.
