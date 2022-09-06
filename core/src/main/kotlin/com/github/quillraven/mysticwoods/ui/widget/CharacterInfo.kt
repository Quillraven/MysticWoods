package com.github.quillraven.mysticwoods.ui.widget

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.mysticwoods.ui.Drawables
import com.github.quillraven.mysticwoods.ui.get
import ktx.actors.plusAssign
import ktx.scene2d.*

@Scene2dDsl
class CharacterInfo(
    charDrawable: Drawables?,
    private val skin: Skin
) : WidgetGroup(), KGroup {
    private val background: Image = Image(skin[Drawables.CHAR_INFO_BGD])
    private val charBgd: Image = Image(if (charDrawable == null) null else skin[charDrawable])
    private val lifeBar: Image = Image(skin[Drawables.LIFE_BAR])
    private val manaBar: Image = Image(skin[Drawables.MANA_BAR])

    init {
        this += background
        this += charBgd.apply {
            setPosition(2f, 2f)
            setSize(22f, 20f)
            setScaling(Scaling.contain)
        }
        this += lifeBar.apply { setPosition(26f, 19f) }
        this += manaBar.apply { setPosition(26f, 13f) }
    }

    override fun getPrefWidth() = background.drawable.minWidth

    override fun getPrefHeight() = background.drawable.minHeight

    fun character(charDrawable: Drawables?) {
        if (charDrawable == null) {
            charBgd.drawable = null
        } else {
            charBgd.drawable = skin[charDrawable]
        }
    }

    fun life(percentage: Float, duration: Float = 0.75f) {
        lifeBar.clearActions()
        lifeBar += scaleTo(MathUtils.clamp(percentage, 0f, 1f), 1f, duration)
    }

    fun mana(percentage: Float, duration: Float = 0.75f) {
        manaBar.clearActions()
        manaBar += scaleTo(MathUtils.clamp(percentage, 0f, 1f), 1f, duration)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.characterInfo(
    charDrawable: Drawables?,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: CharacterInfo.(S) -> Unit = {}
): CharacterInfo = actor(CharacterInfo(charDrawable, skin), init)