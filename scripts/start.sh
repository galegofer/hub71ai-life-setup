#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$project_root/frontend"
if [[ ! -d node_modules ]]; then npm ci; fi
cd "$project_root/backend"
mvn test
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9000 &
backend_pid=$!
trap 'kill "$backend_pid" 2>/dev/null || true' EXIT
cd "$project_root/frontend"
BACKEND_URL=http://127.0.0.1:9000 npm run dev -- --port 4321
