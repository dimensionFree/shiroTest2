# ブログシステムバックエンドプロジェクト

## 概要
このプロジェクトは、ブログの投稿や管理を行うためのシンプルなJavaベースのバックエンドシステムです。主に以下の技術スタックを使用しています：
- Java
- MyBatis
- Redis
- MySQL
- Maven
- flyway
- Shiro (権限管理)
- JWT (JSON Web Token)

## 機能
- ユーザー認証と認可（Shiro + JWTを使用）
- ブログ記事の作成、編集、削除、閲覧
- ロールベースアクセス制御(RBAC)
- Redisを使用したキャッシュ機能
- MyBatisによるデータベース操作

## インストール

### 前提条件
このプロジェクトを実行するためには、以下のソフトウェアが必要です：
- Java 11+
- Maven 3.6+
- MySQL 5.7+
- Redis 3.0+
- Git

### 注意点

#### データソースをセットするため：

### 環境変数の設定
プロジェクトを実行する前に、以下の環境変数を設定する必要があります：

- `DB_HOST`: データベースのホスト
- `DB_USER`: データベースのユーザー名
- `DB_PASSWORD`: データベースのパスワード
- `REDIS_HOST`: Redisサーバーのホスト
- `MAIL_USERNAME`: 認証コード発送用のメール
- `MAIL_PASSWORD`: 認証コード発送用のメールの「アプリ パスワード」
