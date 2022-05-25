package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.AnimationComponent
import com.github.quillraven.mysticwoods.component.DeadComponent
import com.github.quillraven.mysticwoods.component.LifeComponent
import com.github.quillraven.mysticwoods.component.StateComponent
import com.github.quillraven.mysticwoods.state.DefaultState
import ktx.log.logger

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val aniCmps: ComponentMapper<AnimationComponent>,
    private val stateCmps: ComponentMapper<StateComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        if (entity !in aniCmps) {
            // entity has no special animation
            // -> remove it
            log.debug { "Entity $entity without animation gets removed" }
            world.remove(entity)
            return
        }

        if (aniCmps[entity].isAnimationFinished()) {
            val deadCmp = deadCmps[entity]
            if (deadCmp.reviveTime == 0f) {
                // animation done and no revival planned
                // -> remove entity
                log.debug { "Entity $entity with animation gets removed" }
                world.remove(entity)
                return
            }

            deadCmp.reviveTime -= deltaTime
            if (deadCmp.reviveTime <= 0f) {
                // animation done and revival time passed
                // -> revive entity
                log.debug { "Entity $entity gets resurrected" }
                stateCmps[entity].nextState = DefaultState.RESURRECT
                with(lifeCmps[entity]) { life = max }
                configureEntity(entity) { deadCmps.remove(it) }
            }
        }
    }

    companion object {
        private val log = logger<DeadSystem>()
    }
}