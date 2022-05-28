package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.StateComponent

@AllOf([StateComponent::class])
class StateSystem(
    private val stateCmps: ComponentMapper<StateComponent>
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val stateCmp = stateCmps[entity]

        if (stateCmp.stateMachine.currentState != stateCmp.nextState) {
            stateCmp.stateMachine.changeState(stateCmp.nextState)
        }

        stateCmp.stateMachine.update()
    }
}