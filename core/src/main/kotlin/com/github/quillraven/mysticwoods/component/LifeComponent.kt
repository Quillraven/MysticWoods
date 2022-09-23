package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class LifeComponent(
    var life: Float = 30f,
    var max: Float = 30f,
    var regeneration: Float = 1f,
    var takeDamage: Float = 0f,
) : Component<LifeComponent> {
    override fun type() = LifeComponent

    fun isDead(): Boolean = life <= 0f

    companion object : ComponentType<LifeComponent>()
}