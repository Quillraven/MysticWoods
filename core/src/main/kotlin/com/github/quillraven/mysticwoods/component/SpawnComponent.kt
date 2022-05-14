package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Vector2
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import ktx.math.vec2

enum class SpawnType {
    UNDEFINED, PLAYER, SLIME
}

data class SpawnCfg(
    val atlasKey: String,
    val scaleSize: Float = 1f,
    val scaleSpeed: Float = 1f,
    val scalePhysic: Vector2 = vec2(0.3f, 0.3f),
    val physicOffset: Vector2 = vec2(0f, -2f * UNIT_SCALE),
) {
    companion object {
        const val DEFAULT_SPEED = 2f
    }
}

data class SpawnComponent(
    var type: SpawnType = SpawnType.UNDEFINED,
    var location: Vector2 = vec2()
)