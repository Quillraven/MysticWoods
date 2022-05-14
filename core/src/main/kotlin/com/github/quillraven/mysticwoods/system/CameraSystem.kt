package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent

@AllOf([PlayerComponent::class, ImageComponent::class])
class CameraSystem(
    @Qualifier("GameStage") stage: Stage,
    private val imageCmps: ComponentMapper<ImageComponent>,
) : IteratingSystem() {
    private val camera: Camera = stage.camera

    override fun onTickEntity(entity: Entity) {
        // we center on the image because it has an
        // interpolated position for rendering which makes
        // the game smoother
        with(imageCmps[entity]) {
            camera.position.set(image.x, image.y, camera.position.z)
        }
    }
}