package jp.gr.aqua.adicer.mp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform