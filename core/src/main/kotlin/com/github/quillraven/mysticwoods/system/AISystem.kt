package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.AIComponent
import com.github.quillraven.mysticwoods.component.DeadComponent

@AllOf([AIComponent::class])
@NoneOf([DeadComponent::class])
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