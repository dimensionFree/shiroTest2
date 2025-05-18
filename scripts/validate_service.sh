#!/bin/bash
echo "Validating the service..."

echo "Checking if the application is running on localhost:8080..."

MAX_WAIT=20
WAITED=0

until curl --max-time 2 --silent http://localhost:8080 > /dev/null; do
  if [ "$WAITED" -ge "$MAX_WAIT" ]; then
    echo "❌ Timeout after $MAX_WAIT seconds. Application did not become available."
    exit 1
  fi
  echo "Application is not yet available. Waiting..."
  sleep 2
  WAITED=$((WAITED + 2))
done

echo "✅ Application is now running on localhost:8080."
