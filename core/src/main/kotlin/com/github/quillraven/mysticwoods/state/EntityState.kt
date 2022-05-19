package com.github.quillraven.mysticwoods.state

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.g2d.Animation
import com.github.quillraven.mysticwoods.component.AIEntity
import com.github.quillraven.mysticwoods.component.AnimationType

enum class EntityState : State<AIEntity> {
    IDLE {
        override fun enter(aiEntity: AIEntity) {
            aiEntity.animation(AnimationType.IDLE)
        }

        override fun update(aiEntity: AIEntity) {
            when {
                aiEntity.wantsToAttack -> aiEntity.state(ATTACK)
                aiEntity.wantsToMove -> aiEntity.state(RUN)
            }
        }
    },
    RUN {
        override fun enter(aiEntity: AIEntity) {
            aiEntity.animation(AnimationType.RUN)
        }

        override fun update(aiEntity: AIEntity) {
            when {
                aiEntity.wantsToAttack -> aiEntity.state(ATTACK)
                !aiEntity.wantsToMove -> aiEntity.state(IDLE)
            }
        }
    },
    ATTACK {
        override fun enter(aiEntity: AIEntity) {
            with(aiEntity) {
                animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL)
                moveCmp.root = true
                startAttack()
            }
        }

        override fun exit(aiEntity: AIEntity) {
            aiEntity.moveCmp.root = false
        }

        override fun update(aiEntity: AIEntity) {
            val attackCmp = aiEntity.attackCmp
            if (attackCmp.isReady() && !attackCmp.doAttack) {
                // done attacking
                aiEntity.changeToPreviousState()
            } else if (attackCmp.isReady()) {
                // start another attack
                aiEntity.resetAnimation()
                attackCmp.startAttack()
            }
        }
    };

    override fun enter(aiEntity: AIEntity) = Unit

    override fun update(aiEntity: AIEntity) = Unit

    override fun exit(aiEntity: AIEntity) = Unit

    override fun onMessage(aiEntity: AIEntity, telegram: Telegram) = false
}