<div align="center">
<img src='logo.png'alt="FolkPatch logo">

[![Latest Release](https://img.shields.io/github/v/release/matsuzaka-yuki/FolkPatch?label=Release&logo=github)](https://github.com/matsuzaka-yuki/FolkPatch/releases/latest)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/FolkPatch)
[![GitHub License](https://img.shields.io/github/license/matsuzaka-yuki/FolkPatch?logo=gnu)](/LICENSE)

</div>

🌏 **README の言語:** [**English**](./README_EN.md) / [**中文**](./README.md) / [**日本語**](./README_JA.md) / 

FolkPatch - インターフェースの最適化と拡張機能に重視した root 管理ツール。

包括的なドキュメントですぐに開始しましょう。インストール、モジュールの管理、カスタム設定など FolkPatch を快適に使用するための情報はドキュメントに網羅しています。

[📚 完全なドキュメントを読む](https://fp.mysqil.com/) →

<table>
  <tr>
    <td><img alt="" src="docs/1.png"></td>
    <td><img alt="" src="docs/2.png"></td>
    <td><img alt="" src="docs/3.png"></td>
  <tr>
  <tr>
    <td><img alt="" src="docs/4.png"></td>
    <td><img alt="" src="docs/5.png"></td>
    <td><img alt="" src="docs/6.png"></td>
  <tr>
</table>

## 🚀 NEW: コア機能の最適化

> **🎯 スマートなモジュール管理** - boot に埋め込むことなく KPM の読み込みメカニズムを完全に自動化、より安定した root エクスペリエンスを提供します。


### 🔧 モジュールシステムのリファクタリング
- **APM 一括フラッシュシステム:** 一括フラッシュ機能を備えた、Magisk ライクなモジュールシステムをサポート、高い効率性が期待できます
- **KPM の自動読み込み:** 完全に自動化された読み込みメカニズムを備えたカーネルインジェクションでのモジュールのサポートで、手動での埋め込みが不要です
- **完全なモジュールのバックアップ:** すべてのモジュールをワンタップでバックアップ、安心して root アクセスを楽しみましょう

### 📐 インターフェースデザインの最適化
- **カスタム壁紙システム:** カスタム壁紙のサポートでのカスタマイズの強化、パーソナライズされたインターフェースの作成に対応
- **モダン UI デザイン:** スムーズな視覚体験を提供、最適化されたインターフェース設計になっています
- **多言語をサポート:** オンデマンドで複数の言語スタイルを切り替え

### 🛠 機能の強化
- **オンラインモジュールのダウンロード:** 人気のモジュールに素早くアクセスするためにオンラインモジュールダウンロード機能を統合しています
- **グローバルモジュールを除外:** より高速で便利な操作が可能な、グローバルモジュールの除外機能
- **自動更新を削除:** より安定したユーザーエクスペリエンスを実現するために自動更新機能を削除

---

## ✨ 機能

### 🎨 コア機能
- [x] カーネルベースな Android デバイスの root ソリューション
- [x] ARM64 アーキテクチャに対応しているデバイス
- [x] Android カーネル バージョン 3.18 - 6.12 に対応
- [x] APM: Magisk ライクなモジュールシステムに対応
- [x] KPM: カーネルインジェクションのモジュールに対応 (カーネル関数 `inline-hook` と `syscall-table-hook`)

### 🎨 インターフェースとデザイン
- [x] 最適化されたモダンなインターフェース
- [x] カスタム壁紙に対応
- [x] 多言語に対応
- [x] スムーズなユーザーエクスペリエンス
- [x] 美しいビジュアル効果

### 📦 モジュールシステム
- [x] APM を一括でフラッシュ
- [x] 完全な自動 KPM 読み込みメカニズム
- [x] 完全なモジュールのバックアップ機能
- [x] グローバルモジュールの除外
- [x] オンラインモジュールのダウンロード

### ⚡ 技術的な特徴
- [x] [KernelPatch](https://github.com/bmax121/KernelPatch/) のコアをベースにしています
- [x] 安定したエクスペリエンスのために自動更新を削除
- [x] KPM を boot に埋め込みなしで読み込み
- [x] スマートなモジュール管理

## 🚀 ダウンロードとインストール

### 📦 インストール

1. **ダウンロードとインストール:**
   [Releases](https://github.com/matsuzaka-yuki/FolkPatch/releases/latest) の項目から最新の APK をダウンロードしてください

2. **アプリをインストール:**
   直接で APK ファイルを Android デバイスにインストールしてください

3. **使用開始:**
   FolkPatch アプリを開いてガイドに従い、root 設定を完了します

### 📱 システム要件

- **アーキテクチャ:** ARM64 のアーキテクチャに対応
- **カーネル バージョン:** Android カーネル バージョン 3.18 - 6.12 に対応

## 🙏 オープンソースクレジット

このプロジェクトは、以下のオープンソースプロジェクトに基づいています:

- [KernelPatch](https://github.com/bmax121/KernelPatch/) - コアコンポーネント
- [Magisk](https://github.com/topjohnwu/Magisk) - magiskboot と magiskpolicy
- [KernelSU](https://github.com/tiann/KernelSU) - アプリ UI と Magisk モジュールライクのサポート
- [Sukisu-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) - UI デザインのリファレンス
- [APatch](https://github.com/bmax121/APatch) - 上流のブランチ

## 📄 ライセンス

FolkPatch は [GNU General Public License v3 (GPL-3)](http://www.gnu.org/copyleft/gpl.html) に基づいています。

## 💬 コミュニティとディスカッション

### FolkPatch コミュニティとディスカッション
- Telegram チャンネル: [@FolkPatch](https://t.me/FolkPatch)
- QQ Group: 1074588103
