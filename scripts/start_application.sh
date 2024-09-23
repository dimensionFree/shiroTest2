#!/bin/bash
echo "Running Docker container from ECR..."

# 登录到 ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# 拉取镜像
docker pull $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPOSITORY_NAME:latest

# 启动容器
docker run -d -p 80:80 $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPOSITORY_NAME:latest

#!/bin/bash

# 从 Parameter Store 获取数据库连接信息
DB_URL=$(aws ssm get-parameter --name "/myapp/datebase_url" --with-decryption --query "Parameter.Value" --output text)
DB_USERNAME=$(aws ssm get-parameter --name "/myapp/datebase_username" --with-decryption --query "Parameter.Value" --output text)
DB_PASSWORD=$(aws ssm get-parameter --name "/myapp/datebase_pwd" --with-decryption --query "Parameter.Value" --output text)
DEV_MAIL_PASSWORD=$(aws ssm get-parameter --name "/myapp/email_pwd" --with-decryption --query "Parameter.Value" --output text)
DEV_MAIL_USERNAME=$(aws ssm get-parameter --name "/myapp/email_username" --with-decryption --query "Parameter.Value" --output text)

# 运行 Docker 容器并传递环境变量
docker run -d -p 80:80 \
  -e DB_URL="$DB_URL" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e MAIL_USERNAME="$DEV_MAIL_PASSWORD" \
  -e MAIL_PASSWORD="$DEV_MAIL_USERNAME" \
  $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPOSITORY_NAME:latest
