package com.github.quillraven.mysticwoods.system

import box2dLight.RayHandler
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.compareEntity
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import ktx.graphics.use
import ktx.tiled.forEachLayer

@AllOf(components = [ImageComponent::class])
class RenderSystem(
    @Qualifier("GameStage") private val gameStage: Stage,
    @Qualifier("UiStage") private val uiStage: Stage,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val rayHandler: RayHandler,
) : EventListener, IteratingSystem(
    comparator = compareEntity { e1, e2 -> imageCmps[e1].compareTo(imageCmps[e2]) }
) {
    private val orthoCam: OrthographicCamera = gameStage.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, gameStage.batch)
    private var bgdLayers = mutableListOf<TiledMapTileLayer>()
    private var fgdLayers = mutableListOf<TiledMapTileLayer>()

    override fun onTick() {
        super.onTick()
        gameStage.viewport.apply()

        AnimatedTiledMapTile.updateAnimationBaseTime()
        mapRenderer.setView(orthoCam)
        if (bgdLayers.isNotEmpty()) {
            gameStage.batch.color = Color.WHITE
            gameStage.batch.use(orthoCam.combined) {
                bgdLayers.forEach { mapRenderer.renderTileLayer(it) }
            }
        }

        gameStage.run {
            act(deltaTime)
            draw()
        }

        if (fgdLayers.isNotEmpty()) {
            gameStage.batch.color = Color.WHITE
            gameStage.batch.use(orthoCam.combined) {
                fgdLayers.forEach { mapRenderer.renderTileLayer(it) }
            }
        }

        // render lights
        rayHandler.setCombinedMatrix(orthoCam)
        rayHandler.updateAndRender()

        // render UI
        uiStage.run {
            viewport.apply()
            act(deltaTime)
            draw()
        }
    }

    override fun handle(event: Event?): Boolean {
        if (event is MapChangeEvent) {
            mapRenderer.map = event.map
            bgdLayers.clear()
            fgdLayers.clear()
            event.map.forEachLayer<TiledMapTileLayer> { layer ->
                if (layer.name.startsWith("fgd_")) {
                    fgdLayers.add(layer)
                } else {
                    bgdLayers.add(layer)
                }
            }
            return true
        }
        return false
    }

    override fun onTickEntity(entity: Entity) {
        imageCmps[entity].image.toFront()
    }

    override fun onDispose() {
        mapRenderer.dispose()
    }
}