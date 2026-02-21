# Assistant Context API

## 接口说明
- 方法：`GET`
- 路径：`/api/assistant/context`
- 鉴权：`anon`（无需登录）

## 返回结构
统一使用项目 `Result` 包装：

```json
{
  "code": "200",
  "message": "操作成功",
  "dataContent": {
    "city": "Tokyo",
    "latitude": 35.68,
    "longitude": 139.76,
    "weatherCode": 1,
    "temperature": 22.5
  }
}
```

## 缓存策略（后端）
- `IP -> 地理信息`：24 小时
- `经纬度(保留2位小数) -> 天气`：20 分钟

设计目标：减少前端直连第三方 API 的频率，降低外部依赖抖动对页面体验的影响。
