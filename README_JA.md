<div align="center">
<a href="https://github.com/matsuzaka-yuki/FolkPatch/releases/latest"><img src="logo.png" style="width: 800px;" alt="logo"></a>

<h1 align="center">FolkPatch Miuix</h1>

[![Latest Release](https://img.shields.io/github/v/release/matsuzaka-yuki/APatch-Ultra?label=Release&logo=github)](https://github.com/matsuzaka-yuki/APatch-Ultra/releases/latest)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/FolkPatch)
[![GitHub License](https://img.shields.io/github/license/bmax121/APatch?logo=gnu)](/LICENSE)

</div>

**Language / 语言:** [中文](README.md) | [English](README_EN.md) | [日本語](README_JA.md)

**FolkPatch** は [APatch](https://github.com/bmax121/APatch) をベースに開発された拡張型の非並列ブランチです。新たなコア機能を導入せずにインターフェース設計の最適化と機能の拡張を行っています。

## 主な特徴
- MiuiX ライブラリをベースにビルド
- カーネルベースの Android デバイスの root ソリューション
- APM: Magisk ライクなモジュールシステムに対応、一括フラッシュも搭載しており操作性を向上
- KPM: カーネルのインジェクションコードに対応 (カーネル関数 `inline-hook` と `syscall-table-hook`)
- より優れたカスタマイズシステム、カスタム壁紙に対応
- 複数の言語に対応、自由に切り替え可能
- オンラインモジュールダウンロード機能
- Monet のカラー抽出機能

## ダウンロードとインストール

最新の APK を [Releases ページ](https://github.com/matsuzaka-yuki/FolkPatch/releases/latest)からダウンロードしてください。

## システム要件

- ARM64 アーキテクチャに対応
- Android カーネル バージョン 3.18 - 6.12 に対応

## オープンソース情報

このプロジェクトは、以下のオープンソースプロジェクトに基づいています:

- [KernelPatch](https://github.com/bmax121/KernelPatch/) - コアコンポーネント
- [Magisk](https://github.com/topjohnwu/Magisk) - magiskboot と magiskpolicy
- [KernelSU](https://github.com/tiann/KernelSU) - アプリの UI と Magisk ライクなモジュールの対応
- [APatch](https://github.com/bmax121/APatch) - 上流のブランチ

## ライセンス

FolkPatch は [GNU General Public License v3 (GPL-3)](http://www.gnu.org/copyleft/gpl.html) に基づいています。

## FolkPatch ディスカッションとコミュニケーション

- Telegram チャンネル: [@FolkPatch](https://t.me/FolkPatch)
- QQ グループ: 1074588103

## APatch コミュニティ

チャンネル: [@APatch](https://t.me/apatch_discuss)

FolkPatch に関する問題や提案は [@FolkPatch](https://t.me/FolkPatch) のチャンネルまたは QQ グループに報告してください。公式のチャンネルに迷惑をかけないようにしてください。
