package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.mysticwoods.component.StateComponent

class StateSystem : IteratingSystem(family { all(StateComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val stateCmp = entity[StateComponent]

        if (stateCmp.stateMachine.currentState != stateCmp.nextState) {
            stateCmp.stateMachine.changeState(stateCmp.nextState)
        }

        stateCmp.stateMachine.update()
    }
}