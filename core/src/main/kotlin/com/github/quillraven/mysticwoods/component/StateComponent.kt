package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.state.DefaultState
import com.github.quillraven.mysticwoods.state.GlobalState

data class StateEntity(
    val entity: Entity,
    val world: World,
    private val stateCmps: ComponentMapper<StateComponent> = world.mapper(),
    private val animationCmps: ComponentMapper<AnimationComponent> = world.mapper(),
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val attackCmps: ComponentMapper<AttackComponent> = world.mapper(),
    private val lifeCmps: ComponentMapper<LifeComponent> = world.mapper(),
) {
    init {
        lifeCmps.getOrNull(entity)?.let { stateCmps[entity].stateMachine.globalState = GlobalState.CHECK_ALIVE }
    }

    val wantsToMove: Boolean
        get() {
            val moveCmp = moveCmps[entity]
            return !moveCmp.cosSin.isZero
        }

    val wantsToAttack: Boolean
        get() = attackCmps.getOrNull(entity)?.doAttack ?: false

    val isAnimationDone: Boolean
        get() = animationCmps[entity].isAnimationFinished()

    val moveCmp: MoveComponent
        get() = moveCmps[entity]

    val attackCmp: AttackComponent
        get() = attackCmps[entity]

    fun animation(type: AnimationType, mode: PlayMode = PlayMode.LOOP, resetAnimation: Boolean = false) {
        with(animationCmps[entity]) {
            nextAnimation(type)
            this.mode = mode
            if (resetAnimation) {
                stateTime = 0f
            }
        }
    }

    fun resetAnimation() {
        animationCmps[entity].stateTime = 0f
    }

    fun state(newState: EntityState, changeImmediate: Boolean = false) {
        with(stateCmps[entity]) {
            nextState = newState
            if (changeImmediate) {
                stateMachine.changeState(newState)
            }
        }
    }

    fun enableGlobalState(enable: Boolean) {
        if (enable) {
            stateCmps[entity].stateMachine.globalState = GlobalState.CHECK_ALIVE
        } else {
            stateCmps[entity].stateMachine.globalState = null
        }
    }

    fun changeToPreviousState() {
        with(stateCmps[entity]) {
            nextState = stateMachine.previousState
        }
    }

    fun startAttack() {
        attackCmps[entity].startAttack()
    }

    fun isDead(): Boolean = lifeCmps[entity].isDead()
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
) {
    companion object {
        class StateComponentListener(
            private val world: World
        ) : ComponentListener<StateComponent> {
            override fun onComponentAdded(entity: Entity, component: StateComponent) {
                component.stateMachine.owner = StateEntity(entity, world)
            }

            override fun onComponentRemoved(entity: Entity, component: StateComponent) = Unit
        }
    }
}