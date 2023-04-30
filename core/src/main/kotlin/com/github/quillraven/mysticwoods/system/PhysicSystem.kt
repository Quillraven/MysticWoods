package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.system.EntitySpawnSystem.Companion.ACTION_SENSOR
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
    private val aiCmps: ComponentMapper<AIComponent>,
    private val portalCmps: ComponentMapper<PortalComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
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
            entityA in tiledCmps && entityB in collisionCmps && contact.isSensorA && !contact.isSensorB -> {
                tiledCmps[entityA].nearbyEntities += entityB
            }

            entityB in tiledCmps && entityA in collisionCmps && contact.isSensorB && !contact.isSensorA -> {
                tiledCmps[entityB].nearbyEntities += entityA
            }

            // AI entities keep track of their nearby entities to have this information available
            // for their behavior. E.g. a slime entity will attack a player if he comes close
            entityA in aiCmps && entityB in collisionCmps && contact.fixtureA.userData == ACTION_SENSOR -> {
                aiCmps[entityA].nearbyEntities += entityB
            }

            entityB in aiCmps && entityA in collisionCmps && contact.fixtureB.userData == ACTION_SENSOR -> {
                aiCmps[entityB].nearbyEntities += entityA
            }

            // portal collision
            entityA in portalCmps && entityB in playerCmps && !contact.isSensorB -> {
                portalCmps[entityA].triggerEntities += entityB
            }

            entityB in portalCmps && entityA in playerCmps && !contact.isSensorA -> {
                portalCmps[entityB].triggerEntities += entityA
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
            entityA in tiledCmps && contact.isSensorA && !contact.isSensorB -> {
                tiledCmps[entityA].nearbyEntities -= entityB
            }

            entityB in tiledCmps && contact.isSensorB && !contact.isSensorA -> {
                tiledCmps[entityB].nearbyEntities -= entityA
            }

            entityA in aiCmps && contact.fixtureA.userData == ACTION_SENSOR -> {
                aiCmps[entityA].nearbyEntities - entityB
            }

            entityB in aiCmps && contact.fixtureB.userData == ACTION_SENSOR -> {
                aiCmps[entityB].nearbyEntities -= entityA
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