#!/bin/bash
echo "Stopping current Docker containers (if any)..."
docker stop $(docker ps -q) || true
docker rm $(docker ps -a -q) || true
# 设置 AWS 账户 ID 和区域作为环境变量
export AWS_REGION="ap-northeast-1"
export AWS_ACCOUNT_ID="724758113747"
export REPOSITORY_NAME="backend"

