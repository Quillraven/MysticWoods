package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.AnimationComponent
import com.github.quillraven.mysticwoods.component.AnimationType
import com.github.quillraven.mysticwoods.component.LootComponent
import com.github.quillraven.mysticwoods.event.EntityLootEvent
import com.github.quillraven.mysticwoods.event.fire

class LootSystem(
    private val stage: Stage = inject("GameStage")
) : IteratingSystem(family { all(LootComponent) }) {
    override fun onTickEntity(entity: Entity) {
        with(entity[LootComponent]) {
            if (interactEntity == null) {
                return
            }

            entity[AnimationComponent].run {
                nextAnimation(AnimationType.OPEN)
                mode = Animation.PlayMode.NORMAL
            }
            stage.fire(EntityLootEvent())

            entity.configure { it -= LootComponent }
        }
    }
}