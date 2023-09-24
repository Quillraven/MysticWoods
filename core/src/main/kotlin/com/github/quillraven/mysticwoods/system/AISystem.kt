package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.mysticwoods.component.AIComponent
import com.github.quillraven.mysticwoods.component.DeadComponent

class AISystem : IteratingSystem(family { all(AIComponent).none(DeadComponent) }) {
    override fun onTickEntity(entity: Entity) {
        with(entity[AIComponent]) {
            behaviorTree.step()
        }
    }
}