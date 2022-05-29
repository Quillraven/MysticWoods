package com.github.quillraven.mysticwoods.behavior

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.mysticwoods.component.AIEntity

abstract class Action : LeafTask<AIEntity>() {
    val aiEntity: AIEntity
        get() = `object`

    override fun copyTo(task: Task<AIEntity>) = task
}
