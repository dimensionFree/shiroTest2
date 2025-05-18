#!/bin/bash
echo "Validating the service..."

# 检查应用是否在 localhost:8080 上运行
echo "Checking if the application is running on localhost:8080..."

MAX_WAIT=20    # 最多等待秒数
WAITED=0       # 当前已等待时间

until curl -s http://localhost:8080 > /dev/null; do
  if [ "$WAITED" -ge "$MAX_WAIT" ]; then
    echo "❌ Timeout after $MAX_WAIT seconds. Application did not become available."
    exit 1
  fi
  echo "Application is not yet available. Waiting..."
  sleep 5
  WAITED=$((WAITED + 5))
done

echo "✅ Application is now running on localhost:8080."
