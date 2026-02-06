# BOCU-1 デコーダ実装メモ

## 目的
Kotlin Multiplatform 向けに BOCU-1 デコード処理を自前実装するための設計メモ。  
参照元は `docs/CharsetBOCU1.java`（ICU 実装）であり、ICU 依存部分を排除した構成に落とし込む。

## 参照元の位置
- `docs/CharsetBOCU1.java` の `CharsetDecoderBOCU` がデコードロジックの主本体。
- 必要な定数・補助関数は同一ファイル内に全て定義されている。

## 実装方針（Kotlin）
- ICU の `CharsetDecoder` を使わず、**ByteArray → String**（または CharArray）を返す純粋関数にする。
- 「デコード状態（prev / diff / count / pending bytes）」を明示的な state で管理する。
- KMP では `ByteArray` を入力とし、`MutableList<Char>` / `StringBuilder` で出力する。
- サロゲート変換は Kotlin で計算する（`UTF16.getLeadSurrogate`/`getTrailSurrogate` 相当）。

## 必要な状態
- `prev`: 前回の「予測コードポイント」  
  - 初期値は `BOCU1_ASCII_PREV`（0x40）
- `diff`: 差分の部分値（リードバイト解析結果）
- `count`: 残りトレイルバイト数（0〜3）
- `pending`: 途中まで読んだバイト列（エラー報告やデバッグ用途）

## 必要な定数（ICU 由来）
`docs/CharsetBOCU1.java` から移植する。

- `BOCU1_ASCII_PREV = 0x40`
- `BOCU1_MIN = 0x21`
- `BOCU1_MIDDLE = 0x90`
- `BOCU1_MAX_TRAIL = 0xff`
- `BOCU1_RESET = 0xff`
- `BOCU1_TRAIL_CONTROLS_COUNT = 20`
- `BOCU1_TRAIL_BYTE_OFFSET = (BOCU1_MIN - BOCU1_TRAIL_CONTROLS_COUNT)`
- `BOCU1_TRAIL_COUNT = ((BOCU1_MAX_TRAIL - BOCU1_MIN + 1) + BOCU1_TRAIL_CONTROLS_COUNT)`
- `BOCU1_SINGLE = 64`
- `BOCU1_LEAD_2 = 43`
- `BOCU1_LEAD_3 = 3`
- `BOCU1_REACH_POS_1 = (BOCU1_SINGLE - 1)`
- `BOCU1_REACH_NEG_1 = (-BOCU1_SINGLE)`
- `BOCU1_REACH_POS_2 = (BOCU1_REACH_POS_1 + BOCU1_LEAD_2 * BOCU1_TRAIL_COUNT)`
- `BOCU1_REACH_NEG_2 = (BOCU1_REACH_NEG_1 - BOCU1_LEAD_2 * BOCU1_TRAIL_COUNT)`
- `BOCU1_REACH_POS_3 = (BOCU1_REACH_POS_2 + BOCU1_LEAD_3 * BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT)`
- `BOCU1_REACH_NEG_3 = (BOCU1_REACH_NEG_2 - BOCU1_LEAD_3 * BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT)`
- `BOCU1_START_POS_2 = (BOCU1_MIDDLE + BOCU1_REACH_POS_1 + 1)`
- `BOCU1_START_POS_3 = (BOCU1_START_POS_2 + BOCU1_LEAD_2)`
- `BOCU1_START_POS_4 = (BOCU1_START_POS_3 + BOCU1_LEAD_3)`
- `BOCU1_START_NEG_2 = (BOCU1_MIDDLE + BOCU1_REACH_NEG_1)`
- `BOCU1_START_NEG_3 = (BOCU1_START_NEG_2 - BOCU1_LEAD_2)`

配列:
- `bocu1ByteToTrail[0x00..0x20]`
- `bocu1TrailToByte[0..19]`

## 補助関数（ICU 由来）

### `BOCU1_TRAIL_TO_BYTE(trail)`
```
return if (trail >= BOCU1_TRAIL_CONTROLS_COUNT)
    trail + BOCU1_TRAIL_BYTE_OFFSET
else
    bocu1TrailToByte[trail]
```

### `BOCU1_SIMPLE_PREV(c)`
```
return (c & ~0x7f) + BOCU1_ASCII_PREV
```

### `bocu1Prev(c)`
```
if (c <= 0x309f) return 0x3070
else if (0x4e00 <= c && c <= 0x9fa5) return 0x4e00 - BOCU1_REACH_NEG_2
else if (0xac00 <= c) return (0xd7a3 + 0xac00) / 2
else return BOCU1_SIMPLE_PREV(c)
```

### `BOCU1_PREV(c)`
```
return if (c < 0x3040 || c > 0xd7a3) BOCU1_SIMPLE_PREV(c) else bocu1Prev(c)
```

### `decodeBocu1LeadByte(b)`
リードバイトから `(diff << 2) | count` を返す。
（実装は `docs/CharsetBOCU1.java` の `decodeBocu1LeadByte` をそのまま移植）

### `decodeBocu1TrailByte(count, b)`
トレイルバイトを差分に変換する。
（実装は `docs/CharsetBOCU1.java` の `decodeBocu1TrailByte` をそのまま移植）

## デコードフロー（要約）
1. `prev` を初期化（未設定なら `BOCU1_ASCII_PREV`）。
1. 入力バイト列を順次読み込み。
1. 以下で分岐:
   - **単バイト差分**: `BOCU1_START_NEG_2 <= b < BOCU1_START_POS_2`  
     - `c = prev + (b - BOCU1_MIDDLE)`  
     - `prev = BOCU1_SIMPLE_PREV(c)`
   - **制御コード/スペース**: `b <= 0x20`  
     - そのまま出力  
     - `b != 0x20` なら `prev = BOCU1_ASCII_PREV`
   - **BOCU1_RESET (0xFF)**:  
     - `prev = BOCU1_ASCII_PREV`
   - **複数バイト差分**:
     - `diff = decodeBocu1LeadByte(b)`  
     - `count = diff & 3`  
     - `diff >>= 2`  
     - 残り `count` バイトを `decodeBocu1TrailByte` で加算  
     - `c = prev + diff`
     - `prev = BOCU1_PREV(c)`
1. `c` が `0xFFFF` 超ならサロゲートを出力。

## サロゲート計算（Kotlin）
```
val cp = c - 0x10000
val lead = 0xD800 + (cp ushr 10)
val trail = 0xDC00 + (cp and 0x3FF)
```

## エラー条件（最低限）
- トレイルバイト変換結果が負値になった場合はエラー
- `c > 0x10FFFF` はエラー
- 不正シーケンスの場合は代替文字（`U+FFFD`）で埋めるか例外にする（方針決定が必要）

## 実装後の検証
- Android の `Charset.forName("BOCU-1")` デコード結果とバイト列比較
- `dictionaries/` の既存辞書で検索/結果が一致することをテストで確認

