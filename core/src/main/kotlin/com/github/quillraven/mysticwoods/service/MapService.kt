package com.github.quillraven.mysticwoods.service

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.utils.Disposable
import ktx.assets.disposeSafely

interface MapListener {
    fun onMapChanged(map: TiledMap)
}

object MapService : Disposable {
    private lateinit var currentMap: TiledMap
    private val loader = TmxMapLoader()
    private val listeners = mutableListOf<MapListener>()

    fun setMap(path: String) {
        currentMap = loader.load(path)
        listeners.forEach { it.onMapChanged(currentMap) }
    }

    fun addListener(listener: MapListener) {
        listeners.add(listener)
    }

    override fun dispose() {
        currentMap.disposeSafely()
    }
}