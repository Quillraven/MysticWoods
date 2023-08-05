package com.github.quillraven.mysticwoods.screen

import box2dLight.RayHandler
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.world
import com.github.quillraven.mysticwoods.component.AIComponent.Companion.AIComponentListener
import com.github.quillraven.mysticwoods.component.FloatingTextComponent.Companion.FloatingTextComponentListener
import com.github.quillraven.mysticwoods.component.ImageComponent.Companion.ImageComponentListener
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.PhysicComponentListener
import com.github.quillraven.mysticwoods.component.StateComponent.Companion.StateComponentListener
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import com.github.quillraven.mysticwoods.event.fire
import com.github.quillraven.mysticwoods.input.PlayerInputProcessor
import com.github.quillraven.mysticwoods.input.gdxInputProcessor
import com.github.quillraven.mysticwoods.system.*
import com.github.quillraven.mysticwoods.ui.disposeSkin
import com.github.quillraven.mysticwoods.ui.loadSkin
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld

abstract class TestScreen(private var testMapPath: String = "") : KtxScreen {
    private val gameAtlas = TextureAtlas("graphics/game.atlas")
    val gameStage = Stage(ExtendViewport(16f, 9f))
    val uiStage = Stage(ExtendViewport(320f, 180f))
    private val phWorld = createWorld(gravity = Vector2.Zero).apply {
        autoClearForces = false
    }
    private val rayHandler = RayHandler(phWorld)
    val eWorld = world {
        injectables {
            add(rayHandler)
            add(phWorld)
            add("GameStage", gameStage)
            add("UiStage", uiStage)
            add("GameAtlas", gameAtlas)
        }

        components {
            add<PhysicComponentListener>()
            add<ImageComponentListener>()
            add<StateComponentListener>()
            add<AIComponentListener>()
            add<FloatingTextComponentListener>()
        }

        systems {
            add<EntitySpawnSystem>()
            add<CollisionSpawnSystem>()
            add<CollisionDespawnSystem>()
            add<AISystem>()
            add<PhysicSystem>()
            add<AnimationSystem>()
            add<MoveSystem>()
            add<AttackSystem>()
            add<LootSystem>()
            add<InventorySystem>()
            add<DeadSystem>()
            add<LifeSystem>()
            add<StateSystem>()
            add<CameraSystem>()
            add<FloatingTextSystem>()
            add<RenderSystem>()
            add<DebugSystem>()
        }
    }
    private var tiledMap: TiledMap? = null

    init {
        loadSkin()
        eWorld.system<DebugSystem>().enabled = true
        eWorld.systems.forEach { sys ->
            if (sys is EventListener) {
                gameStage.addListener(sys)
            }
        }
        PlayerInputProcessor(eWorld, gameStage, uiStage)
        gdxInputProcessor(uiStage)
    }

    override fun show() {
        tiledMap?.disposeSafely()
        if (testMapPath.isNotBlank()) {
            tiledMap = TmxMapLoader().load(testMapPath)
            gameStage.fire(MapChangeEvent(tiledMap!!))
        }
    }

    override fun resize(width: Int, height: Int) {
        gameStage.viewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
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
        uiStage.disposeSafely()
        gameAtlas.disposeSafely()
        tiledMap?.disposeSafely()
        disposeSkin()
    }
}