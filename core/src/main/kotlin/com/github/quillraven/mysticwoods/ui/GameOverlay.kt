package com.github.quillraven.mysticwoods.ui

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.github.quillraven.mysticwoods.ui.widget.CharacterInfo
import com.github.quillraven.mysticwoods.ui.widget.characterInfo
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.actors.txt
import ktx.scene2d.*

class GameOverlay(
    private val model: GameOverlayModel,
    skin: Skin
) : Table(skin), KTable {

    private val enemyInfo: CharacterInfo
    private val playerInfo: CharacterInfo
    private val popupLabel: Label

    init {
        // UI
        setFillParent(true)

        enemyInfo = characterInfo(null, skin) {
            this.alpha = 0f
            it.row()
        }

        table {
            background = skin[Drawables.FRAME_BGD]

            this@GameOverlay.popupLabel = label(text = "", style = Labels.FRAME.skinKey) { lblCell ->
                this.setAlignment(Align.topLeft)
                this.wrap = true
                lblCell.expand().fill().pad(14f)
            }

            this.alpha = 0f
            it.expand().width(130f).height(90f).top().row()
        }

        playerInfo = characterInfo(Drawables.PLAYER, skin)

        // data binding
        model.onPropertyChange(model::playerLife) { lifePercentage ->
            playerLife(lifePercentage)
        }
        model.onPropertyChange(model::enemyLife) { lifePercentage ->
            enemyLife(lifePercentage)
        }
        model.onPropertyChange(model::lootText) { lootInfo ->
            popup(lootInfo)
        }
    }

    fun playerLife(percentage: Float) = playerInfo.life(percentage)

    fun playerMana(percentage: Float) = playerInfo.mana(percentage)

    private fun Actor.resetFadeOutDelay() {
        this.actions
            .filterIsInstance<SequenceAction>()
            .lastOrNull()
            ?.let { sequence ->
                val delay = sequence.actions.last() as DelayAction
                delay.time = 0f
            }
    }

    fun showEnemyInfo(charDrawable: Drawables, lifePercentage: Float, manaPercentage: Float) {
        enemyInfo.character(charDrawable)
        enemyInfo.life(lifePercentage, 0f)
        enemyInfo.mana(manaPercentage, 0f)

        if (enemyInfo.alpha == 0f) {
            // enemy info hidden -> fade it in
            enemyInfo.clearActions()
            enemyInfo += sequence(fadeIn(1f, Interpolation.bounceIn), delay(5f, fadeOut(0.5f)))
        } else {
            // enemy info already fading in -> just reset the fadeout timer. No need to fade in again
            enemyInfo.resetFadeOutDelay()
        }
    }

    fun enemyLife(percentage: Float) = enemyInfo.also { it.resetFadeOutDelay() }.life(percentage)

    fun enemyMana(percentage: Float) = enemyInfo.also { it.resetFadeOutDelay() }.mana(percentage)

    fun popup(infoText: String) {
        popupLabel.txt = infoText
        if (popupLabel.parent.alpha == 0f) {
            popupLabel.parent.clearActions()
            popupLabel.parent += sequence(fadeIn(0.2f), delay(4f, fadeOut(0.75f)))
        } else {
            popupLabel.parent.resetFadeOutDelay()
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.gameOverlay(
    model: GameOverlayModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: GameOverlay.(S) -> Unit = {}
): GameOverlay = actor(GameOverlay(model, skin), init)