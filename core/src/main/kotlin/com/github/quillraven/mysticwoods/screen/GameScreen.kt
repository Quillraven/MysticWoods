package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.component.ImageComponent.Companion.ImageComponentListener
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.PhysicComponentListener
import com.github.quillraven.mysticwoods.system.*
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld

fun gdxError(message: Any): Nothing = throw GdxRuntimeException(message.toString())

class GameScreen : KtxScreen {
    private val gameStage = Stage(ExtendViewport(16f, 9f))
    private val phWorld = createWorld(gravity = Vector2.Zero).apply {
        autoClearForces = false
    }
    private val eWorld = World {
        inject(phWorld)
        inject("GameStage", gameStage)

        componentListener<PhysicComponentListener>()
        componentListener<ImageComponentListener>()

        system<PlayerInputSystem>()
        system<SpawnSystem>()
        system<MoveSystem>()
        system<PhysicSystem>()
        system<AnimationSystem>()
        system<CameraSystem>()
        system<RenderSystem>()
        system<DebugSystem>()
    }

    override fun resize(width: Int, height: Int) {
        gameStage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        eWorld.update(delta)
    }

    override fun dispose() {
        eWorld.dispose()
        phWorld.disposeSafely()
        gameStage.disposeSafely()
    }
}