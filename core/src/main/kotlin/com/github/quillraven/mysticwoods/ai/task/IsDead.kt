package com.github.quillraven.mysticwoods.ai.task

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.mysticwoods.component.AIEntity

class IsDead : LeafTask<AIEntity>() {
    override fun copyTo(task: Task<AIEntity>) = task

    override fun execute(): Status {
        return if (`object`.isDead) Status.SUCCEEDED else Status.FAILED
    }
}