package jp.sblo.pandora.dice

import okio.Path
import okio.Path.Companion.toPath

internal fun String.toOkioPath(): Path = replace('\\', '/').toPath()
