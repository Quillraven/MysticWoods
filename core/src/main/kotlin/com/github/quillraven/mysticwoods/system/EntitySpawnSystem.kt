package com.github.quillraven.mysticwoods.system

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.actor.FlipImage
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.physicCmpFromImage
import com.github.quillraven.mysticwoods.component.SpawnCfg.Companion.DEFAULT_ATTACK_DAMAGE
import com.github.quillraven.mysticwoods.component.SpawnCfg.Companion.DEFAULT_LIFE
import com.github.quillraven.mysticwoods.component.SpawnCfg.Companion.DEFAULT_SPEED
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import ktx.app.gdxError
import ktx.box2d.box
import ktx.box2d.circle
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.id
import ktx.tiled.layer
import ktx.tiled.x
import ktx.tiled.y
import kotlin.math.roundToInt

class EntitySpawnSystem(
    private val atlas: TextureAtlas = inject("GameAtlas"),
    private val physicWorld: World = inject(),
    private val rayHandler: RayHandler = inject(),
) : EventListener, IteratingSystem(family { all(SpawnComponent) }) {
    private val cachedCfgs = mutableMapOf<String, SpawnCfg>()
    private val cachedSizes = mutableMapOf<String, Vector2>()

    override fun onTickEntity(entity: Entity) {
        with(entity[SpawnComponent]) {
            val cfg = spawnCfg(type)
            val relativeSize = size(cfg.atlasKey)
            LOG.debug { "Spawning entity of type $type with size $relativeSize" }

            world.entity {
                it += ImageComponent().apply {
                    image = FlipImage().apply {
                        setScaling(Scaling.fill)
                        setPosition(location.x, location.y)
                        setSize(relativeSize.x * cfg.scaleSize, relativeSize.y * cfg.scaleSize)
                    }
                }

                it += AnimationComponent().apply {
                    nextAnimation(cfg.atlasKey, AnimationType.IDLE)
                }

                it += physicCmpFromImage(physicWorld, it[ImageComponent].image, cfg.bodyType) { cmp, width, height ->
                    val w = width * cfg.scalePhysic.x
                    val h = height * cfg.scalePhysic.y
                    cmp.size.set(w, h)
                    cmp.offset.set(cfg.physicOffset)

                    // hit box
                    box(w, h, cmp.offset) {
                        isSensor = cfg.bodyType != BodyDef.BodyType.StaticBody
                        userData = HIT_BOX_SENSOR
                        filter.categoryBits = cfg.categoryBit
                    }

                    if (cfg.bodyType != BodyDef.BodyType.StaticBody) {
                        // collision box
                        val collH = h * 0.4f
                        COLLISION_OFFSET.set(cmp.offset)
                        COLLISION_OFFSET.y -= h * 0.5f - collH * 0.5f
                        box(w, collH, COLLISION_OFFSET) { filter.categoryBits = cfg.categoryBit }
                    }
                }

                if (cfg.hasLight) {
                    val dist = 5f..6.5f
                    val physicCmp = it[PhysicComponent]
                    it += LightComponent(
                        distance = dist,
                        light =
                        PointLight(rayHandler, 64, LightComponent.lightColor, dist.endInclusive, 0f, 0f).apply {
                            attachToBody(physicCmp.body, 0f, -0.25f)
                            // softness allows light to go through objects
                            // setSoftnessLength(3.5f)
                        }
                    )
                }

                if (cfg.scaleSpeed != 0f) {
                    it += MoveComponent(DEFAULT_SPEED * cfg.scaleSpeed)
                }

                if (cfg.canAttack) {
                    it += AttackComponent(
                        damage = (DEFAULT_ATTACK_DAMAGE * cfg.scaleAttackDamage).roundToInt(),
                        maxDelay = cfg.attackDelay,
                        extraRange = cfg.attackExtraRange
                    )
                }

                if (cfg.lifeScale > 0) {
                    it += LifeComponent(life = DEFAULT_LIFE * cfg.lifeScale, max = DEFAULT_LIFE * cfg.lifeScale)
                }

                if (cfg.bodyType != BodyDef.BodyType.StaticBody) {
                    // entity is not static -> add collision component to spawn
                    // collision entities around it
                    it += CollisionComponent()
                }

                when (type) {
                    PLAYER_TYPE -> {
                        it += PlayerComponent()
                        // add state component at the end since its ComponentListener initialization logic
                        // depends on some components added above
                        it += StateComponent()
                        it += InventoryComponent().apply {
                            itemsToAdd += ItemType.SWORD
                            itemsToAdd += ItemType.BIG_SWORD
                            itemsToAdd += ItemType.ARMOR
                            itemsToAdd += ItemType.HELMET
                            itemsToAdd += ItemType.BOOTS
                        }
                    }

                    CHEST_TYPE -> {
                        it += LootComponent()
                    }

                    else -> {
                        // any other entity gets an AIComponent to potentially
                        // use a behavior tree for its behavior.
                        // AIComponent entities also have a list of nearby other entities
                        // that they can interact with
                        it += AIComponent().apply {
                            if (cfg.aiTreePath.isNotBlank()) {
                                treePath = cfg.aiTreePath
                            }
                        }
                        // such entities also get an "action sensor"
                        // entities who are within that sensor get added to the nearby entities list
                        it[PhysicComponent].body.circle(4f) {
                            isSensor = true
                            userData = ACTION_SENSOR
                        }
                    }
                }
            }
        }

        entity.remove()
    }

    private fun spawnCfg(type: String): SpawnCfg = cachedCfgs.getOrPut(type) {
        when {
            // player is 48x48 graphic -> scale down physic body to match 16x16 world
            type == PLAYER_TYPE -> SpawnCfg(
                "player",
                scaleSpeed = 3f,
                scalePhysic = vec2(0.3f, 0.3f),
                physicOffset = vec2(0f, -10f * UNIT_SCALE),
                scaleAttackDamage = 1.25f,
                attackExtraRange = 0.6f,
                hasLight = true,
                categoryBit = LightComponent.b2dPlayer,
            )
            // chest gets a StaticBody so that entities cannot walk through it
            // because DynamicBody entities do not collide with each other
            type == CHEST_TYPE -> SpawnCfg(
                "chest",
                bodyType = BodyDef.BodyType.StaticBody,
                canAttack = false,
                lifeScale = 0f,
            )
            // slim is a 32x32 graphic -> scale down physic body to match 16x16 world
            type == "SLIME" -> SpawnCfg(
                "slime",
                lifeScale = 0.75f,
                scalePhysic = vec2(0.3f, 0.3f),
                physicOffset = vec2(0f, -2f * UNIT_SCALE),
                aiTreePath = "ai/slime.tree",
                hasLight = true,
                categoryBit = LightComponent.b2dSlime,
            )

            type.isNotBlank() -> SpawnCfg(type.lowercase())
            else -> gdxError("SpawnType must be specified")
        }
    }

    private fun size(atlasKey: String): Vector2 {
        return cachedSizes.getOrPut(atlasKey) {
            val regions = atlas.findRegions("$atlasKey/${AnimationType.IDLE.atlasKey}")
            if (regions.isEmpty) {
                gdxError("There are no texture regions for $atlasKey")
            }
            val firstFrame = regions.first()
            vec2(firstFrame.originalWidth * UNIT_SCALE, firstFrame.originalHeight * UNIT_SCALE)
        }

    }

    override fun handle(event: Event?): Boolean {
        if (event is MapChangeEvent) {
            val entityLayer = event.map.layer("entities")
            entityLayer.objects.forEach { mapObj ->
                val typeStr = mapObj.name ?: gdxError("MapObject ${mapObj.id} of 'entities' layer does not have a NAME")

                world.entity {
                    it += SpawnComponent(typeStr, vec2(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE))
                }
            }
            return true
        }
        return false
    }

    companion object {
        private val LOG = logger<EntitySpawnSystem>()
        private val COLLISION_OFFSET = vec2()
        private const val PLAYER_TYPE = "PLAYER"
        private const val CHEST_TYPE = "CHEST"
        const val ACTION_SENSOR = "ActionSensor"
        const val HIT_BOX_SENSOR = "HitBoxSensor"
    }
}