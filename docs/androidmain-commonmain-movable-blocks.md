# androidMain から commonMain へ移動可能なブロック調査

調査日: 2026-02-06  
対象: `composeApp/src/androidMain/kotlin`

## 目的

`androidMain` 内の実装について、`commonMain` へ移動できるブロックを優先度付きで整理する。

## 判定基準

- そのまま移動しやすい: Android API 依存が実質ない。
- 軽い分離で移動可能: `R.string` / `Typeface` / `Uri` などを引数化・抽象化すれば移動可能。
- 中規模リファクタで移動可能: リポジトリ分割や expect/actual などの設計変更が必要。
- 現状は androidMain 維持: Android フレームワーク依存が強い。

## そのまま移動しやすい

- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/dialogs/ResultClickDialog.kt:15`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/components/SearchTextField.kt:24`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/theme/Color.kt:5`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/theme/Theme.kt:7`
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/theme/Type.kt:9`

## 軽い分離で移動可能

- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/ResultModel.kt:32` (`allText`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/ResultModel.kt:47` (`links`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/ResultModel.kt:124` (`Mode`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/components/ResultItems.kt:29` (`R.string` を引数化すれば可)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/components/SearchResultList.kt:15` (`ResultModel` の共通化が前提)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/SettingsScreen.kt:188` (`SettingsCategoryHeader`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/SettingsScreen.kt:199` (`SettingsClickableItem`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/SettingsScreen.kt:224` (`SwitchSettingItem`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/InstallScreen.kt:105` (`@StringRes` 依存の置換が必要)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/DownloadRepository.kt:80` (`findDicEntry`, I/O 入出力の注入が必要)

## 中規模リファクタで移動可能

- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/SearchRepository.kt:157` 以降の検索結果組み立てロジック
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/DictionaryRepository.kt:77` 以降の辞書追加・並び替え・削除ロジック
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/viewmodel/AdiceViewModel.kt:17` の UI 状態定義と状態更新ロジック

## 現状は androidMain 維持が妥当

- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/ContextModel.kt:18` (`Context`/`Resources`/`AssetManager`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/model/DictionaryRepository.kt:50` (`Uri`/`ContentResolver`)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/dialogs/ResultLongClickDialog.kt:22` (Clipboard/Intent)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/ui/screens/MainScreen.kt:105` (BackHandler + Activity 終了)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/MainActivity.kt:22` (Activity/Intent)
- `composeApp/src/androidMain/kotlin/jp/gr/aqua/adice/AdiceApplication.kt:6` (Application 初期化)

## 最短の移行順序（提案）

1. `ui/theme` 3ファイルを `commonMain` へ移動。
2. `ResultClickDialog` と `SearchTextField` を `commonMain` へ移動。
3. `ResultModel` の Android 依存フィールド (`Typeface`) を分離し、`ResultItems` / `SearchResultList` を共通化。
4. リポジトリ層は I/O 抽象を導入したうえで段階的に共通化。
