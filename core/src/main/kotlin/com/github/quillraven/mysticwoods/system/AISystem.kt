package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.AIComponent

@AllOf([AIComponent::class])
class AISystem(
    private val aiCmps: ComponentMapper<AIComponent>
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(aiCmps[entity]) {
            if (treePath.isBlank()) {
                return
            }

            behaviorTree.step()
        }
    }
}