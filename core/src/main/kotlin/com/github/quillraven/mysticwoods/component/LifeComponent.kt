package com.github.quillraven.mysticwoods.component

data class LifeComponent(
    var life: Float = 30f,
    var max: Float = 30f,
    var regeneration: Float = 1f,
    var takeDamage: Float = 0f,
) {
    fun isDead(): Boolean = life <= 0f
}