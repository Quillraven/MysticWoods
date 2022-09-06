package com.github.quillraven.mysticwoods.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity

fun Stage.fire(event: Event) = this.root.fire(event)

data class MapChangeEvent(val map: TiledMap) : Event()

data class CollisionDespawnEvent(val cell: Cell) : Event()

data class EntityAttackEvent(val atlasKey: String) : Event()

data class EntityDeathEvent(val atlasKey: String) : Event()

class EntityLootEvent : Event()

// damage is not used at the moment but might be useful in the future ;)
class EntityTakeDamageEvent(val entity: Entity, val damage: Float) : Event()

class EntityReviveEvent(val entity: Entity) : Event()