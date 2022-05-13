package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

@AllOf(components = [PhysicComponent::class, ImageComponent::class])
class PhysicSystem(
    private val physicWorld: World,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
) : IteratingSystem(interval = Fixed(1 / 60f)), ContactListener {
    init {
        physicWorld.setContactListener(this)
    }

    override fun onUpdate() {
        if (physicWorld.autoClearForces) {
            LOG.error { "AutoClearForces must be set to false to guarantee a correct physic step behavior." }
            physicWorld.autoClearForces = false
        }
        super.onUpdate()
        physicWorld.clearForces()
    }

    override fun onTick() {
        super.onTick()
        physicWorld.step(deltaTime, 6, 2)
    }

    // store position before world update for smooth interpolated rendering
    override fun onTickEntity(entity: Entity) {
        val imageCmp = imageCmps[entity]
        val physicCmp = physicCmps[entity]
        val (bodyX, bodyY) = physicCmp.body.position

        imageCmp.image.run {
            setPosition(
                bodyX - width * 0.5f,
                bodyY - height * 0.5f
            )
        }

        if (!physicCmp.impulse.isZero) {
            physicCmp.body.applyLinearImpulse(physicCmp.impulse, physicCmp.body.worldCenter, true)
            physicCmp.impulse.setZero()
        }
    }

    // interpolate between position before world step and real position after world step for smooth rendering
    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val imageCmp = imageCmps[entity]
        val physicCmp = physicCmps[entity]

        imageCmp.image.run {
            val prevX = x
            val prevY = y
            val (bodyX, bodyY) = physicCmp.body.position

            setPosition(
                MathUtils.lerp(prevX, bodyX - width * 0.5f, 1f - alpha),
                MathUtils.lerp(prevY, bodyY - height * 0.5f, 1f - alpha)
            )
        }
    }

    override fun beginContact(contact: Contact) {
        // TODO
    }

    override fun endContact(contact: Contact) {
        // TODO
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        contact.isEnabled = false
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) = Unit

    companion object {
        private val LOG = logger<PhysicSystem>()
    }
}