package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.github.quillraven.fleks.Entity

class TiledComponent {
    lateinit var cell: TiledMapTileLayer.Cell
    val nearbyEntities = mutableSetOf<Entity>()
}