package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.AnimationComponent
import com.github.quillraven.mysticwoods.component.DeadComponent
import com.github.quillraven.mysticwoods.component.LifeComponent
import com.github.quillraven.mysticwoods.event.EntityReviveEvent
import com.github.quillraven.mysticwoods.event.fire
import ktx.log.logger

class DeadSystem(
    private val gameStage: Stage = inject("GameStage"),
) : IteratingSystem(family { all(DeadComponent) }) {
    override fun onTickEntity(entity: Entity) {
        if (!(entity has AnimationComponent)) {
            // entity has no special animation
            // -> remove it
            log.debug { "Entity $entity without animation gets removed" }
            entity.remove()
            return
        }

        if (entity[AnimationComponent].isAnimationFinished()) {
            val deadCmp = entity[DeadComponent]
            if (deadCmp.reviveTime == 0f) {
                // animation done and no revival planned
                // -> remove entity
                log.debug { "Entity $entity with animation gets removed" }
                entity.remove()
                return
            }

            deadCmp.reviveTime -= deltaTime
            if (deadCmp.reviveTime <= 0f) {
                // animation done and revival time passed
                // -> revive entity
                log.debug { "Entity $entity gets resurrected" }
                with(entity[LifeComponent]) { life = max }
                entity.configure { it -= DeadComponent }
                gameStage.fire(EntityReviveEvent(entity))
            }
        }
    }

    companion object {
        private val log = logger<DeadSystem>()
    }
}