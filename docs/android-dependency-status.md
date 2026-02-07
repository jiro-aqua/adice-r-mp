# Android 依存の残存状況（2026-02-07）

## サマリー
- `commonMain` の `android.*` 依存: **0**
- `androidMain` Kotlin ファイル数: **28**
- `android.*` import を含むファイル数: **17**
- `android.*` import 総数: **33**

現状は「**中〜多め**」の残存です。

## 依存が残っている主な領域
1. エントリポイント / プラットフォーム
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/MainActivity.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/AdiceApplication.kt`

2. モデル / リポジトリ（Android API 依存）
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/ContextModel.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/DictionaryRepository.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/PreferenceRepository.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/SearchRepository.kt`

3. ViewModel（AndroidViewModel 依存）
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/viewmodel/AdiceViewModel.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/viewmodel/PreferencesGeneralViewModel.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/viewmodel/PreferencesDictionaryViewModel.kt`

4. UI（一部 Android API 依存）
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/AboutScreen.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/InstallScreen.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/MainScreen.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/SettingsScreen.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/navigation/AdiceNavigation.kt`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/dialogs/ResultLongClickDialog.kt`

## 剥がし優先度
### 優先度A（すぐ剥がせる）
- `AboutScreen` の `BitmapFactory + assets.open` を Compose Resources 化
- `AboutScreen` の `Intent` フォールバックを削減（`UriHandler` 優先）
- `InstallScreen` の `Resources/Build` 言語判定を共通ロジック化
- `AdiceNavigation` の `android.net.Uri` 依存を文字列パースへ置換
- `SearchRepository` の `android.util.Log` を抽象ログへ置換

### 優先度B（設計変更が必要）
- `AndroidViewModel` を `ViewModel` 化（DI/Factory 前提）

### 優先度C（Android固有で残りやすい）
- SAF（`Uri`, `OpenableColumns`）依存: `DictionaryRepository`
- SharedPreferences 依存: `PreferenceRepository`
- Activity/Intent 受け口: `MainActivity`

## 備考
- `commonMain` は既に Android 直依存がないため、今後の作業は `androidMain` の薄層化が中心。
- 短期効果は「優先度A」を先に削る方が高い。
