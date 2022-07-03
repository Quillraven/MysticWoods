package com.github.quillraven.mysticwoods.state

import com.badlogic.gdx.graphics.g2d.Animation
import com.github.quillraven.mysticwoods.component.AnimationType
import com.github.quillraven.mysticwoods.component.EntityState
import com.github.quillraven.mysticwoods.component.StateEntity

enum class DefaultState : EntityState {
    IDLE {
        override fun enter(stateEntity: StateEntity) {
            stateEntity.animation(AnimationType.IDLE)
        }

        override fun update(stateEntity: StateEntity) {
            when {
                stateEntity.wantsToAttack -> stateEntity.state(ATTACK)
                stateEntity.wantsToMove -> stateEntity.state(RUN)
            }
        }
    },
    RUN {
        override fun enter(stateEntity: StateEntity) {
            stateEntity.animation(AnimationType.RUN)
        }

        override fun update(stateEntity: StateEntity) {
            when {
                stateEntity.wantsToAttack -> stateEntity.state(ATTACK)
                !stateEntity.wantsToMove -> stateEntity.state(IDLE)
            }
        }
    },
    ATTACK {
        override fun enter(stateEntity: StateEntity) {
            with(stateEntity) {
                animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL)
                moveCmp.root = true
                startAttack()
            }
        }

        override fun exit(stateEntity: StateEntity) {
            stateEntity.moveCmp.root = false
        }

        override fun update(stateEntity: StateEntity) {
            val attackCmp = stateEntity.attackCmp
            if (attackCmp.isReady() && !attackCmp.doAttack) {
                // done attacking
                stateEntity.changeToPreviousState()
            } else if (attackCmp.isReady()) {
                // start another attack
                stateEntity.resetAnimation()
                attackCmp.startAttack()
            }
        }
    },
    DEAD {
        override fun enter(stateEntity: StateEntity) {
            // no need to set the DEATH animation because this is done
            // for any entity in the LifeSystem
            stateEntity.moveCmp.root = true
        }

        override fun update(stateEntity: StateEntity) {
            if (!stateEntity.isDead()) {
                stateEntity.state(RESURRECT)
            }
        }
    },
    RESURRECT {
        override fun enter(stateEntity: StateEntity) {
            stateEntity.enableGlobalState(true)
            stateEntity.animation(AnimationType.DEATH, Animation.PlayMode.REVERSED, true)
        }

        override fun update(stateEntity: StateEntity) {
            if (stateEntity.isAnimationDone) {
                stateEntity.state(IDLE)
                stateEntity.moveCmp.root = false
            }
        }
    };
}

enum class GlobalState : EntityState {
    CHECK_ALIVE {
        override fun update(stateEntity: StateEntity) {
            if (stateEntity.isDead()) {
                stateEntity.enableGlobalState(false)
                stateEntity.state(DefaultState.DEAD, true)
            }
        }
    };
}