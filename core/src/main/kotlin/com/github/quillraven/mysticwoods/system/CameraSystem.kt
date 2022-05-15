package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import com.github.quillraven.mysticwoods.service.MapListener
import com.github.quillraven.mysticwoods.service.MapService
import ktx.tiled.height
import ktx.tiled.width

@AllOf([PlayerComponent::class, ImageComponent::class])
class CameraSystem(
    @Qualifier("GameStage") stage: Stage,
    private val imageCmps: ComponentMapper<ImageComponent>,
) : MapListener, IteratingSystem() {
    private val camera: Camera = stage.camera
    private var maxW = 0f
    private var maxH = 0f

    init {
        MapService.addListener(this)
    }

    override fun onTickEntity(entity: Entity) {
        // we center on the image because it has an
        // interpolated position for rendering which makes
        // the game smoother
        with(imageCmps[entity]) {
            val viewW = camera.viewportWidth * 0.5f
            val viewH = camera.viewportHeight * 0.5f
            camera.position.set(
                image.x.coerceIn(viewW, maxW - viewW),
                image.y.coerceIn(viewH, maxH - viewH),
                camera.position.z
            )
        }
    }

    override fun onMapChanged(map: TiledMap) {
        maxW = map.width.toFloat()
        maxH = map.height.toFloat()
    }
}