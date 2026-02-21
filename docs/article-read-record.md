# 文章阅读记录

## 规则说明

- 文章详情统一使用 `GET /api/article/find/{id}`。
- 只有当请求参数 `recordRead=true` 时，后端才会写入阅读记录。
- 不传 `recordRead` 或传 `false` 时，仅返回详情，不记录阅读。

## 前端调用约定

- 阅读页面：`GET /api/article/find/{id}?recordRead=true`
- 编辑页面：`GET /api/article/manage/find/{id}`（或 `GET /api/article/find/{id}` 且不带 `recordRead=true`）

## 阅读记录字段

- `article_id`：文章 ID
- `reader_ip`：阅读者 IP（优先 `X-Forwarded-For`，其次 `X-Real-IP`，最后 `remoteAddr`）
- `read_time`：阅读时间，`DATETIME(6)`
- `reader_user_id`：登录用户 ID（匿名访问为空）
- `reader_user_agent`：请求头 `User-Agent`

## 管理端统计接口

- `GET /api/article/read/detail/{id}`：查询文章阅读总量、去重 IP、按天统计、最近阅读记录（需 `ARTICLE_READ` 权限）。
