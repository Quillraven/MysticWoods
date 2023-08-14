package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.github.quillraven.mysticwoods.dialog.dialog
import com.github.quillraven.mysticwoods.event.EntityDialogEvent
import com.github.quillraven.mysticwoods.event.fire
import com.github.quillraven.mysticwoods.ui.Drawables
import com.github.quillraven.mysticwoods.ui.model.DialogModel
import com.github.quillraven.mysticwoods.ui.model.GameModel
import com.github.quillraven.mysticwoods.ui.view.DialogView
import com.github.quillraven.mysticwoods.ui.view.GameView
import com.github.quillraven.mysticwoods.ui.view.dialogView
import com.github.quillraven.mysticwoods.ui.view.gameView
import ktx.scene2d.actors

class UiTestScreen : TestScreen() {
    private val gameModel = GameModel(eWorld, gameStage)
    private lateinit var gameOverlay: GameView

    private val dialogModel = DialogModel(gameStage)
    private lateinit var dialogOverlay: DialogView

    override fun show() {
        super.show()
        uiStage.clear()
        uiStage.actors {
            gameOverlay = gameView(gameModel)
            dialogOverlay = dialogView(dialogModel)
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
                gameOverlay.playerLife(0f)
                gameOverlay.playerMana(0f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_2) -> {
                gameOverlay.playerLife(0.25f)
                gameOverlay.playerMana(0.25f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_3) -> {
                gameOverlay.playerLife(0.5f)
                gameOverlay.playerMana(0.5f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_4) -> {
                gameOverlay.playerLife(0.75f)
                gameOverlay.playerMana(0.75f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_5) -> {
                gameOverlay.playerLife(1f)
                gameOverlay.playerMana(1f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_6) -> {
                gameOverlay.enemyLife(0f)
                gameOverlay.enemyMana(0f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_7) -> {
                gameOverlay.enemyLife(0.25f)
                gameOverlay.enemyMana(0.25f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_8) -> {
                gameOverlay.enemyLife(0.5f)
                gameOverlay.enemyMana(0.5f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_9) -> {
                gameOverlay.enemyLife(0.75f)
                gameOverlay.enemyMana(0.75f)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_0) -> {
                gameOverlay.enemyLife(1f)
                gameOverlay.enemyMana(1f)
            }

            Gdx.input.isKeyJustPressed(Keys.Q) -> {
                gameOverlay.showEnemyInfo(Drawables.SLIME, 1f, 1f)
            }

            Gdx.input.isKeyJustPressed(Keys.W) -> {
                gameOverlay.showEnemyInfo(Drawables.PLAYER, 0.5f, 0.25f)
            }

            Gdx.input.isKeyJustPressed(Keys.E) -> {
                gameOverlay.popup("You found some [#ff0000]awesome stuff[]!")
            }

            Gdx.input.isKeyJustPressed(Keys.D) -> {
                val dialog = dialog("testDialog") {
                    node(0, "Dialog text page 1") {
                        option("Ok") {
                            action = { this@dialog.goToNode(1) }
                        }
                    }
                    node(1, "Dialog text page 2") {
                        option("Back") {
                            action = { this@dialog.goToNode(0) }
                        }
                        option("End") {
                            action = { this@dialog.end() }
                        }
                    }
                    start()
                }
                gameStage.fire(EntityDialogEvent(dialog))
            }
        }
    }
}