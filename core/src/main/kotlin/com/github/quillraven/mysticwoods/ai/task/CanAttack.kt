package com.github.quillraven.mysticwoods.ai.task

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.mysticwoods.component.AIEntity

class CanAttack : LeafTask<AIEntity>() {
    override fun copyTo(task: Task<AIEntity>) = task

    override fun execute(): Status {
        if (`object`.canAttack) {
            return Status.SUCCEEDED
        }
        return Status.FAILED
    }
}