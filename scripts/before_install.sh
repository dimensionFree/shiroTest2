#!/bin/bash
echo "Stopping current Docker containers (if any)..."
docker stop $(docker ps -q) || true
docker rm $(docker ps -a -q) || true
