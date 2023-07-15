package com.github.quillraven.mysticwoods.screen

import box2dLight.Light
import box2dLight.RayHandler
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.configureWorld
import com.github.quillraven.mysticwoods.MysticWoods
import com.github.quillraven.mysticwoods.component.LightComponent
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import com.github.quillraven.mysticwoods.event.fire
import com.github.quillraven.mysticwoods.input.PlayerInputProcessor
import com.github.quillraven.mysticwoods.input.gdxInputProcessor
import com.github.quillraven.mysticwoods.system.*
import com.github.quillraven.mysticwoods.ui.disposeSkin
import com.github.quillraven.mysticwoods.ui.loadSkin
import com.github.quillraven.mysticwoods.ui.model.GameModel
import com.github.quillraven.mysticwoods.ui.model.InventoryModel
import com.github.quillraven.mysticwoods.ui.view.PauseView
import com.github.quillraven.mysticwoods.ui.view.gameView
import com.github.quillraven.mysticwoods.ui.view.inventoryView
import com.github.quillraven.mysticwoods.ui.view.pauseView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.scene2d.actors

class GameScreen(game: MysticWoods) : KtxScreen {
    private val gameStage = game.gameStage
    private val uiStage = game.uiStage
    private val gameAtlas = TextureAtlas("graphics/game.atlas")
    private val phWorld = createWorld(gravity = Vector2.Zero).apply {
        autoClearForces = false
    }
    private val rayHandler = RayHandler(phWorld).apply {
        // don't make light super bright
        RayHandler.useDiffuseLight(true)

        // player only throws shadows for map environment but not for enemies like slimes
        Light.setGlobalContactFilter(LightComponent.b2dPlayer, 1, LightComponent.b2dEnvironment)

        setAmbientLight(LightSystem.dayLightColor)
    }
    private val eWorld = configureWorld {
        injectables {
            add(phWorld)
            add("GameStage", gameStage)
            add("UiStage", uiStage)
            add("GameAtlas", gameAtlas)
            add(rayHandler)
        }

        systems {
            add(EntitySpawnSystem())
            add(CollisionSpawnSystem())
            add(CollisionDespawnSystem())
            add(AISystem())
            add(PhysicSystem())
            add(AnimationSystem())
            add(MoveSystem())
            add(AttackSystem())
            add(LootSystem())
            add(InventorySystem())
            // DeadSystem must come before LifeSystem
            // because LifeSystem will add DeadComponent to an entity and sets its death animation.
            // Since the DeadSystem is checking if the animation is done it needs to be called after
            // the death animation is set which will be in the next frame in the AnimationSystem above.
            add(DeadSystem())
            add(LifeSystem())
            add(StateSystem())
            add(CameraSystem())
            add(FloatingTextSystem())
            add(RenderSystem())
            add(LightSystem())
            add(AudioSystem())
            add(DebugSystem())
        }
    }
    private var currentMap: TiledMap? = null

    init {
        loadSkin()
        eWorld.systems.forEach { sys ->
            if (sys is EventListener) {
                gameStage.addListener(sys)
            }
        }
        PlayerInputProcessor(eWorld, gameStage, uiStage)
        gdxInputProcessor(uiStage)

        // UI
        uiStage.actors {
            gameView(GameModel(eWorld, gameStage))
            inventoryView(InventoryModel(eWorld, gameStage)) {
                this.isVisible = false
            }
            pauseView { this.isVisible = false }
        }
    }

    override fun show() {
        setMap("maps/demo.tmx")
    }

    private fun pauseWorld(pause: Boolean) {
        val mandatorySystems = setOf(
            AnimationSystem::class,
            CameraSystem::class,
            RenderSystem::class,
            DebugSystem::class
        )
        eWorld.systems
            .filter { it::class !in mandatorySystems }
            .forEach { it.enabled = !pause }

        uiStage.actors.filterIsInstance<PauseView>().first().isVisible = pause
    }

    override fun resize(width: Int, height: Int) {
        val screenX = gameStage.viewport.screenX
        val screenY = gameStage.viewport.screenY
        val screenW = gameStage.viewport.screenWidth
        val screenH = gameStage.viewport.screenHeight
        rayHandler.useCustomViewport(screenX, screenY, screenW, screenH)
    }

    override fun pause() = pauseWorld(true)

    override fun resume() = pauseWorld(false)

    private fun setMap(path: String) {
        currentMap?.disposeSafely()
        val newMap = TmxMapLoader().load(path)
        currentMap = newMap
        gameStage.fire(MapChangeEvent(newMap))
    }

    override fun render(delta: Float) {
        val dt = delta.coerceAtMost(0.25f)
        GdxAI.getTimepiece().update(dt)
        eWorld.update(dt)
    }

    override fun dispose() {
        eWorld.dispose()
        phWorld.disposeSafely()
        gameAtlas.disposeSafely()
        currentMap?.disposeSafely()
        disposeSkin()
        rayHandler.disposeSafely()
    }
}