package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import ktx.math.vec2

data class SpawnCfg(
    val atlasKey: String,
    val scaleSize: Float = 1f,
    val scaleSpeed: Float = 1f,
    val canAttack: Boolean = true,
    val scaleAttackDamage: Float = 1f,
    val attackDelay: Float = 0.2f,
    val attackExtraRange: Float = 0f,
    val lifeScale: Float = 1f,
    val bodyType: BodyType = BodyType.DynamicBody,
    val scalePhysic: Vector2 = vec2(1f, 1f),
    val physicOffset: Vector2 = vec2(0f, 0f),
    val aiTreePath: String = "",
) {
    companion object {
        const val DEFAULT_SPEED = 2f
        const val DEFAULT_LIFE = 15
        const val DEFAULT_ATTACK_DAMAGE = 5
    }
}

data class SpawnComponent(
    var type: String = "",
    var location: Vector2 = vec2()
) : Component<SpawnComponent> {
    override fun type() = SpawnComponent

    companion object : ComponentType<SpawnComponent>()
}