package com.github.quillraven.mysticwoods.ai.task

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.mysticwoods.component.AIEntity

class DieTask : LeafTask<AIEntity>() {
    override fun copyTo(task: Task<AIEntity>) = task

    // when an AI entity is dead then it simply does nothing
    // such entities move to the DEATH state as part of the StateComponent
    // logic, and therefore they are rooted, play their death animation and will be removed
    override fun execute() = Status.SUCCEEDED
}