# Kotlin Multiplatform 移行サマリー & プラン

## サマリー

本タスクの目的は、既存の Android 向け辞書ビューアを Kotlin Multiplatform (KMP) へ移行し、Android と Desktop を主な対象として共通化することです（iOS は対象外）。

移行方針は以下の通りです。

- Java で実装されている PDIC アクセス層を KMP へ移行する。`java.nio` 依存は `okio` に置き換える。
- Android では icu4j が統合されているため `Charset.forName("BOCU-1")` で BOCU-1 デコーダが利用可能だが、KMP 向けに独自の BOCU-1 デコーダを Kotlin で実装する。
- JNI 経由のネイティブ実装は、同等の Java 実装をアクティブ化し、JNI/NDK 実装は削除する。
- UI は Android View から Compose Multiplatform へ移行する。Android 依存の詳細実装は一旦保留とする。
- **注意**: 検索処理は BOCU-1 **デコードだけでなくエンコードも必要**（現行は検索語を BOCU-1 へエンコードしてインデックス比較しているため）。
- **前提**: `Index(...)` の `unicode` は常に `true` のため、BOCU-1 以外のエンコーディング分岐は対象外とする。

## プラン（初期）

1. JNI/NDK 廃止
   - Java 側の同等処理を有効化。
   - JNI 呼び出しと `app/src/main/cpp/` 配下の実装を削除。
   - 現状 JNI は `IndexCache.createIndex()` のみに使用されており、同等の Java 実装がコメントアウトで存在する。
1. PDIC アクセス層のユニットテスト整備（前提）
   - 対象は現行 Java 実装と KMP 移行後の両方。
   - スコープは「検索語 → 結果抽出」のフローを優先。
   - テスト辞書は `dictionaries/` の既存ファイルを使用する（必要になれば最小辞書を追加できる構成にする）。
   - Java/KMP 実装で挙動が同じである事を保証する。
1. 目標アーキテクチャ整理
   - KMP 共有モジュールに PDIC 読み取り・検索ロジックを集約。
   - Android/Desktop それぞれの UI 層は Compose MP を前提に構成。
1. KMP 構成の新設
   - `shared`（KMP 共通）、`androidApp`、`desktopApp` を新設・再編。
   - 依存関係の整理（`okio`、`kotlinx.coroutines`、Compose MP、テスト系）。
   - Gradle 設定（`kotlin-multiplatform`、`compose`、ターゲット定義）。
1. PDIC アクセス層の KMP 化
   - `java.nio` を `okio` ベースの I/O に置換。
   - BOCU-1 デコーダを Kotlin 実装として追加（Android の icu4j 統合とは別に独自実装が必要）。
   - **BOCU-1 エンコーダ**（検索語のエンコードに必要）を Kotlin で実装するか、検索方式を変更する。
   - 現行実装は `ByteBuffer`/`RandomAccessFile`/`Charset` に依存しているため、KMP 用に `ByteArray` 操作へ置換が必要。
   - ヘッダー文字列は ASCII、辞書名は BOCU-1（未使用）のため `Shift_JIS` デコードは不要（現行の SJIS デコードは実質影響なしのバグ）。
   - `unicode` は常に `true` 前提のため、BOCU-1 以外の分岐は削除/無視する。
1. UI の Compose MP 化
   - Android はすでに Compose ベースの UI に移行済み（`ui/` 配下）。
   - Compose Multiplatform への共通化と Desktop 向け UI の整備が残課題。
   - Android 依存の詳細実装は保留し、共通 UI を優先。
   - Android 依存のコンポーネント（`AndroidView` の WebView など）は段階的に代替検討する。

## 範囲外

- iOS 対応
- BOCU-1 以外の `icu4j` 機能移植

## 実行可能性メモ

- KMP ビルド構成は現状存在しないため、Android/Desktop を含む新規構成が必要。
- JNI は当初、性能が貧弱な時期の Android デバイス向けに導入されたもの。近年のデバイスでは Java 実装で問題ない前提。
- `WeakReference`/`SoftReference` を使うキャッシュは JVM 依存のため、KMP では別実装（LRU 等）が必要。

## 未解決／方針未決定の問題

- **BOCU-1 エンコードの実装方針**: Kotlin での自前実装にするか、検索アルゴリズムを変更してエンコードを不要にするか未決。
  → エンコードもサンプルソースに従って Kotlin 自前実装で導入する
- **キャッシュ戦略**: 参照キャッシュのサイズ方針、LRU の実装方針、メモリ制約の基準が未決。
- **インデックスキャッシュ I/O 抽象化**: Okio `Source`/`Sink` に統一する設計と API 形状が未決。
- **テスト辞書の配布・取り扱い**: 既存辞書の同梱可否、最小辞書の生成方法、ライセンス確認が未決。
  → 既存辞書同梱可能。
- **BOCU-1 テストの icu4j 依存**: JVM 向けテストは icu4j 依存のまま。KMP では使えないため、将来的にテストベクタ/ダンプ比較などへ置換が必要（保留）。
- **Desktop の辞書配置と権限**: 保存先、初回インストール導線、ファイルダイアログの仕様が未決。
- **WebView 代替**: Desktop での HTML 表示やリンク処理の代替実装が未決。
- **パフォーマンス目標**: インデックス生成・検索・スクロールの許容時間やメモリ上限が未定義。

## 提案する作業順序

各ステップでユニットテストを実行し、回帰がないことを確認する。

1. [完了]BOCU-1 Kotlin デコーダ実装(**JUnit（JDK）テスト**)
   - 既存辞書を用いた差分比較テストを用意する。
   - 実装メモ: `docs/bocu1-decoder-notes.md`
1. [完了]BOCU-1 Kotlin エンコーダ実装(**JUnit（JDK）テスト**)
   - 検索語を BOCU-1 へエンコードしてインデックス比較する現行動作と同等にする。
   - Android の `Charset.forName("BOCU-1")` と出力一致するかテストする。
   - `Charset` 依存 → BOCU-1 Kotlin デコーダ
1. [完了]JNI/NDK を Java 実装へ置換
   - `IndexCache` の Java 実装 `countIndexWords()` を復活させて使用。
   - `Natives` / JNI ヘッダ / C++ 実装 / CMake 設定 / NDK 設定を段階削除。
1. [完了]テスト整備(**JUnit（JDK）テスト**)
   - この時点で Android 依存は消えているはず。JDK 依存は残る。
   - テスト辞書は `dictionaries/` を使用し、必要に応じて最小辞書を追加する。
   - 現在の Java 実装は信用できるので、dictionaries/の下の辞書を、見出し語/result.getTrans()の形にダンプする。
   - 一度きりしか動作しないテストを作ってダンプを生成すること。ダンプは dictionaries/に保存すること。
   - テストの中に辞書を読む処理を作るのでは無く、このダンプ結果と比較するようにテストを行う。
1. [完了]Kotlin への書き替え
   - `Header`（ヘッダ解析）を Kotlin 化
   - `IndexCache`（インデックス読み出し・比較）を Kotlin 化
   - `Index`（検索とブロック解析）を Kotlin 化
   - `Dice` / `DiceFactory`（ライフサイクル管理）を Kotlin 化
   - `Result` / `Element` / `Idic*` を共通化
1. [完了]Android 依存の再棚卸し（KMP 移植前）
   - 既存の Android 依存箇所を再確認し、`docs/android-dependencies.md` に整理する。
   - 共有化対象と Android 専用層の切り分け方針を整理する。
1. 依存ライブラリの置換（Android 依存排除フェーズ）
   - `java.nio` 置換:
   - `RandomAccessFile` → `okio.FileHandle` / `okio.Source`
   - `ByteBuffer` → `ByteArray` + 明示的なリトルエンディアン操作
   - 参照キャッシュ（`WeakReference` / `SoftReference`）→ KMP で動作する LRU 等に置換
   - インデックスキャッシュ入出力（`FileInputStream` / `FileOutputStream`）→ Okio `Source` / `Sink` へ抽象化
1. KMP 構成の新設
   - プロジェクト構成を KMP(Android/Desktop)向けに構成を変更
   - モジュール構成: `shared` / `androidApp` / `desktopApp` を想定。
   - ターゲット: `android()`, `jvm("desktop")` を基本とする。
   - 依存整理: `okio`, `kotlinx.coroutines`, Compose MP, テスト（`kotlin.test`）。
   - 既存 `app/` の Gradle を KMP 構成に合わせて段階移行する。
   - テストを `commonTest`/ に寄せ、KMP で共有できる形にする。
1. Compose MP への共通化と Desktop 対応
   - 既存 Compose UI を Compose MP へ共有できる形に整理。
   - Desktop 向け UI を Compose MP で追加。
   - Android 依存コンポーネント（WebView 等）を代替/分離。
