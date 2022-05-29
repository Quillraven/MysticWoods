package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.event.EntityDeathEvent
import com.github.quillraven.mysticwoods.event.fire

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val animationCmps: ComponentMapper<AnimationComponent>,
    @Qualifier("GameStage") private val stage: Stage,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.max)

        if (lifeCmp.takeDamage > 0f) {
            lifeCmp.life -= lifeCmp.takeDamage
            lifeCmp.takeDamage = 0f
        }

        if (lifeCmp.isDead()) {
            animationCmps.getOrNull(entity)?.let { aniCmp ->
                stage.fire(EntityDeathEvent(aniCmp.atlasKey))
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
}