#!/bin/bash
echo "Running Docker container from ECR..."

## 登录到 ECR

# Export variables
export AWS_ACCOUNT_ID=724758113747
export AWS_REGION=ap-northeast-1
export REPOSITORY_NAME=backend

# 输出其他调试信息
echo "AWS_ACCOUNT_ID: $AWS_ACCOUNT_ID"
echo "AWS_REGION: $AWS_REGION"
echo "REPOSITORY_NAME: $REPOSITORY_NAME"

echo "logining aws"

aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

##删除历史镜像
docker rmi $(docker images | grep backend | awk '{print $3}')

## 拉取镜像
echo "pulling docker"
docker pull $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPOSITORY_NAME:latest

#!/bin/bash

# 从 Parameter Store 获取数据库连接信息
DB_URL=$(aws ssm get-parameter --name "/myapp/datebase_url" --with-decryption --query "Parameter.Value" --output text)
DB_USERNAME=$(aws ssm get-parameter --name "/myapp/datebase_username" --with-decryption --query "Parameter.Value" --output text)
DB_PASSWORD=$(aws ssm get-parameter --name "/myapp/datebase_pwd" --with-decryption --query "Parameter.Value" --output text)
DEV_MAIL_PASSWORD=$(aws ssm get-parameter --name "/myapp/email_pwd" --with-decryption --query "Parameter.Value" --output text)
DEV_MAIL_USERNAME=$(aws ssm get-parameter --name "/myapp/email_username" --with-decryption --query "Parameter.Value" --output text)
TURNSTILE_SECRET=$(aws ssm get-parameter --name "/myapp/turnstileSecretKey" --with-decryption --query "Parameter.Value" --output text)
JWT_SECRET=$(aws ssm get-parameter --name "/myapp/jwt_secret" --with-decryption --query "Parameter.Value" --output text)
DEV_REDIS_HOST=my-backend-service-redis-1

# Export variables
export DB_URL
export DB_USERNAME
export DB_PASSWORD
export DEV_MAIL_USERNAME
export DEV_MAIL_PASSWORD
export DEV_REDIS_HOST
export TURNSTILE_SECRET
export JWT_SECRET

# 调试输出，确保正确获取到值
echo "DB_URL: $DB_URL"
echo "DB_USERNAME: $DB_USERNAME"
echo "DB_PASSWORD: $DB_PASSWORD"
echo "DEV_MAIL_USERNAME: $DEV_MAIL_USERNAME"
echo "DEV_REDIS_HOST: $DEV_REDIS_HOST"
echo "TURNSTILE_SECRET: $TURNSTILE_SECRET"
echo "JWT_SECRET: $JWT_SECRET"

echo "Docker Compose Installing..."
pwd
# 获取最新版本号
COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep -oP '"tag_name": "\K(.*)(?=")')

# 下载并安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/${curl -s https://api.github.com/repos/docker/compose/releases/latest | grep -oP '"tag_name": "\K(.*)(?=")'}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

sleep 20


echo "Docker Compose installed."
ls

# 启动 Docker Compose
echo "Starting services with Docker Compose..."
docker-compose -p my-backend-service up -d

## 运行 Docker 容器并传递环境变量
#echo "gonna runing container"
#
#CONTAINER_ID=$(docker run -d -p 8080:8080 -e DB_URL="$DB_URL" -e DB_USERNAME="$DB_USERNAME" -e DB_PASSWORD="$DB_PASSWORD" -e DEV_MAIL_USERNAME="$DEV_MAIL_USERNAME" -e DEV_MAIL_PASSWORD="$DEV_MAIL_PASSWORD" $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPOSITORY_NAME:latest)
#
## 检查是否成功启动容器
#if [ $? -ne 0 ]; then
#  echo "Docker container failed to start."
#  exit 1
#fi

## 输出 Docker 容器的日志
#echo "Fetching logs from the container..."
#docker logs $(docker ps -q --filter "name=backend")  # 替换为你的服务名
#
## 等待容器变为健康状态
#echo "Waiting for the container to be healthy..."
#CONTAINER_ID=$(docker ps -q --filter "name=backend")  # 替换为你的服务名
#while [ "$(docker inspect --format='{{.State.Health.Status}}' $CONTAINER_ID)" != "healthy" ]; do
#  echo "Container is not healthy yet. Waiting..."
#  sleep 5
#done


echo "docker run excuted"

