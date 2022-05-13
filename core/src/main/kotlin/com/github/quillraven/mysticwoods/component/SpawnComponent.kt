package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Vector2
import ktx.math.vec2

enum class SpawnType {
    UNDEFINED, PLAYER, SLIME
}

data class SpawnCfg(
    val atlasKey: String,
    val size: Vector2,
    val speed: Vector2,
)

data class SpawnComponent(
    var type: SpawnType = SpawnType.UNDEFINED,
    var location: Vector2 = vec2()
)