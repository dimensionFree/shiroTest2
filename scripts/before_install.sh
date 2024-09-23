#!/bin/bash
echo "Stopping current Docker containers (if any)..."
docker stop $(docker ps -q) || true
docker rm $(docker ps -a -q) || true

# Export variables
export AWS_ACCOUNT_ID=724758113747
export AWS_REGION=ap-northeast-1
export REPOSITORY_NAME=backend