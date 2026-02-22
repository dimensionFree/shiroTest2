# shiroTest2 Backend

## 1. 概要
本プロジェクトは Spring Boot ベースのブログ向けバックエンドです。  
ユーザー管理、記事管理、権限管理、閲覧履歴、アシスタント操作履歴を提供します。

主な技術スタック：

- Java 11
- Spring Boot
- MyBatis-Plus + PageHelper
- MySQL + Flyway
- Redis
- Shiro + JWT
- Maven

## 2. 主な機能

### 2.1 ユーザー認証・認可
- ユーザー登録、ログイン、トークン更新、セッション保活
- Shiro + JWT による認証
- RBAC（ロールベース権限制御）

### 2.2 記事機能
- 記事の作成・更新・削除・参照
- 公開／非公開（`is_public`）制御
- 閲覧詳細・最新閲覧履歴の取得

### 2.3 閲覧履歴（記事）
- 記録項目：
  - アクセス IP（生値）
  - 時刻
  - 国／都道府県／市区（位置情報）
- 書き込み方式：
  - まず Redis の分単位バケットへ保存
  - 定期ジョブで DB に flush
  - 管理 API は `autoFlush=true` で取得前に強制 flush

### 2.4 アシスタント操作履歴（右下キャラ）
- `tap` / `flick` などの操作種別・操作内容を記録
- 記録項目：
  - アクセス IP（生値）
  - 操作タイプ・アクション
  - payload
  - 国／都道府県／市区（位置情報）
- 書き込み方式：
  - チャット以外：Redis 集約後に DB へ
  - チャット系：完全トレースのため直接 DB 保存

### 2.5 記録除外（Ignore IP）
- 除外 IP リストの管理
- 除外対象 IP は閲覧履歴・アシスタント履歴を記録しない

## 3. 時刻・タイムゾーン仕様
- 記録の元時刻は UTC 基準で生成
- DB 保存直前に UTC+9（JST）へ補正
- 目的：実行環境の時差差分による時刻ずれ防止

## 4. ローカル開発用の公開 IP 補助
`localhost` では通常 `127.0.0.1` / プライベート IP しか取得できず、都市判定ができません。  
開発時のみ、位置情報解決に使う IP を上書きできます。

- フラグ：`app.dev.use-public-ip-for-local`（デフォルト `false`）
- 値：`app.dev.public-ip`

注意：
- 上書きは Geo 解決にのみ使用
- DB の `clientIp/readerIp` 生値は変更しない
- 本番環境では無効推奨

対応する環境変数（Spring relaxed binding）：

- `APP_DEV_USE_PUBLIC_IP_FOR_LOCAL=true|false`
- `APP_DEV_PUBLIC_IP=157.65.238.56`

## 5. 必須環境変数
本プロジェクトは主に環境変数で設定します：

- `DB_URL` 例：`jdbc:mysql://localhost:3306`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `JWT_SECRET`
- `TURNSTILE_SECRET`

任意（開発補助）：

- `APP_DEV_USE_PUBLIC_IP_FOR_LOCAL`
- `APP_DEV_PUBLIC_IP`

## 6. ローカル起動

### 6.1 前提
- JDK 11+
- Maven 3.6+
- MySQL 8+
- Redis 6+

### 6.2 起動手順
1. MySQL / Redis を起動
2. 環境変数を設定
3. アプリ起動

```bash
mvn spring-boot:run
```

または：

```bash
mvn clean package
java -jar target/shiroTest-1.0-SNAPSHOT.jar
```

## 7. テスト

### 7.1 全体テスト
```bash
mvn test
```

### 7.2 よく使う対象テスト
```bash
mvn "-Dtest=ArticleReadRecordServiceImplTest,AssistantInteractionRecordServiceImplTest" test
```

## 8. CI/CD（現行構成）
AWS CodePipeline（`backend`）の流れ：

1. Source（GitHub `master`）
2. Build（CodeBuild）
   - `mvn test`
   - `mvn package`
   - Docker イメージを ECR へ push
3. Deploy（CodeDeploy）

関連ファイル：

- `buildspec.yml`
- `appspec.yml`
- `docker-compose.yml`
- `Dockerfile`

補足：
- Build 失敗時は Deploy へ進まないため、通常は直前の成功デプロイ版が稼働継続
- Deploy 失敗時の自動ロールバック有無は CodeDeploy のデプロイグループ設定に依存
