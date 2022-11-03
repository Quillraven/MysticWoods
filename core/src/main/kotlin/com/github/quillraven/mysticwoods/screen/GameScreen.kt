package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.world
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.component.AIComponent.Companion.onAiAdd
import com.github.quillraven.mysticwoods.component.FloatingTextComponent.Companion.onFloatingAdd
import com.github.quillraven.mysticwoods.component.FloatingTextComponent.Companion.onFloatingRemove
import com.github.quillraven.mysticwoods.component.ImageComponent.Companion.onImageAdd
import com.github.quillraven.mysticwoods.component.ImageComponent.Companion.onImageRemove
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.onPhysicAdd
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.onPhysicRemove
import com.github.quillraven.mysticwoods.component.StateComponent.Companion.onStateAdd
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import com.github.quillraven.mysticwoods.event.fire
import com.github.quillraven.mysticwoods.input.PlayerInputProcessor
import com.github.quillraven.mysticwoods.input.gdxInputProcessor
import com.github.quillraven.mysticwoods.system.*
import com.github.quillraven.mysticwoods.ui.disposeSkin
import com.github.quillraven.mysticwoods.ui.loadSkin
import com.github.quillraven.mysticwoods.ui.model.GameModel
import com.github.quillraven.mysticwoods.ui.model.InventoryModel
import com.github.quillraven.mysticwoods.ui.view.gameView
import com.github.quillraven.mysticwoods.ui.view.inventoryView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.scene2d.actors

class GameScreen : KtxScreen {
    private val gameAtlas = TextureAtlas("graphics/game.atlas")
    private val gameStage = Stage(ExtendViewport(16f, 9f))
    private val uiStage = Stage(ExtendViewport(320f, 180f))
    private val phWorld = createWorld(gravity = Vector2.Zero).apply {
        autoClearForces = false
    }
    private val eWorld = world {
        injectables {
            add(phWorld)
            add("GameStage", gameStage)
            add("UiStage", uiStage)
            add("GameAtlas", gameAtlas)
        }

        components {
            onAdd(PhysicComponent, onPhysicAdd)
            onRemove(PhysicComponent, onPhysicRemove)

            onAdd(ImageComponent, onImageAdd)
            onRemove(ImageComponent, onImageRemove)

            onAdd(StateComponent, onStateAdd)

            onAdd(AIComponent, onAiAdd)

            onAdd(FloatingTextComponent, onFloatingAdd)
            onRemove(FloatingTextComponent, onFloatingRemove)
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
        PlayerInputProcessor(eWorld, uiStage)
        gdxInputProcessor(uiStage)

        // UI
        uiStage.actors {
            gameView(GameModel(eWorld, gameStage))
            inventoryView(InventoryModel(eWorld, gameStage)) {
                this.isVisible = false
            }
        }
    }

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
        currentMap?.disposeSafely()
        disposeSkin()
    }
}