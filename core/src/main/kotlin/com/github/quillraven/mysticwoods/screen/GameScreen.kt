package com.github.quillraven.mysticwoods.screen

import box2dLight.Light
import box2dLight.RayHandler
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.world
import com.github.quillraven.mysticwoods.MysticWoods
import com.github.quillraven.mysticwoods.component.AIComponent.Companion.AIComponentListener
import com.github.quillraven.mysticwoods.component.FloatingTextComponent.Companion.FloatingTextComponentListener
import com.github.quillraven.mysticwoods.component.ImageComponent.Companion.ImageComponentListener
import com.github.quillraven.mysticwoods.component.LightComponent
import com.github.quillraven.mysticwoods.component.LightComponent.Companion.LightComponentListener
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.PhysicComponentListener
import com.github.quillraven.mysticwoods.component.StateComponent.Companion.StateComponentListener
import com.github.quillraven.mysticwoods.input.PlayerInputProcessor
import com.github.quillraven.mysticwoods.input.gdxInputProcessor
import com.github.quillraven.mysticwoods.system.*
import com.github.quillraven.mysticwoods.ui.disposeSkin
import com.github.quillraven.mysticwoods.ui.loadSkin
import com.github.quillraven.mysticwoods.ui.model.DialogModel
import com.github.quillraven.mysticwoods.ui.model.GameModel
import com.github.quillraven.mysticwoods.ui.model.InventoryModel
import com.github.quillraven.mysticwoods.ui.view.*
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

    private val eWorld = world {
        injectables {
            add(phWorld)
            add("GameStage", gameStage)
            add("UiStage", uiStage)
            add("GameAtlas", gameAtlas)
            add(rayHandler)
        }

        components {
            add<PhysicComponentListener>()
            add<ImageComponentListener>()
            add<StateComponentListener>()
            add<AIComponentListener>()
            add<FloatingTextComponentListener>()
            add<LightComponentListener>()
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
            add<DialogSystem>()
            add<InventorySystem>()
            // DeadSystem must come before LifeSystem
            // because LifeSystem will add DeadComponent to an entity and sets its death animation.
            // Since the DeadSystem is checking if the animation is done it needs to be called after
            // the death animation is set which will be in the next frame in the AnimationSystem above.
            add<DeadSystem>()
            add<LifeSystem>()
            add<StateSystem>()
            add<PortalSystem>()
            add<CameraSystem>()
            add<FloatingTextSystem>()
            add<RenderSystem>()
            add<LightSystem>()
            add<AudioSystem>()
            add<DebugSystem>()
        }
    }

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
            dialogView(DialogModel(gameStage))
            inventoryView(InventoryModel(eWorld, gameStage)) {
                this.isVisible = false
            }
            pauseView { this.isVisible = false }
        }
    }

    override fun show() {
        eWorld.system<PortalSystem>().setMap("maps/demo.tmx")
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

    override fun render(delta: Float) {
        val dt = delta.coerceAtMost(0.25f)
        GdxAI.getTimepiece().update(dt)
        eWorld.update(dt)
    }

    override fun dispose() {
        eWorld.dispose()
        phWorld.disposeSafely()
        gameAtlas.disposeSafely()
        disposeSkin()
        rayHandler.disposeSafely()
    }
}