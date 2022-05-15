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

    private fun TiledMapTileLayer.forEachCell(
        startX: Int,
        startY: Int,
        size: Int,
        action: (TiledMapTileLayer.Cell, Int, Int) -> Unit
    ) {
        for (x in startX - size..startX + size) {
            for (y in startY - size until startY + size) {
                this.getCell(x, y)?.let { action(it, x, y) }
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        if (entity in playerCmps) {
            // for player entities we will spawn the collision objects around them that are not spawned yet
            val (playerX, playerY) = physicCmps[entity].body.position

            tileLayers.forEach { layer ->
                layer.forEachCell(playerX.toInt(), playerY.toInt(), SPAWN_AREA_SIZE) { tileCell, x, y ->
                    if (tileCell.tile.objects.isEmpty()) {
                        // tileCell is not linked to a tile with collision objects -> do nothing
                        return@forEachCell
                    }
                    if (tileCell in processedCells) {
                        // tileCell already processed -> do nothing
                        return@forEachCell
                    }

                    processedCells.add(tileCell)
                    tileCell.tile.objects.forEach { mapObj ->
                        world.entity {
                            physicCmpFromShape2D(physicWorld, x, y, mapObj.shape)
                            add<TiledComponent> {
                                cell = tileCell
                                // add entity immediately here, otherwise the newly created
                                // collision entity might get removed by the code below because
                                // the physic collision event will come later in the PhysicSystem when
                                // the physic world gets updated
                                nearbyEntities.add(entity)
                            }
                        }
                    }
                }
            }
        } else {
            // for existing collision tiled entities we check if there are no nearby entities anymore
            // and remove them in that case
            if (tiledCmps[entity].nearbyEntities.isEmpty()) {
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
    }
}