package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Vector2
import ktx.math.vec2

data class MoveComponent(
    val speed: Vector2 = vec2(),
    val max: Vector2 = vec2(),
    var angle: Vector2 = vec2(),
    var alpha: Float = 0f,
)