package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent
import ktx.math.component1
import ktx.math.component2

@AllOf([MoveComponent::class, PhysicComponent::class])
class MoveSystem(
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val imgCmps: ComponentMapper<ImageComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val moveCmp = moveCmps[entity]
        val physicCmp = physicCmps[entity]

        if (moveCmp.cos == 0f && moveCmp.sin == 0f) {
            // no movement
            if (!physicCmp.body.linearVelocity.isZero) {
                // entity is moving -> stop it
                val mass = physicCmp.body.mass
                val (velX, velY) = physicCmp.body.linearVelocity
                physicCmp.impulse.set(
                    mass * (0f - velX),
                    mass * (0f - velY)
                )
            }
            return
        }

        val mass = physicCmp.body.mass
        val (velX, velY) = physicCmp.body.linearVelocity
        physicCmp.impulse.set(
            mass * (moveCmp.speed * moveCmp.cos - velX),
            mass * (moveCmp.speed * moveCmp.sin - velY)
        )

        // flip image if entity moves left/right
        imgCmps.getOrNull(entity)?.let { imgCmp ->
            if (moveCmp.cos != 0f) {
                imgCmp.image.flipX = moveCmp.cos < 0
            }
        }
    }
}