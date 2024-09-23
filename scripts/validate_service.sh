#!/bin/bash
echo "Validating the service..."

# 检查应用是否在 localhost:80 上运行
curl -f http://localhost:80 || exit 1
