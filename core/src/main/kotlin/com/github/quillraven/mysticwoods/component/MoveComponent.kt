package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class MoveComponent(
    var speed: Float = 0f,
    var cos: Float = 0f,
    var sin: Float = 0f,
    var root: Boolean = false,
    var slow: Boolean = false,
) : Component<MoveComponent> {
    override fun type() = MoveComponent

    companion object : ComponentType<MoveComponent>()
}