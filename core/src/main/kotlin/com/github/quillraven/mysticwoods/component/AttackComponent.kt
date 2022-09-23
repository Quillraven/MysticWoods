package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class AttackState {
    READY, PREPARE, ATTACKING, DEAL_DAMAGE
}

data class AttackComponent(
    var doAttack: Boolean = false,
    var damage: Int = 0,
    var delay: Float = 0f,
    var maxDelay: Float = 0f,
    var extraRange: Float = 0f,
    var state: AttackState = AttackState.READY
) : Component<AttackComponent> {
    override fun type() = AttackComponent

    fun isReady() = state == AttackState.READY

    fun startAttack() {
        state = AttackState.PREPARE
    }

    companion object : ComponentType<AttackComponent>()
}