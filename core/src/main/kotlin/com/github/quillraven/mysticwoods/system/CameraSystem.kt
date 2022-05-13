package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.PhysicComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import ktx.math.component1
import ktx.math.component2

@AllOf([PlayerComponent::class])
class CameraSystem(
    @Qualifier("GameStage") stage: Stage,
    private val physicCmps: ComponentMapper<PhysicComponent>,
) : IteratingSystem() {
    private val camera: Camera = stage.camera

    override fun onTickEntity(entity: Entity) {
        with(physicCmps[entity]) {
            val (posX, posY) = body.position
            camera.position.set(posX, posY, camera.position.z)
        }
    }
}