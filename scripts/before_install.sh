#!/bin/bash
echo "Stopping and removing Docker containers containing the keyword 'backend'..."

## 停止名称中包含 'backend' 的容器
#docker ps -q --filter "name=backend" | xargs -r docker stop
#
## 删除名称中包含 'backend' 的容器
#docker ps -a -q --filter "name=backend" | xargs -r docker rm



echo "Containers with the keyword 'backend' have been stopped and removed (if any)."
