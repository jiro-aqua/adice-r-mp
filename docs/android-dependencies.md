# Android 依存の棚卸し（KMP 移植前）

## 目的

KMP 移植にあたり、現状の Android 依存箇所を再確認し、共有化対象と Android 専用層の切り分けの起点にする。

## 依存サマリー

- Android framework: `Application`/`Context`/`Resources`/`AssetManager`/`ContentResolver`/`Uri`/`Intent`/`Activity`/`ClipboardManager`/`Toast`/`Build`/`WebView` など。
- AndroidX: `AndroidViewModel`/`PreferenceManager`/`ActivityResultContracts`/`Navigation`/`Compose Android` など。
- Android リソース: `R`、`res/`、`assets/`、`AndroidManifest.xml`。
- 端末固有 API: `android.graphics.Typeface`、`android.text.Html`、`android.util.Log`。
- JDK: `java.io.*`、`java.util.*`、`java.util.regex.*`、`java.nio.*`（置換対象）、`java.lang.StringBuilder` など。

## ファイル別リスト（主な依存箇所）

- `app/src/main/AndroidManifest.xml`: Android アプリのマニフェスト定義。
- `app/src/main/res/`: Android リソース（文字列、レイアウト、メニュー、アニメーション、drawable）。
- `app/src/main/assets/`: Android アセット（辞書関連ファイル）。
- `app/src/main/java/jp/gr/aqua/adice/AdiceApplication.kt`: `Application`/`Context` 初期化。
- `app/src/main/java/jp/gr/aqua/adice/MainActivity.kt`: `Activity`、`Intent`、`SearchManager`、Compose Android。
- `app/src/main/java/jp/gr/aqua/adice/model/ContextModel.kt`: `Context`/`Resources`/`AssetManager`/`ContentResolver`/`cacheDir`/`filesDir`。
- `app/src/main/java/jp/gr/aqua/adice/model/DictionaryRepository.kt`: `ContentResolver`/`Uri`/`OpenableColumns`/ファイル配置（`filesDir`/`cacheDir`）。
- `app/src/main/java/jp/gr/aqua/adice/model/PreferenceRepository.kt`: `SharedPreferences`/`PreferenceManager`/`BuildConfig`/`R`。
- `app/src/main/java/jp/gr/aqua/adice/model/SearchRepository.kt`: `Typeface`/`Html`/`Log`/`R`/`assets`。
- `app/src/main/java/jp/gr/aqua/adice/model/ResultModel.kt`: `Typeface`（Android フォント依存）。
- `app/src/main/java/jp/gr/aqua/adice/viewmodel/AdiceViewModel.kt`: `AndroidViewModel`/`Application`/`viewModelScope`。
- `app/src/main/java/jp/gr/aqua/adice/viewmodel/PreferencesGeneralViewModel.kt`: `AndroidViewModel`/`Application`/`Uri`。
- `app/src/main/java/jp/gr/aqua/adice/viewmodel/PreferencesDictionaryViewModel.kt`: `AndroidViewModel`/`Application`。
- `app/src/main/java/jp/gr/aqua/adice/ui/navigation/AdiceNavigation.kt`: `Uri`/`androidx.navigation`。
- `app/src/main/java/jp/gr/aqua/adice/ui/screens/MainScreen.kt`: `Activity`/`Build`/`LocalContext`/`BackHandler`。
- `app/src/main/java/jp/gr/aqua/adice/ui/screens/SettingsScreen.kt`: `Uri`/`Toast`/`ActivityResultContracts`/`LocalContext`。
- `app/src/main/java/jp/gr/aqua/adice/ui/screens/DictionarySettingsScreen.kt`: `Toast`/`LocalContext`。
- `app/src/main/java/jp/gr/aqua/adice/ui/screens/InstallScreen.kt`: `stringResource`（Android リソース）。
- `app/src/main/java/jp/gr/aqua/adice/ui/screens/AboutScreen.kt`: `stringResource`（Android リソース）。
- `app/src/main/java/jp/gr/aqua/adice/ui/dialogs/ResultLongClickDialog.kt`: `ClipboardManager`/`Intent`/`LocalContext`。
- `app/src/main/java/jp/gr/aqua/adice/ui/components/WebViewWrapper.kt`: `WebView`/`AndroidView`（Android 固有 UI）。

## JDK 依存の棚卸し（KMP 共有化の観点）

### PDIC コア（KMP 化の主対象）

- `app/src/main/java/jp/sblo/pandora/dice/` 配下:
  - `java.io.*`（`RandomAccessFile`/`FileInputStream`/`FileOutputStream` など）: `okio` へ置換対象。
  - `java.nio.*`（`ByteBuffer` など）: `ByteArray` + 明示的なエンディアン操作へ置換対象。
  - `java.util.*`（`Vector`/`Hashtable`/`Stack`/`Enumeration`/`Date` 等）: `kotlin.collections`/`kotlin.time`/`kotlinx.datetime` 等への置換候補。
  - `java.util.regex.*`: Kotlin `Regex` への置換候補。

### アプリ層（現状 Android 依存を含む）

- `app/src/main/java/jp/gr/aqua/adice/model/DictionaryRepository.kt`: `java.io.File`/`FileInputStream`/`FileOutputStream`。
- `app/src/main/java/jp/gr/aqua/adice/model/SearchRepository.kt`: `java.io.File`/`java.util.*`。
- `app/src/main/java/jp/gr/aqua/adice/model/ResultModel.kt`: `java.lang.StringBuilder`/`java.util.regex.Pattern`。
- `app/src/main/java/jp/gr/aqua/adice/model/PreferenceRepository.kt`: `java.util.regex.Pattern`。

## 共有化・分離の当たり（次の作業の論点）

- `ContextModel`/`PreferenceRepository`/`DictionaryRepository` は KMP での I/O 抽象化（`okio` + `FileSystem`）と設定保存の置換が必要。
- `SearchRepository`/`ResultModel` の `Typeface`/`Html`/`Log` は共通層では置換・分離が必要。
- UI 層は Compose MP へ寄せつつ、`WebView`/`ActivityResult`/`LocalContext` などは Android 側に隔離。
## 共有化・分離の当たり（次の作業の論点）

- `ContextModel`/`PreferenceRepository`/`DictionaryRepository` は KMP での I/O 抽象化（`okio` + `FileSystem`）と設定保存の置換が必要。
- `SearchRepository`/`ResultModel` の `Typeface`/`Html`/`Log` は共通層では置換・分離が必要。
- UI 層は Compose MP へ寄せつつ、`WebView`/`ActivityResult`/`LocalContext` などは Android 側に隔離。
