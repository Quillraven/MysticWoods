package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.component.ImageComponent.Companion.ImageComponentListener
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.PhysicComponentListener
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import com.github.quillraven.mysticwoods.event.fire
import com.github.quillraven.mysticwoods.system.*
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld

fun gdxError(message: Any): Nothing = throw GdxRuntimeException(message.toString())

class GameScreen : KtxScreen {
    private val gameAtlas = TextureAtlas("graphics/game.atlas")
    private val gameStage = Stage(ExtendViewport(16f, 9f))
    private val phWorld = createWorld(gravity = Vector2.Zero).apply {
        autoClearForces = false
    }
    private val eWorld = World {
        inject(phWorld)
        inject("GameStage", gameStage)
        inject("GameAtlas", gameAtlas)

        componentListener<PhysicComponentListener>()
        componentListener<ImageComponentListener>()

        system<PlayerInputSystem>()
        system<EntitySpawnSystem>()
        system<CollisionSpawnSystem>()
        system<MoveSystem>()
        system<PhysicSystem>()
        system<AnimationSystem>()
        system<CameraSystem>()
        system<RenderSystem>()
        system<DebugSystem>()
    }
    private var currentMap: TiledMap? = null

    override fun show() {
        setMap("maps/demo.tmx")
    }

    private fun setMap(path: String) {
        currentMap?.disposeSafely()
        val newMap = TmxMapLoader().load(path)
        currentMap = newMap
        gameStage.fire(MapChangeEvent(newMap))
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
        gameAtlas.disposeSafely()
        currentMap?.disposeSafely()
    }
}