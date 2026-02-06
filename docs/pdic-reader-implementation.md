# PDIC/Unicode 実装メモ (aDiceR)

**目的**
PDIC/Unicode 仕様書（`docs/pdicu-dic.md`）と実装の突き合わせ結果をまとめる。実装の挙動と、仕様のうち読み取りに関わる部分の対応を記録する。

**対象ソース**
- `app/src/main/java/jp/sblo/pandora/dice/Dice.java`
- `app/src/main/java/jp/sblo/pandora/dice/Header.java`
- `app/src/main/java/jp/sblo/pandora/dice/Index.java`
- `app/src/main/java/jp/sblo/pandora/dice/IndexCache.java`
- `app/src/main/java/jp/sblo/pandora/dice/BlockCache.java`
- `app/src/main/java/jp/sblo/pandora/dice/Natives.java`
- `app/src/main/cpp/jp_sblo_pandora_dice_Natives.cpp`
- `app/src/main/java/jp/gr/aqua/adice/model/DictionaryRepository.kt`

**対応辞書の前提**
- `Dice.open` は `version` の上位バイトが `0x06` 以上で、`os == 0x20` の辞書のみ受け付ける（BOCU-1 の Unicode 辞書前提）。
- `Header.load` は ver4/5/6 を読めるが、アプリ側は ver6 のみ使用。
- `Charset.forName("BOCU-1")` を使用するため、BOCU-1 の Charset プロバイダが利用可能である必要がある。

**ヘッダー部（仕様 3.4）**
- 読み込みはファイル先頭 256 バイトのみ。`headername`/`dictitle` は Shift_JIS でデコード。
- 参照フィールドは `version`, `lword`, `ljapa`, `block_size`, `index_block`, `header_size`, `extheader`, `index_blkbit`, `nindex2`, `nblock2` など。
- `attrlen == 1` のときのみ有効とみなす。
- `header_size` と `extheader` を使ってインデックス部の開始位置を算出する。
- `dictype` のビットや暗号化フラグは未検証。

**インデックス部（仕様 3.6）**
- 開始位置は `header_size + extheader`。
- サイズは `block_size * index_block`。
- インデックス要素数は `nindex2` を使用。
- ブロック番号サイズは `index_blkbit` により 2 or 4 バイトを選択。
- インデックスデータは `IndexCache` で 1KB 単位にキャッシュ（512KB 以下なら全読み込み）。
- ポインタ配列 `mIndexPtr` はインデックス内の NULL 区切りを走査して生成。
- 生成したポインタ配列は `cacheDir/<辞書名>.idx` に保存・再利用（サイズは `(nindex+1)*4`）。
- 比較処理は BOCU-1 のバイト列を unsigned 比較。検索語が一致し、次のバイトが `\t` の場合も一致扱い（見出語検索キーのタブ分割に対応）。

**データ部 / ブロック（仕様 3.7）**
- データ開始位置は `header_size + extheader + index_size`。
- データブロック先頭 2 バイトをブロック長として解釈し、`len * block_size` を読み込む。
- ブロック長の最上位ビットが立っている場合は 32bit 長の可能性を示すが、実装は下位 15bit のみ利用。
- ブロックキャッシュは LRU 風（1000 件）で SoftReference を保持。

**レコード構造（Unicode 実装）**
```text
field_len(2 or 4)  // ブロック先頭の最上位ビットで決定
comp_len(1)
headword_attr(1)
headword(BOCU-1, NULL終端, 前項との差分圧縮)
translation / extension...
```
- `comp_len` は同一ブロック内の前見出語の先頭一致長。`mCompbuff` に前回値を保持して復元。
- `headword` に `\t` が含まれる場合、左側を見出語、右側を表示用文字列として分離。

**訳語・拡張部の扱い（仕様 3.7.5以降）**
- `headword_attr` に `0x10` が立っている場合を拡張構成として扱う。
- 拡張構成では訳語は NULL 終端で、続く拡張属性を `0x80` が出るまで読む。
- `拡張属性` の `0x10`（バイナリ）や `0x40`（圧縮）が立っている場合は詳細解析を行わず打ち切り。
- 実装が解釈する拡張種別は次の 2 つのみ。
- `0x01`: 用例（NULL 終端テキスト）
- `0x02`: 発音記号（NULL 終端テキスト）
- それ以外の拡張（リンクデータ、バイナリ、圧縮等）は未対応。
- 拡張構成でない場合、訳語はレコード末尾までをそのまま使用（NULL 終端なし）。
- 文字列は BOCU-1 でデコードし、訳語と用例は `\r` を除去する。

**検索フロー**
- インデックス部を二分探索（最大 32 反復）で該当ブロックを決定。
- ブロック内は逐次検索で一致語を抽出。
- 検索結果数は `SetSearchMax` による上限を適用。

**未対応・注意点**
- 暗号化辞書、逆リファレンス辞書、リファレンス登録語は未実装。
- 拡張部のバイナリ／圧縮データ、リンクデータは解析しない。
- `dicorder`（辞書順・大小同一視など）は考慮しない。
- 32bit ブロック長を完全には扱っていない（MSB を落とすのみ）。

**同梱辞書のヘッダー確認（参考）**
- `dictionaries/*.dic` は `version=0x060A`, `block_size=1024`, `header_size=1024`, `os=0x20` を確認。
