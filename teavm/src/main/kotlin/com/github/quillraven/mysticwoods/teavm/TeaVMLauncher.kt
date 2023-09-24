@file:JvmName("TeaVMLauncher")

package com.github.quillraven.mysticwoods.teavm

import com.github.quillraven.mysticwoods.MysticWoods
import com.github.xpenatan.gdx.backends.teavm.TeaApplication
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration

/** Launches the TeaVM/HTML application. */
fun main() {
    val config = TeaApplicationConfiguration("canvas").apply {
        width = 0
        height = 0
    }

    TeaApplication(MysticWoods(), config)
}