package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class DeadComponent(
    var reviveTime: Float = 0f
) : Component<DeadComponent> {
    override fun type() = DeadComponent

    companion object : ComponentType<DeadComponent>()
}