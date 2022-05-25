package com.github.quillraven.mysticwoods.component

data class LifeComponent(
    var life: Float = 30f,
    var max: Float = 30f,
    var regeneration: Float = 1f,
) {
    fun isDead(): Boolean = life <= 0f
}