# sbm-application-API

[![Version](https://img.shields.io/badge/version-0.2.0-blue.svg)](https://github.com/kaminuma/sbm-application_API)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://openjdk.java.net/)

## 📌 Overview

`sbm-application-API` は、**Self-Balanced Memory (SBM) アプリケーション** のバックエンド API です。<br>
ユーザーの生活記録を管理し、データ分析をサポートするために設計されています。

SBM は、日々の活動を記録・振り返ることで、より良い生活習慣を構築することを目的としたプラットフォームです。<br>
API は、データの記録・分析・可視化をサポートし、**Webフロントエンド**（Vue.js）と**Androidネイティブアプリ**の両方と連携して動作します。

## ✨ Features

### 🔐 認証機能
- **JWT認証**: セキュアなトークンベース認証
- **Google OAuth2認証**: WebとAndroidアプリ共通の認証フロー
- **ユーザー登録・ログイン**: ローカルアカウント管理

### 📊 データ管理
- **生活記録管理**: 日々の活動データの記録・取得
- **AI分析連携**: Gemini APIを活用したデータ分析
- **データ可視化サポート**: フロントエンドでの分析結果表示

### 🔧 技術仕様
- **RESTful API**: 標準的なREST API設計
- **データベース**: MySQL 8.0 + MyBatis
- **マイグレーション**: Flyway対応
- **セキュリティ**: Spring Security + OAuth2
- **ファイル処理**: Apache POI（Excel/CSV対応）

---

## 🛠️ Setup & Installation

### **1️⃣ リポジトリをクローン**
```sh
git clone https://github.com/kaminuma/sbm-application_API.git
cd sbm-application-API
```

### **2️⃣ 環境変数の設定**
Google OAuth2認証を使用する場合は、以下の環境変数を設定してください：
```sh
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

### **3️⃣ Docker でDBを起動**
```sh
docker-compose up -d
```
MySQLのrootパスワードは `docker-compose.yml` の `MYSQL_ROOT_PASSWORD` を参照してください（デフォルトは `rootpassword`）。

### **4️⃣ アプリケーションのビルド・起動**

#### 共通（どのエディタでもOK）
```sh
./gradlew build
./gradlew bootRun
```

#### VS Codeの場合
1. 拡張機能「Extension Pack for Java」などをインストール
2. プロジェクトフォルダを開く
3. 必要に応じて `src/main/java/importApp/ImportAppApplication.java` を右クリックして「Javaで実行」

#### IntelliJの場合
- プロジェクトを開き、`ImportAppApplication.java` を右クリックして「実行」

---

## 🔌 API エンドポイント

### 認証
- `POST /api/v1/auth/register` - ユーザー登録
- `POST /api/v1/auth/login` - ローカルログイン
- `GET /oauth2/authorization/google` - Google認証開始
- `POST /api/v1/auth/oauth2/session` - OAuth2セッション取得

### その他のAPI
- その他のエンドポイントについては、アプリケーション起動後に確認可能

---

## 📱 クライアント連携

### Webフロントエンド
- Vue.jsベースのSPAアプリケーション
- Google OAuth2認証対応済み

### Androidネイティブアプリ
- Kotlin + Jetpack Compose
- Custom Tabsを使用したGoogle認証

---

## 🔧 開発・デプロイ

### 本番環境
- GitHub Actionsによる自動デプロイ
- AWS EC2 + RDS環境
- HTTPS対応済み

### セキュリティ
- OWASP Dependency Check実行
- JWT トークンベース認証
- CORS 設定済み

---

## ⚠️ トラブルシューティング

- **MySQL接続エラーが出る場合**
  - ローカルMySQL（brew等）を停止し、DockerのMySQLのみを起動してください。
  - パスワードやポート競合に注意してください。
- **DB接続情報**
  - `src/main/resources/application-local.yml` の `spring.datasource` 設定を確認してください。
- **Google認証エラー**
  - `GOOGLE_CLIENT_ID`と`GOOGLE_CLIENT_SECRET`の環境変数を確認してください。
- **よくあるエラー**
  - `Access denied for user 'root'@'localhost'` → パスワードやDB起動状況を再確認

---

## 📚 ドキュメント

- [`CHANGELOG.md`](CHANGELOG.md) - 変更履歴
- [`docs/GOOGLE_AUTH_FRONTEND_SPEC.md`](docs/GOOGLE_AUTH_FRONTEND_SPEC.md) - フロントエンドGoogle認証実装仕様
- [`docs/ANDROID_GOOGLE_AUTH_IMPLEMENTATION.md`](docs/ANDROID_GOOGLE_AUTH_IMPLEMENTATION.md) - Android Google認証実装ガイド

---

## 📜 License
This project is licensed under the **Apache License 2.0**.
