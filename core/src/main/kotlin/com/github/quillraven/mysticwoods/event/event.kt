package com.github.quillraven.mysticwoods.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage

fun Stage.fire(event: Event) = this.root.fire(event)

data class MapChangeEvent(val map: TiledMap) : Event()