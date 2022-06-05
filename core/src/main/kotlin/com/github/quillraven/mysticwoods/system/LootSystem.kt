package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.AnimationComponent
import com.github.quillraven.mysticwoods.component.AnimationType
import com.github.quillraven.mysticwoods.component.LootComponent
import com.github.quillraven.mysticwoods.event.EntityLootEvent
import com.github.quillraven.mysticwoods.event.fire

@AllOf([LootComponent::class])
class LootSystem(
    private val lootCmps: ComponentMapper<LootComponent>,
    private val aniCmps: ComponentMapper<AnimationComponent>,
    @Qualifier("GameStage") private val stage: Stage
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(lootCmps[entity]) {
            if (interactEntity == null) {
                return
            }

            aniCmps[entity].run {
                nextAnimation(AnimationType.OPEN)
                mode = Animation.PlayMode.NORMAL
            }
            stage.fire(EntityLootEvent())

            configureEntity(entity) { lootCmps.remove(it) }
        }
    }
}