package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.Vector2
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
    private fun setSpeedImpulse(
        speed: Vector2,
        angleDeg: Vector2,
        physicCmp: PhysicComponent
    ) {
        val mass = physicCmp.body.mass
        val (velX, velY) = physicCmp.body.linearVelocity
        val (speedX, speedY) = speed
        physicCmp.impulse.set(
            mass * (speedX * angleDeg.x - velX),
            mass * (speedY * angleDeg.y - velY)
        )
    }

    override fun onTickEntity(entity: Entity) {
        val moveCmp = moveCmps[entity]
        if (moveCmp.stop) {
            if (moveCmp.alpha > 0) {
                // entity is moving -> stop it
                moveCmp.alpha = 0f
                with(physicCmps[entity]) {
                    val mass = body.mass
                    val (velX, velY) = body.linearVelocity
                    impulse.x = mass * (0f - velX)
                    impulse.y = mass * (0f - velY)
                }
            }
            return
        }

        val physicCmp = physicCmps[entity]
        if (moveCmp.alpha >= 1) {
            setSpeedImpulse(moveCmp.max, moveCmp.angleDeg, physicCmp)
        } else {
            // multiply by 0.5f to take 2 seconds instead of 1 to get to maximum speed
            moveCmp.alpha += (deltaTime * 0.5f)
            moveCmp.speed.lerp(moveCmp.max, moveCmp.alpha)
            setSpeedImpulse(moveCmp.speed, moveCmp.angleDeg, physicCmp)
        }

        imgCmps.getOrNull(entity)?.let { imgCmp ->
            if (moveCmp.angleDeg.x != 0f) {
                imgCmp.image.flipX = moveCmp.angleDeg.x < 0
            }
        }
    }
}