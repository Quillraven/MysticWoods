package com.github.quillraven.mysticwoods.ai.task

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.github.quillraven.mysticwoods.component.AIEntity

class IsEnemyNearby : LeafTask<AIEntity>() {
    override fun copyTo(task: Task<AIEntity>) = task

    override fun execute(): Status {
        `object`.firstOrNullNearbyEnemy() ?: return Status.FAILED
        return Status.SUCCEEDED
    }
}