package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.event.EntityDeathEvent
import com.github.quillraven.mysticwoods.event.EntityTakeDamageEvent
import com.github.quillraven.mysticwoods.event.fire
import ktx.assets.disposeSafely
import ktx.math.vec2

class LifeSystem(
    private val gameStage: Stage = inject("GameStage"),
) : IteratingSystem(family { all(LifeComponent, PhysicComponent).none(DeadComponent) }) {
    private val damFont = BitmapFont(Gdx.files.internal("damage.fnt")).apply { data.setScale(0.33f) }
    private val damFntStyle = LabelStyle(damFont, Color.WHITE)

    override fun onTickEntity(entity: Entity) {
        val lifeCmp = entity[LifeComponent]
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.max)

        if (lifeCmp.takeDamage > 0f) {
            val physicCmp = entity[PhysicComponent]
            lifeCmp.life -= lifeCmp.takeDamage
            gameStage.fire(EntityTakeDamageEvent(entity, lifeCmp.takeDamage))
            damageFloatingText(lifeCmp.takeDamage, physicCmp.body.position, physicCmp.size)
            lifeCmp.takeDamage = 0f
        }

        if (lifeCmp.isDead()) {
            entity.getOrNull(AnimationComponent)?.let { aniCmp ->
                gameStage.fire(EntityDeathEvent(aniCmp.atlasKey))
                aniCmp.nextAnimation(AnimationType.DEATH)
                aniCmp.mode = Animation.PlayMode.NORMAL
            }

            entity.configure {
                it += DeadComponent().apply {
                    if (it has PlayerComponent) {
                        // revive player after 7 seconds
                        reviveTime = 7f
                    }
                }
            }
        }
    }

    private fun damageFloatingText(damage: Float, entityPosition: Vector2, entitySize: Vector2) {
        world.entity {
            it += FloatingTextComponent(
                vec2(entityPosition.x, entityPosition.y - entitySize.y * 0.5f),
                1.5f,
                Label(damage.toInt().toString(), damFntStyle)
            )
        }
    }

    override fun onDispose() {
        damFont.disposeSafely()
    }
}