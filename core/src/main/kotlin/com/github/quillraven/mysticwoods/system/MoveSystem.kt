package com.github.quillraven.mysticwoods.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent
import ktx.math.component1
import ktx.math.component2

class MoveSystem : IteratingSystem(family { all(MoveComponent, PhysicComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val moveCmp = entity[MoveComponent]
        val physicCmp = entity[PhysicComponent]

        if (moveCmp.cosSin.isZero || moveCmp.root) {
            // no direction for movement or entity is rooted
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
        val slowFactor = if (moveCmp.slow) 0.2f else 1f
        val (cos, sin) = moveCmp.cosSin
        physicCmp.impulse.set(
            mass * (moveCmp.speed * slowFactor * cos - velX),
            mass * (moveCmp.speed * slowFactor * sin - velY)
        )

        // flip image if entity moves left/right
        entity.getOrNull(ImageComponent)?.let { imgCmp ->
            if (cos != 0f) {
                imgCmp.image.flipX = cos < 0
            }
        }
    }
}