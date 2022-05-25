package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.DeadComponent
import com.github.quillraven.mysticwoods.component.LifeComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.max)

        if (lifeCmp.isDead()) {
            configureEntity(entity) {
                deadCmps.add(it) {
                    if (it in playerCmps) {
                        // revive player after 5 seconds
                        reviveTime = 5f
                    }
                }
            }
        }
    }
}