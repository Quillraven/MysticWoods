package com.github.quillraven.mysticwoods.component

enum class AttackState {
    READY, PREPARE, ATTACKING
}

data class AttackComponent(
    var doAttack: Boolean = false,
    var state: AttackState = AttackState.READY
) {
    fun isReady() = state == AttackState.READY

    fun startAttack() {
        state = AttackState.PREPARE
    }
}