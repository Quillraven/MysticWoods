package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.AnimationComponent
import com.github.quillraven.mysticwoods.component.AttackComponent
import com.github.quillraven.mysticwoods.component.AttackState

@AllOf([AttackComponent::class])
class AttackSystem(
    private val attackCmps: ComponentMapper<AttackComponent>,
    private val animationCmps: ComponentMapper<AnimationComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val attackCmp = attackCmps[entity]
        if (attackCmp.doAttack && attackCmp.state == AttackState.PREPARE) {
            attackCmp.doAttack = false
            attackCmp.state = AttackState.ATTACKING
        }
        val isDone = animationCmps.getOrNull(entity)?.isAnimationFinished() ?: true
        if (isDone) {
            attackCmp.state = AttackState.READY
        }
    }
}