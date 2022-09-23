package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

class TiledComponent(
    val cell: TiledMapTileLayer.Cell
) : Component<TiledComponent> {

    val nearbyEntities = mutableSetOf<Entity>()

    override fun type() = TiledComponent

    companion object : ComponentType<TiledComponent>()
}