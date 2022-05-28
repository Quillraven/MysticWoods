package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.component.AIComponent.Companion.AIComponentListener
import com.github.quillraven.mysticwoods.component.ImageComponent.Companion.ImageComponentListener
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.PhysicComponentListener
import com.github.quillraven.mysticwoods.component.StateComponent.Companion.StateComponentListener
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import com.github.quillraven.mysticwoods.event.fire
import com.github.quillraven.mysticwoods.system.*
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld

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
        componentListener<StateComponentListener>()
        componentListener<AIComponentListener>()

        system<PlayerInputSystem>()
        system<EntitySpawnSystem>()
        system<CollisionSpawnSystem>()
        system<CollisionDespawnSystem>()
        system<AISystem>()
        system<PhysicSystem>()
        system<AnimationSystem>()
        system<MoveSystem>()
        system<AttackSystem>()
        // DeadSystem must come before LifeSystem
        // because LifeSystem will add DeadComponent to an entity but the death animation itself
        // is set in the StateSystem afterwards.
        // Since the DeadSystem is checking if the animation is done it needs to be called after
        // the death animation is set which will be in the next frame.
        system<DeadSystem>()
        system<LifeSystem>()
        system<StateSystem>()
        system<CameraSystem>()
        system<RenderSystem>()
        system<DebugSystem>()
    }
    private var currentMap: TiledMap? = null

    init {
        eWorld.systems.forEach { sys ->
            if (sys is EventListener) {
                gameStage.addListener(sys)
            }
        }
    }

    override fun show() {
        // TODO update Fleks to have access to systems and check for EventListener systems and add them to the stage
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
        val dt = delta.coerceAtMost(0.25f)
        GdxAI.getTimepiece().update(dt)
        eWorld.update(dt)
    }

    override fun dispose() {
        eWorld.dispose()
        phWorld.disposeSafely()
        gameStage.disposeSafely()
        gameAtlas.disposeSafely()
        currentMap?.disposeSafely()
    }
}