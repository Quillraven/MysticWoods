package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.CollisionComponent
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent
import com.github.quillraven.mysticwoods.component.TiledComponent
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

@AllOf(components = [PhysicComponent::class, ImageComponent::class])
class PhysicSystem(
    private val physicWorld: World,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val tiledCmps: ComponentMapper<TiledComponent>,
    private val collisionCmps: ComponentMapper<CollisionComponent>,
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
        val physicCmp = physicCmps[entity]
        physicCmp.prevPos.set(physicCmp.body.position)

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
            val (prevX, prevY) = physicCmp.prevPos
            val (bodyX, bodyY) = physicCmp.body.position

            setPosition(
                MathUtils.lerp(prevX, bodyX, alpha) - width * 0.5f,
                MathUtils.lerp(prevY, bodyY, alpha) - height * 0.5f
            )
        }
    }

    private val Fixture.entity: Entity
        get() = this.body.userData as Entity

    override fun beginContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity

        // keep track of nearby entities for tiled collision entities.
        // when there are no nearby entities then the collision object will be removed
        if (entityA in tiledCmps && entityB in collisionCmps && contact.fixtureA.isSensor) {
            tiledCmps[entityA].nearbyEntities += entityB
        } else if (entityB in tiledCmps && entityA in collisionCmps && contact.fixtureB.isSensor) {
            tiledCmps[entityB].nearbyEntities += entityA
        }
    }

    override fun endContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity

        // keep track of nearby entities for tiled collision entities.
        // when there are no nearby entities then the collision object will be removed
        if (entityA in tiledCmps && entityB in collisionCmps && contact.fixtureA.isSensor) {
            tiledCmps[entityA].nearbyEntities -= entityB
        } else if (entityB in tiledCmps && entityA in collisionCmps && contact.fixtureB.isSensor) {
            tiledCmps[entityB].nearbyEntities -= entityA
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        // only allow collision between Dynamic and Static bodies
        contact.isEnabled = (contact.fixtureA.body.type == StaticBody && contact.fixtureB.body.type == DynamicBody) ||
                (contact.fixtureB.body.type == StaticBody && contact.fixtureA.body.type == DynamicBody)
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) = Unit

    companion object {
        private val LOG = logger<PhysicSystem>()
    }
}