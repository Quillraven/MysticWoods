package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.state.DefaultState
import com.github.quillraven.mysticwoods.state.GlobalState

data class StateEntity(
    val entity: Entity,
    val world: World,
) {
    init {
        with(world) {
            entity.getOrNull(LifeComponent)
                ?.let { entity[StateComponent].stateMachine.globalState = GlobalState.CHECK_ALIVE }
        }
    }

    val wantsToMove: Boolean
        get() = with(world) {
            val moveCmp = entity[MoveComponent]
            return !moveCmp.cosSin.isZero
        }

    val wantsToAttack: Boolean
        get() = with(world) { entity.getOrNull(AttackComponent)?.doAttack ?: false }

    val isAnimationDone: Boolean
        get() = with(world) { entity[AnimationComponent].isAnimationFinished() }

    val moveCmp: MoveComponent
        get() = with(world) { entity[MoveComponent] }

    val attackCmp: AttackComponent
        get() = with(world) { entity[AttackComponent] }

    fun animation(type: AnimationType, mode: PlayMode = PlayMode.LOOP, resetAnimation: Boolean = false) = with(world) {
        with(entity[AnimationComponent]) {
            nextAnimation(type)
            this.mode = mode
            if (resetAnimation) {
                stateTime = 0f
            }
        }
    }

    fun resetAnimation() = with(world) {
        entity[AnimationComponent].stateTime = 0f
    }

    fun state(newState: EntityState, changeImmediate: Boolean = false) = with(world) {
        with(entity[StateComponent]) {
            nextState = newState
            if (changeImmediate) {
                stateMachine.changeState(newState)
            }
        }
    }

    fun enableGlobalState(enable: Boolean) = with(world) {
        if (enable) {
            entity[StateComponent].stateMachine.globalState = GlobalState.CHECK_ALIVE
        } else {
            entity[StateComponent].stateMachine.globalState = null
        }
    }

    fun changeToPreviousState() = with(world) {
        with(entity[StateComponent]) {
            nextState = stateMachine.previousState
        }
    }

    fun startAttack() = with(world) {
        entity[AttackComponent].startAttack()
    }

    fun isDead(): Boolean = with(world) { entity[LifeComponent].isDead() }
}

interface EntityState : State<StateEntity> {
    override fun enter(stateEntity: StateEntity) = Unit

    override fun update(stateEntity: StateEntity) = Unit

    override fun exit(stateEntity: StateEntity) = Unit

    override fun onMessage(stateEntity: StateEntity, telegram: Telegram) = false
}

data class StateComponent(
    var nextState: EntityState = DefaultState.IDLE,
    val stateMachine: DefaultStateMachine<StateEntity, EntityState> = DefaultStateMachine()
) : Component<StateComponent> {
    override fun type() = StateComponent

    override fun World.onAdd(entity: Entity) {
        stateMachine.owner = StateEntity(entity, this)
    }

    companion object : ComponentType<StateComponent>()
}