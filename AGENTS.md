# AGENTS.md

このファイルは、このリポジトリで作業するエージェント向けの共通ガイドです。

## 言語
- ユーザーへの説明・進捗報告・最終報告は日本語で行う。
- コード・識別子・コマンドは既存実装に合わせて英語のままでよい。

## プロジェクト概要
- Kotlin Multiplatform（Compose）構成。
- 主なコード配置:
  - 共通ロジック: `composeApp/src/commonMain`
  - Android固有: `composeApp/src/androidMain`
  - Desktop(JVM)固有: `composeApp/src/jvmMain`

## 実装方針
- 既存のアーキテクチャと命名規則を優先し、大きな設計変更は依頼がある場合のみ行う。
- プラットフォーム依存実装は `androidMain` / `jvmMain` に分離する。
- 文字列リソースを追加・変更する場合、必要に応じて `values` と `values-ja` の両方を更新する。
- UI変更時は、既存のComposeテーマ・コンポーネント構成に整合させる。

## 作業手順
1. 変更前に関連コードを探索し、影響範囲を明確にする。
2. 変更は最小差分で実施する。
3. 変更後は可能な範囲でビルドまたは静的確認を行う。
4. 最終報告では、変更ファイル・要点・未実施検証を明示する。

## 推奨コマンド
- Androidビルド:
  - `./gradlew :composeApp:assembleDebug`
- Android Kotlinコンパイル確認:
  - `./gradlew :composeApp:compileDebugKotlinAndroid`
- Desktop実行:
  - `./gradlew :composeApp:run`

## 注意事項
- 破壊的コマンド（例: `git reset --hard`）は、明示的な依頼なしに実行しない。
- 無関係な既存変更は巻き戻さない。
- エージェントは `gradlew`（`./gradlew` / `gradlew.bat` を含む）を実行しない。
- ソースファイルの改行コードはCRLF
- 