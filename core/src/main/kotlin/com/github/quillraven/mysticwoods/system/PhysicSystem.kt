package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.system.EntitySpawnSystem.Companion.ACTION_SENSOR
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

class PhysicSystem(
    private val physicWorld: World = inject(),
) : IteratingSystem(
    family = family { all(PhysicComponent, ImageComponent) },
    interval = Fixed(1 / 60f)
), ContactListener {
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
        val physicCmp = entity[PhysicComponent]
        physicCmp.prevPos.set(physicCmp.body.position)

        if (!physicCmp.impulse.isZero) {
            physicCmp.body.applyLinearImpulse(physicCmp.impulse, physicCmp.body.worldCenter, true)
            physicCmp.impulse.setZero()
        }
    }

    // interpolate between position before world step and real position after world step for smooth rendering
    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val imageCmp = entity[ImageComponent]
        val physicCmp = entity[PhysicComponent]

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

    private val Contact.isSensorA: Boolean
        get() = this.fixtureA.isSensor

    private val Contact.isSensorB: Boolean
        get() = this.fixtureB.isSensor

    override fun beginContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity

        when {
            // keep track of nearby entities for tiled collision entities.
            // when there are no nearby entities then the collision object will be removed
            entityA has TiledComponent && entityB has CollisionComponent && contact.isSensorA && !contact.isSensorB -> {
                entityA[TiledComponent].nearbyEntities += entityB
            }

            entityB has TiledComponent && entityA has CollisionComponent && contact.isSensorB && !contact.isSensorA -> {
                entityB[TiledComponent].nearbyEntities += entityA
            }
            // AI entities keep track of their nearby entities to have this information available
            // for their behavior. E.g. a slime entity will attack a player if he comes close
            entityA has AIComponent && entityB has CollisionComponent && contact.fixtureA.userData == ACTION_SENSOR -> {
                entityA[AIComponent].nearbyEntities += entityB
            }

            entityB has AIComponent && entityA has CollisionComponent && contact.fixtureB.userData == ACTION_SENSOR -> {
                entityB[AIComponent].nearbyEntities += entityA
            }
        }
    }

    override fun endContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity

        // same as beginContact but we remove entities instead
        // Note: we cannot add the collision component check in endContact because when an entity
        // gets removed then it does not have any components anymore, but it might be part of the
        // nearbyEntities set.
        // -> simply remove entities all the time because the set will take care of correct removal calls
        when {
            entityA has TiledComponent && contact.isSensorA && !contact.isSensorB -> {
                entityA[TiledComponent].nearbyEntities -= entityB
            }

            entityB has TiledComponent && contact.isSensorB && !contact.isSensorA -> {
                entityB[TiledComponent].nearbyEntities -= entityA
            }

            entityA has AIComponent && contact.fixtureA.userData == ACTION_SENSOR -> {
                entityA[AIComponent].nearbyEntities - entityB
            }

            entityB has AIComponent && contact.fixtureB.userData == ACTION_SENSOR -> {
                entityB[AIComponent].nearbyEntities -= entityA
            }
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