package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.AnyOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.PhysicComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.physicCmpFromShape2D
import com.github.quillraven.mysticwoods.component.PlayerComponent
import com.github.quillraven.mysticwoods.component.TiledComponent
import com.github.quillraven.mysticwoods.service.MapListener
import com.github.quillraven.mysticwoods.service.MapService
import ktx.box2d.body
import ktx.box2d.loop
import ktx.collections.GdxArray
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.isEmpty
import ktx.tiled.shape
import ktx.tiled.width

@AnyOf([PlayerComponent::class, TiledComponent::class])
class CollisionSpawnSystem(
    private val physicWorld: World,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val tiledCmps: ComponentMapper<TiledComponent>,
) : MapListener, IteratingSystem() {
    private val tileLayers = GdxArray<TiledMapTileLayer>()
    private val processedCells = mutableSetOf<TiledMapTileLayer.Cell>()

    init {
        MapService.addListener(this)
    }

    override fun onTickEntity(entity: Entity) {
        if (entity in playerCmps) {
            // for player entities we will spawn the collision objects around them that are not spawned yet
            val (playerX, playerY) = physicCmps[entity].body.position

            // TODO remove nesting by using extension functions
            tileLayers.forEach { layer ->
                for (x in playerX.toInt() - SPAWN_AREA_SIZE..playerX.toInt() + SPAWN_AREA_SIZE) {
                    for (y in playerY.toInt() - SPAWN_AREA_SIZE until playerY.toInt() + SPAWN_AREA_SIZE) {
                        layer.getCell(x, y)?.let { tileCell ->
                            if (tileCell.tile.objects.isEmpty()) {
                                // tileCell is not linked to a tile with collision objects -> do nothing
                                return@let
                            }
                            if (tileCell in processedCells) {
                                // tileCell already processed -> do nothing
                                return@let
                            }

                            processedCells.add(tileCell)
                            tileCell.tile.objects.forEach { mapObj ->
                                LOG.debug { "Creating tiled entity" }
                                world.entity {
                                    physicCmpFromShape2D(physicWorld, x, y, mapObj.shape)
                                    add<TiledComponent> {
                                        cell = tileCell
                                        nearbyEntities.add(entity)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // for existing collision tiled entities we check if there are no nearby entities anymore
            // and remove them in that case
            if (tiledCmps[entity].nearbyEntities.isEmpty()) {
                LOG.debug { "Removing tiled entity" }
                processedCells.remove(tiledCmps[entity].cell)
                world.remove(entity)
            }
        }
    }

    override fun onMapChanged(map: TiledMap) {
        map.layers.getByType(TiledMapTileLayer::class.java, tileLayers)
        world.entity {
            val w = map.width.toFloat()
            val h = map.height.toFloat()
            add<PhysicComponent> {
                body = physicWorld.body(BodyDef.BodyType.StaticBody) {
                    position.set(0f, 0f)
                    fixedRotation = true
                    allowSleep = false
                    loop(
                        vec2(0f, 0f),
                        vec2(w, 0f),
                        vec2(w, h),
                        vec2(0f, h),
                    ) { userData = "mapArea" }
                }
            }
        }
    }

    companion object {
        const val SPAWN_AREA_SIZE = 3
        private val LOG = logger<CollisionSpawnSystem>()
    }
}