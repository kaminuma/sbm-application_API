# sbm-application-API

## 📌 Overview

`sbm-application-API` は、**Self-Balanced Memory (SBM) アプリケーション** のバックエンド API です。<br>
ユーザーの生活記録を管理し、データ分析をサポートするために設計されています。

SBM は、日々の活動を記録・振り返ることで、より良い生活習慣を構築することを目的としたプラットフォームです。<br>
API は、データの記録・分析・可視化をサポートし、フロントエンド（Vue.js）と連携して動作します。

---

## 🛠️ Setup & Installation

### **1️⃣ リポジトリをクローン**
```sh
git clone https://github.com/kaminuma/sbm-application_API.git
cd sbm-application-API
```

### **2️⃣ Docker でDBを起動**
```sh
docker-compose up -d
```
MySQLのrootパスワードは `docker-compose.yml` の `MYSQL_ROOT_PASSWORD` を参照してください（デフォルトは `rootpassword`）。

### **3️⃣ アプリケーションのビルド・起動**

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

## ⚠️ トラブルシューティング

- **MySQL接続エラーが出る場合**
  - ローカルMySQL（brew等）を停止し、DockerのMySQLのみを起動してください。
  - パスワードやポート競合に注意してください。
- **DB接続情報**
  - `src/main/resources/application-local.yml` の `spring.datasource` 設定を確認してください。
- **よくあるエラー**
  - `Access denied for user 'root'@'localhost'` → パスワードやDB起動状況を再確認

---

## 📜 License
This project is licensed under the **Apache License 2.0**.
