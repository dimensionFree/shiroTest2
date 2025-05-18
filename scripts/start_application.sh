#!/bin/bash
set -e

echo "▶️ Logging in to ECR..."
export AWS_ACCOUNT_ID=724758113747
export AWS_REGION=ap-northeast-1
export REPOSITORY_NAME=backend

aws ecr get-login-password --region $AWS_REGION \
  | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

echo "▶️ Pulling latest backend image..."
docker pull $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPOSITORY_NAME:latest

echo "▶️ Fetching parameters from AWS SSM Parameter Store..."
DB_URL=$(aws ssm get-parameter --name "/myapp/datebase_url" --with-decryption --query "Parameter.Value" --output text)
DB_USERNAME=$(aws ssm get-parameter --name "/myapp/datebase_username" --with-decryption --query "Parameter.Value" --output text)
DB_PASSWORD=$(aws ssm get-parameter --name "/myapp/datebase_pwd" --with-decryption --query "Parameter.Value" --output text)
DEV_MAIL_PASSWORD=$(aws ssm get-parameter --name "/myapp/email_pwd" --with-decryption --query "Parameter.Value" --output text)
DEV_MAIL_USERNAME=$(aws ssm get-parameter --name "/myapp/email_username" --with-decryption --query "Parameter.Value" --output text)
TURNSTILE_SECRET=$(aws ssm get-parameter --name "/myapp/turnstileSecretKey" --with-decryption --query "Parameter.Value" --output text)
JWT_SECRET=$(aws ssm get-parameter --name "/myapp/jwt_secret" --with-decryption --query "Parameter.Value" --output text)

# 环境变量注入给 docker-compose 使用
export DB_URL DB_USERNAME DB_PASSWORD DEV_MAIL_USERNAME DEV_MAIL_PASSWORD DEV_REDIS_HOST TURNSTILE_SECRET JWT_SECRET
export DEV_REDIS_HOST=my-backend-service-redis-1


# 调试输出，确保正确获取到值
echo "DB_URL: $DB_URL"
echo "DB_USERNAME: $DB_USERNAME"
echo "DB_PASSWORD: $DB_PASSWORD"
echo "DEV_MAIL_USERNAME: $DEV_MAIL_USERNAME"
echo "DEV_REDIS_HOST: $DEV_REDIS_HOST"
echo "TURNSTILE_SECRET: $TURNSTILE_SECRET"
echo "JWT_SECRET: $JWT_SECRET"

#echo "▶️ Generating init.sql for MySQL initialization..."
#mkdir -p mysql
#
#cat > ./mysql/init.sql <<EOF
#CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
#GRANT ALL PRIVILEGES ON shiroTest.* TO '${DB_USERNAME}'@'%';
#FLUSH PRIVILEGES;
#EOF
#
#echo "✅ init.sql generated."
docker-compose down -v
echo "▶️ Starting services via Docker Compose..."
docker-compose -p my-backend-service up -d --build

echo "✅ Deployment completed successfully."
