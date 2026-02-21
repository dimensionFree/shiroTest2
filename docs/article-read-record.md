# 文章阅读记录

## 功能说明

- 当调用 `GET /api/article/find/{id}` 读取文章详情时，后端会自动写入一条阅读记录。
- 阅读记录按“每次读取一条”落库，用于后续做阅读分析与审计。
- 管理端可调用 `GET /api/article/read/detail/{id}` 获取阅读详情（需登录且有 `ARTICLE_READ` 权限）。
- 管理端可调用 `PATCH /api/article/manage/public/{id}?isPublic=true|false` 切换文章公开状态（需 `ARTICLE_EDIT`）。

## 记录字段

- `article_id`：被阅读文章 ID
- `reader_ip`：阅读者 IP（优先取 `X-Forwarded-For`，其次 `X-Real-IP`，最后 `remoteAddr`）
- `read_time`：阅读时间（`DATETIME(6)`）
- `reader_user_id`：登录用户 ID（匿名访问为空）
- `reader_user_agent`：请求头 `User-Agent`

## 数据库

- Flyway 脚本：
  - `src/main/resources/db/migration/V19__add_article_read_record.sql`
  - `src/main/resources/db/migration/V20__change_article_read_time_to_datetime.sql`
- 文章公开状态：
  - `src/main/resources/db/migration/V21__add_article_is_public.sql`
- `article.is_public`：
  - `1`：公开，匿名可读
  - `0`：不公开，匿名不可读（登录用户可读）
- 新表：`article_read_record`
