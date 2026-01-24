<div align="center">
<a href="https://github.com/matsuzaka-yuki/FolkPatch/releases/latest"><img src="logo.png" style="width: 128px;" alt="logo"></a>

<h1 align="center">FolkPatch Manager</h1>

[![Latest Release](https://img.shields.io/github/v/release/matsuzaka-yuki/FolkPatch?label=Release&logo=github)](https://github.com/matsuzaka-yuki/FolkPatch/releases/latest)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/FolkPatch)
[![GitHub License](https://img.shields.io/github/license/matsuzaka-yuki/FolkPatch?logo=gnu)](/LICENSE)

</div>

**Language / 语言:** [日本語](README_JA.md) | [English](README_EN.md) | [中文](README.md)

**FolkPatch** は、新しいコア機能を導入することなくインターフェース設計の最適化と機能の拡張のみを行った [APatch](https://github.com/bmax121/APatch) に基づいた非並列なブランチです。

# メインの機能

- カーネルベースな Android デバイスの root 化ソリューション
- APM: Magisk ライクなモジュールシステムをサポートと一括フラッシュでの高い効率性
- KPM: カーネルインジェクションなモジュールをサポート (カーネル関数の `inline-hook` と `syscall-table-hook`)
- より安定したユーザーエクスペリエンスの実現のために自動更新機能を削除
- カスタム壁紙のサポートによるシステムカスタマイズの強化
- 完全に自動化された KPM 読み込みメカニズムで boot の組み込みが不要
- 完全なモジュールのバックアップで安心して root アクセスが楽しめます
- オンデマンドでの複数の言語スタイルの切り替え
- より高速な操作のためのグローバルモジュールの除外
- オンラインモジュールのダウンロード機能

## ダウンロードとインストール

[リリース](https://github.com/matsuzaka-yuki/FolkPatch/releases/latest)の項目から最新の APK をダウンロードしてください。

## システム要件

- ARM64 アーキテクチャに対応
- Android カーネルバージョン 3.18 - 6.12 に対応

## オープンソースクレジット

このプロジェクトは、以下のオープンソースプロジェクトに基づいています:

- [KernelPatch](https://github.com/bmax121/KernelPatch/) - コア
- [Magisk](https://github.com/topjohnwu/Magisk) - magiskboot と magiskpolicy
- [KernelSU](https://github.com/tiann/KernelSU) - アプリ UI と Magisk モジュールライクのサポート
- [Sukisu-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) - UI デザインのリファレンス
- [APatch](https://github.com/bmax121/APatch) - 上流のブランチ

## ライセンス

FolkPatch は [GNU General Public License v3 (GPL)](http://www.gnu.org/copyleft/gpl.html) に基づいています。

# FolkPatch のディスカッションとコミュニケーション
- Telegram チャンネル: [@FolkPatch](https://t.me/FolkPatch)

# APatch コミュニティ

Telegram チャンネル: [@APatch](https://t.me/apatch_discuss)

FolkPatch に関する質問や提案は、[@FolkPatch](https://t.me/FolkPatch) チャンネルまたは、QQ グループにお寄せください。公式チャンネルに迷惑をかけないようにしてください。
