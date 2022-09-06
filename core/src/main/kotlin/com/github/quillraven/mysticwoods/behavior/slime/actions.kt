package com.github.quillraven.mysticwoods.behavior.slime

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute
import com.badlogic.gdx.ai.utils.random.FloatDistribution
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.mysticwoods.behavior.Action
import com.github.quillraven.mysticwoods.component.AIEntity
import com.github.quillraven.mysticwoods.component.AnimationType
import com.github.quillraven.mysticwoods.event.EntityAggroEvent
import ktx.math.vec2

class AttackTask : Action() {
    override fun execute(): Status {
        if (status != Status.RUNNING) {
            aiEntity.attack()
            aiEntity.animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL, true)
            return Status.RUNNING
        }

        if (aiEntity.isAnimationDone()) {
            aiEntity.animation(AnimationType.IDLE)
            aiEntity.stopMovement()
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }
}

class MoveTask(
    @JvmField
    @TaskAttribute(required = true)
    var range: Float = 0f
) : Action() {
    override fun execute(): Status {
        if (status != Status.RUNNING) {
            aiEntity.animation(AnimationType.RUN)
            aiEntity.fireEvent(EntityAggroEvent(aiEntity.entity, aiEntity.target))
            return Status.RUNNING
        }

        aiEntity.checkTargetStillNearby()
        aiEntity.moveToTarget()
        if (aiEntity.inTargetRange(range)) {
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }

    override fun copyTo(task: Task<AIEntity>): Task<AIEntity> {
        (task as MoveTask).range = range
        return task
    }
}

class WanderTask(
    @JvmField
    @TaskAttribute(required = true)
    var range: Float = 0f
) : Action() {
    private val startLoc = vec2()
    private val targetLoc = vec2()

    override fun execute(): Status {
        if (status != Status.RUNNING) {
            if (startLoc.isZero) {
                startLoc.set(aiEntity.location)
            }
            aiEntity.animation(AnimationType.RUN)
            targetLoc.set(startLoc)
            targetLoc.x += MathUtils.random(-range, range)
            targetLoc.y += MathUtils.random(-range, range)
            aiEntity.moveToLocation(targetLoc)
            aiEntity.moveSlow(true)
            return Status.RUNNING
        }

        if (aiEntity.inRange(range, targetLoc)) {
            aiEntity.stopMovement()
            return Status.SUCCEEDED
        } else if (aiEntity.findNearbyEnemy()) {
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }

    override fun end() {
        aiEntity.moveSlow(false)
    }

    override fun copyTo(task: Task<AIEntity>): Task<AIEntity> {
        (task as WanderTask).range = range
        return task
    }
}

class IdleTask(
    @JvmField
    @TaskAttribute(required = true)
    var duration: FloatDistribution? = null
) : Action() {
    private var currentDuration = 0f

    override fun execute(): Status {
        if (status != Status.RUNNING) {
            aiEntity.animation(AnimationType.IDLE)
            currentDuration = duration?.nextFloat() ?: 1f
            return Status.RUNNING
        }

        currentDuration -= GdxAI.getTimepiece().deltaTime
        if (aiEntity.findNearbyEnemy() || currentDuration <= 0f) {
            // enemy nearby or idle time is over -> leave idle behavior
            return Status.SUCCEEDED
        }

        // remain in idle state for the given duration
        return Status.RUNNING
    }

    override fun copyTo(task: Task<AIEntity>): Task<AIEntity> {
        (task as IdleTask).duration = duration
        return task
    }
}
