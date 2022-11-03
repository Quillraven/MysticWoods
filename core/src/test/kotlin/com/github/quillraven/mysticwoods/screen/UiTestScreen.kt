package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.github.quillraven.mysticwoods.ui.Drawables
import com.github.quillraven.mysticwoods.ui.model.GameModel
import com.github.quillraven.mysticwoods.ui.view.GameView
import com.github.quillraven.mysticwoods.ui.view.gameView
import ktx.scene2d.actors

class UiTestScreen : TestScreen() {
    private val model = GameModel(eWorld, gameStage)
    private lateinit var overlay: GameView

    override fun show() {
        super.show()
        uiStage.clear()
        uiStage.actors {
            overlay = gameView(model)
        }
        uiStage.isDebugAll = true
    }

    override fun render(delta: Float) {
        super.render(delta)
        when {
            Gdx.input.isKeyJustPressed(Keys.R) -> {
                hide()
                show()
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_1) -> {
                overlay.playerLife(0f)
                overlay.playerMana(0f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_2) -> {
                overlay.playerLife(0.25f)
                overlay.playerMana(0.25f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_3) -> {
                overlay.playerLife(0.5f)
                overlay.playerMana(0.5f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_4) -> {
                overlay.playerLife(0.75f)
                overlay.playerMana(0.75f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_5) -> {
                overlay.playerLife(1f)
                overlay.playerMana(1f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_6) -> {
                overlay.enemyLife(0f)
                overlay.enemyMana(0f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_7) -> {
                overlay.enemyLife(0.25f)
                overlay.enemyMana(0.25f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_8) -> {
                overlay.enemyLife(0.5f)
                overlay.enemyMana(0.5f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_9) -> {
                overlay.enemyLife(0.75f)
                overlay.enemyMana(0.75f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_0) -> {
                overlay.enemyLife(1f)
                overlay.enemyMana(1f)
            }

            Gdx.input.isKeyJustPressed(Keys.Q) -> {
                overlay.showEnemyInfo(Drawables.SLIME, 1f, 1f)
            }

            Gdx.input.isKeyJustPressed(Keys.W) -> {
                overlay.showEnemyInfo(Drawables.PLAYER, 0.5f, 0.25f)
            }

            Gdx.input.isKeyJustPressed(Keys.E) -> {
                overlay.popup("You found some [#ff0000]awesome stuff[]!")
            }
        }
    }
}