package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.actor.FlipImage
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.physicCmpFromImage
import com.github.quillraven.mysticwoods.component.SpawnCfg.Companion.DEFAULT_SPEED
import com.github.quillraven.mysticwoods.screen.gdxError
import com.github.quillraven.mysticwoods.service.MapListener
import com.github.quillraven.mysticwoods.service.MapService
import ktx.box2d.box
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.*

@AllOf([SpawnComponent::class])
class EntitySpawnSystem(
    @Qualifier("GameAtlas") private val atlas: TextureAtlas,
    private val physicWorld: World,
    private val spawnCmps: ComponentMapper<SpawnComponent>,
) : MapListener, IteratingSystem() {
    private val cachedCfgs = mutableMapOf<SpawnType, SpawnCfg>()
    private val cachedSizes = mutableMapOf<String, Vector2>()

    init {
        MapService.addListener(this)
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

                this.physicCmpFromImage(physicWorld, imageCmp.image) { width, height ->
                    val w = width * cfg.scalePhysic.x
                    val h = height * cfg.scalePhysic.y
                    // hit box
                    box(w, h, cfg.physicOffset) {
                        isSensor = true
                    }
                    // collision box
                    val collH = h * 0.4f
                    COLLISION_OFFSET.set(cfg.physicOffset)
                    COLLISION_OFFSET.y -= h * 0.5f - collH * 0.5f
                    box(w, collH, COLLISION_OFFSET)
                }

                if (cfg.scaleSpeed != 0f) {
                    add<MoveComponent> { max = DEFAULT_SPEED * cfg.scaleSpeed }
                }

                if (type == SpawnType.PLAYER) {
                    add<PlayerComponent>()
                }
            }
        }

        world.remove(entity)
    }

    private fun spawnCfg(type: SpawnType): SpawnCfg = cachedCfgs.getOrPut(type) {
        when (type) {
            SpawnType.PLAYER -> SpawnCfg("player", scaleSpeed = 3f, physicOffset = vec2(0f, -10f * UNIT_SCALE))
            SpawnType.SLIME -> SpawnCfg("slime")
            SpawnType.UNDEFINED -> gdxError("SpawnType must be specified")
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

    override fun onMapChanged(map: TiledMap) {
        val entityLayer = map.layer("entities")
        entityLayer.objects.forEach { mapObj ->
            val typeStr = mapObj.type ?: gdxError("MapObject ${mapObj.id} of 'entities' layer does not have a TYPE")

            world.entity {
                add<SpawnComponent> {
                    type = SpawnType.valueOf(typeStr)
                    location.set(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE)
                }
            }
        }
    }

    companion object {
        private val LOG = logger<EntitySpawnSystem>()
        private val COLLISION_OFFSET = vec2()
    }
}