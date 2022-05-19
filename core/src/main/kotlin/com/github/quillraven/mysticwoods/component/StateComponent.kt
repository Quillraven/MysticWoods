package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.mysticwoods.state.EntityState

data class AIEntity(
    val entity: Entity,
    private val stateCmps: ComponentMapper<StateComponent>,
    private val animationCmps: ComponentMapper<AnimationComponent>,
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val attackCmps: ComponentMapper<AttackComponent>,
) {
    val wantsToMove: Boolean
        get() {
            val moveCmp = moveCmps[entity]
            return moveCmp.cos != 0f || moveCmp.sin != 0f
        }

    val wantsToAttack: Boolean
        get() = attackCmps.getOrNull(entity)?.doAttack ?: false

    val moveCmp: MoveComponent
        get() = moveCmps[entity]

    val attackCmp: AttackComponent
        get() = attackCmps[entity]

    fun animation(type: AnimationType, mode: PlayMode = PlayMode.LOOP) {
        with(animationCmps[entity]) {
            nextAnimation(type)
            this.mode = mode
        }
    }

    fun resetAnimation() {
        animationCmps[entity].stateTime = 0f
    }

    fun state(newState: EntityState) {
        stateCmps[entity].nextState = newState
    }

    fun changeToPreviousState() {
        with(stateCmps[entity]) {
            nextState = stateMachine.previousState
        }
    }

    fun startAttack() {
        attackCmps[entity].startAttack()
    }
}

data class StateComponent(
    var nextState: EntityState = EntityState.IDLE,
    val stateMachine: DefaultStateMachine<AIEntity, EntityState> = DefaultStateMachine()
) {
    companion object {
        class StateComponentListener(
            private val stateCmps: ComponentMapper<StateComponent>,
            private val animationCmps: ComponentMapper<AnimationComponent>,
            private val moveCmps: ComponentMapper<MoveComponent>,
            private val attackCmps: ComponentMapper<AttackComponent>
        ) : ComponentListener<StateComponent> {
            override fun onComponentAdded(entity: Entity, component: StateComponent) {
                component.stateMachine.owner = AIEntity(
                    entity,
                    stateCmps,
                    animationCmps,
                    moveCmps,
                    attackCmps
                )
            }

            override fun onComponentRemoved(entity: Entity, component: StateComponent) = Unit
        }
    }
}