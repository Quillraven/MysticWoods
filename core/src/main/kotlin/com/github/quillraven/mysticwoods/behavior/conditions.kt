package com.github.quillraven.mysticwoods.behavior

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute
import com.github.quillraven.mysticwoods.component.AIEntity

abstract class Condition : LeafTask<AIEntity>() {
    val aiEntity: AIEntity
        get() = `object`

    override fun copyTo(task: Task<AIEntity>) = task

    abstract fun condition(): Boolean

    override fun execute(): Status {
        return when {
            condition() -> Status.SUCCEEDED
            else -> Status.FAILED
        }
    }
}

class CanAttack(
    @JvmField
    @TaskAttribute(required = true)
    var range: Float = 0f
) : Condition() {
    override fun condition() = aiEntity.canAttack(range)

    override fun copyTo(task: Task<AIEntity>): Task<AIEntity> {
        (task as CanAttack).range = range
        return task
    }
}


class IsEnemyNearby : Condition() {
    override fun condition() = aiEntity.findNearbyEnemy()
}
