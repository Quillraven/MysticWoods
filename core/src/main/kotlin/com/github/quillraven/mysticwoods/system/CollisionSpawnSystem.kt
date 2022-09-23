package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.CollisionComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.physicCmpFromShape2D
import com.github.quillraven.mysticwoods.component.TiledComponent
import com.github.quillraven.mysticwoods.event.CollisionDespawnEvent
import com.github.quillraven.mysticwoods.event.MapChangeEvent
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

class CollisionSpawnSystem(
    private val physicWorld: World = inject(),
) : EventListener, IteratingSystem(family { all(CollisionComponent, PhysicComponent) }) {
    private val tileLayers = GdxArray<TiledMapTileLayer>()
    private val processedCells = mutableSetOf<TiledMapTileLayer.Cell>()

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
        // for collision entities we will spawn the collision objects around them that are not spawned yet
        val (entityX, entityY) = entity[PhysicComponent].body.position

        tileLayers.forEach { layer ->
            layer.forEachCell(entityX.toInt(), entityY.toInt(), SPAWN_AREA_SIZE) { tileCell, x, y ->
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
                        it += physicCmpFromShape2D(physicWorld, x, y, mapObj.shape)
                        it += TiledComponent(tileCell).also { tiledCmp ->
                            // add entity immediately here, otherwise the newly created
                            // collision entity might get removed by the CollisionDespawnSystem because
                            // the physic collision event will come later in the PhysicSystem when
                            // the physic world gets updated
                            tiledCmp.nearbyEntities.add(entity)
                        }
                    }
                }
            }
        }
    }

    override fun handle(event: Event?): Boolean {
        if (event is MapChangeEvent) {
            processedCells.clear()
            event.map.layers.getByType(TiledMapTileLayer::class.java, tileLayers)

            // create map boundary collision object
            world.entity {
                val w = event.map.width.toFloat()
                val h = event.map.height.toFloat()
                it += PhysicComponent().apply {
                    body = physicWorld.body(BodyDef.BodyType.StaticBody) {
                        position.set(0f, 0f)
                        fixedRotation = true
                        allowSleep = false
                        loop(
                            vec2(0f, 0f),
                            vec2(w, 0f),
                            vec2(w, h),
                            vec2(0f, h),
                        )
                    }
                }
            }
            return true
        } else if (event is CollisionDespawnEvent) {
            processedCells.remove(event.cell)
            return true
        }
        return false
    }

    companion object {
        const val SPAWN_AREA_SIZE = 1
    }
}