package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.ImageComponent
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.component.PhysicComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import ktx.math.component1
import ktx.math.component2

@AllOf([PlayerComponent::class])
class CameraSystem(
    @Qualifier("GameStage") stage: Stage,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>,
) : IteratingSystem() {
    private val camera: Camera = stage.camera

    override fun onTickEntity(entity: Entity) {
        with(physicCmps[entity]) {
            val (posX, posY) = body.position
            camera.position.set(posX, posY, camera.position.z)
        }

        //TODO remove debug
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveCmps[entity].direction(90f)
            moveCmps[entity].stop = false
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveCmps[entity].direction(270f)
            moveCmps[entity].stop = false
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveCmps[entity].direction(0f)
            moveCmps[entity].stop = false
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveCmps[entity].direction(180f)
            moveCmps[entity].stop = false
        } else {
            moveCmps[entity].stop = true
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            imageCmps[entity].image.scaleX += 0.25f
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            imageCmps[entity].image.scaleX -= 0.25f
        }
    }
}