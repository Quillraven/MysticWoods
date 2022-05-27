package com.github.quillraven.mysticwoods.ai.task

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.mysticwoods.component.AIEntity

class AttackTask : LeafTask<AIEntity>() {
    override fun copyTo(task: Task<AIEntity>) = task

    override fun execute(): Status {
        `object`.attack()
        return Status.SUCCEEDED
    }
}