package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.actor.FlipImage
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.physicCmpFromImage
import com.github.quillraven.mysticwoods.component.SpawnCfg.Companion.DEFAULT_SPEED
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import ktx.app.gdxError
import ktx.box2d.box
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.*

@AllOf([SpawnComponent::class])
class EntitySpawnSystem(
    @Qualifier("GameAtlas") private val atlas: TextureAtlas,
    @Qualifier("GameStage") stage: Stage,
    private val physicWorld: World,
    private val spawnCmps: ComponentMapper<SpawnComponent>,
) : EventListener, IteratingSystem() {
    private val cachedCfgs = mutableMapOf<String, SpawnCfg>()
    private val cachedSizes = mutableMapOf<String, Vector2>()

    init {
        stage.addListener(this)
    }

    override fun onTickEntity(entity: Entity) {
        with(spawnCmps[entity]) {
            val cfg = spawnCfg(type)
            val relativeSize = size(cfg.atlasKey)
            LOG.debug { "Spawning entity of type $type with size $relativeSize" }

            world.entity {
                val imageCmp = add<ImageComponent> {
                    image = FlipImage().apply {
                        setScaling(Scaling.fill)
                        setPosition(location.x, location.y)
                        setSize(relativeSize.x * cfg.scaleSize, relativeSize.y * cfg.scaleSize)
                    }
                }

                add<AnimationComponent> {
                    nextAnimation(cfg.atlasKey, AnimationType.IDLE)
                }

                this.physicCmpFromImage(physicWorld, imageCmp.image, cfg.bodyType) { width, height ->
                    val w = width * cfg.scalePhysic.x
                    val h = height * cfg.scalePhysic.y

                    // hit box
                    box(w, h, cfg.physicOffset) {
                        isSensor = cfg.bodyType != BodyDef.BodyType.StaticBody
                    }

                    if (cfg.bodyType != BodyDef.BodyType.StaticBody) {
                        // collision box
                        val collH = h * 0.4f
                        COLLISION_OFFSET.set(cfg.physicOffset)
                        COLLISION_OFFSET.y -= h * 0.5f - collH * 0.5f
                        box(w, collH, COLLISION_OFFSET)
                    }
                }

                if (cfg.scaleSpeed != 0f) {
                    add<MoveComponent> { max = DEFAULT_SPEED * cfg.scaleSpeed }
                }

                if (cfg.bodyType != BodyDef.BodyType.StaticBody) {
                    // entity is not static -> add collision component to spawn
                    // collision entities around it
                    add<CollisionComponent>()
                }

                if (type == PLAYER_TYPE) {
                    add<PlayerComponent>()
                }
            }
        }

        world.remove(entity)
    }

    private fun spawnCfg(type: String): SpawnCfg = cachedCfgs.getOrPut(type) {
        when {
            // player is 48x48 graphic -> scale down physic body to match 16x16 world
            type == PLAYER_TYPE -> SpawnCfg(
                "player",
                scaleSpeed = 3f,
                scalePhysic = vec2(0.3f, 0.3f),
                physicOffset = vec2(0f, -10f * UNIT_SCALE)
            )
            // chest gets a StaticBody so that entities cannot walk through it
            // because DynamicBody entities do not collide with each other
            type == "CHEST" -> SpawnCfg(
                "chest",
                bodyType = BodyDef.BodyType.StaticBody
            )
            // slim is a 32x32 graphic -> scale down physic body to match 16x16 world
            type == "SLIME" -> SpawnCfg(
                "slime",
                scalePhysic = vec2(0.3f, 0.3f),
                physicOffset = vec2(0f, -2f * UNIT_SCALE)
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
                val typeStr = mapObj.type ?: gdxError("MapObject ${mapObj.id} of 'entities' layer does not have a TYPE")

                world.entity {
                    add<SpawnComponent> {
                        type = typeStr
                        location.set(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE)
                    }
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
    }
}