package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.compareEntity
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.service.MapListener
import com.github.quillraven.mysticwoods.service.MapService
import ktx.graphics.use

@AllOf(components = [ImageComponent::class])
class RenderSystem(
    @Qualifier("GameStage") private val stage: Stage,
    private val imageCmps: ComponentMapper<ImageComponent>
) : MapListener, IteratingSystem(
    comparator = compareEntity { e1, e2 -> imageCmps[e1].compareTo(imageCmps[e2]) }
) {
    private val orthoCam: OrthographicCamera = stage.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, stage.batch)
    private var bgdLayers = mutableListOf<TiledMapTileLayer>()
    private var fgdLayers = mutableListOf<TiledMapTileLayer>()

    init {
        MapService.addListener(this)
    }

    override fun onTick() {
        super.onTick()
        with(stage) {
            viewport.apply()

            AnimatedTiledMapTile.updateAnimationBaseTime()
            mapRenderer.setView(orthoCam)
            if (bgdLayers.isNotEmpty()) {
                stage.batch.use(orthoCam.combined) {
                    bgdLayers.forEach { mapRenderer.renderTileLayer(it) }
                }
            }

            act(deltaTime)
            draw()

            if (fgdLayers.isNotEmpty()) {
                stage.batch.use(orthoCam.combined) {
                    fgdLayers.forEach { mapRenderer.renderTileLayer(it) }
                }
            }
        }
    }

    override fun onMapChanged(map: TiledMap) {
        mapRenderer.map = map
        bgdLayers.clear()
        fgdLayers.clear()
        map.layers.forEach { layer ->
            if (layer !is TiledMapTileLayer) {
                return@forEach
            }

            if (layer.name.startsWith("fgd_")) {
                fgdLayers.add(layer)
            } else {
                bgdLayers.add(layer)
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        imageCmps[entity].image.toFront()
    }

    override fun onDispose() {
        mapRenderer.dispose()
    }
}