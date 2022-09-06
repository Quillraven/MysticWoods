package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.event.EntityDeathEvent
import com.github.quillraven.mysticwoods.event.EntityTakeDamageEvent
import com.github.quillraven.mysticwoods.event.fire
import ktx.assets.disposeSafely

@AllOf([LifeComponent::class, PhysicComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val animationCmps: ComponentMapper<AnimationComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    @Qualifier("GameStage") private val gameStage: Stage,
) : IteratingSystem() {
    private val damFont = BitmapFont(Gdx.files.internal("damage.fnt")).apply { data.setScale(0.33f) }
    private val damFntStyle = LabelStyle(damFont, Color.WHITE)

    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.max)

        if (lifeCmp.takeDamage > 0f) {
            val physicCmp = physicCmps[entity]
            lifeCmp.life -= lifeCmp.takeDamage
            gameStage.fire(EntityTakeDamageEvent(entity, lifeCmp.takeDamage))
            damageFloatingText(lifeCmp.takeDamage, physicCmp.body.position, physicCmp.size)
            lifeCmp.takeDamage = 0f
        }

        if (lifeCmp.isDead()) {
            animationCmps.getOrNull(entity)?.let { aniCmp ->
                gameStage.fire(EntityDeathEvent(aniCmp.atlasKey))
                aniCmp.nextAnimation(AnimationType.DEATH)
                aniCmp.mode = Animation.PlayMode.NORMAL
            }

            configureEntity(entity) {
                deadCmps.add(it) {
                    if (it in playerCmps) {
                        // revive player after 7 seconds
                        reviveTime = 7f
                    }
                }
            }
        }
    }

    private fun damageFloatingText(damage: Float, entityPosition: Vector2, entitySize: Vector2) {
        world.entity {
            add<FloatingTextComponent> {
                txtLocation.set(entityPosition.x, entityPosition.y - entitySize.y * 0.5f)
                lifeSpan = 1.5f
                label = Label(damage.toInt().toString(), damFntStyle)
            }
        }
    }

    override fun onDispose() {
        damFont.disposeSafely()
    }
}