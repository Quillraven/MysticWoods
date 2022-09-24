package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import ktx.tiled.height
import ktx.tiled.width

class CameraSystem(
    stage: Stage = inject("GameStage")
) : EventListener, IteratingSystem(family { all(PlayerComponent, ImageComponent) }) {
    private val camera: Camera = stage.camera
    private var maxW = 0f
    private var maxH = 0f

    override fun onTickEntity(entity: Entity) {
        // we center on the image because it has an
        // interpolated position for rendering which makes
        // the game smoother
        with(entity[ImageComponent]) {
            val viewW = camera.viewportWidth * 0.5f
            val viewH = camera.viewportHeight * 0.5f
            camera.position.set(
                image.x.coerceIn(viewW, maxW - viewW),
                image.y.coerceIn(viewH, maxH - viewH),
                camera.position.z
            )
        }
    }

    override fun handle(event: Event?): Boolean {
        if (event is MapChangeEvent) {
            maxW = event.map.width.toFloat()
            maxH = event.map.height.toFloat()
            return true
        }
        return false
    }
}