package com.github.quillraven.mysticwoods.actor

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.mysticwoods.screen.TestScreen
import ktx.scene2d.Scene2DSkin

class FlipImageTest : TestScreen() {

    private lateinit var rotatingImg: Actor
    private lateinit var rotatingFlipImg1: Actor
    private lateinit var rotatingFlipImg2: Actor

    private fun testImg(x: Float, y: Float): Actor = Image(Scene2DSkin.defaultSkin.getDrawable("sword")).apply {
        setScaling(Scaling.fill)
        setPosition(x, y)
        setSize(16f, 16f)
        setOrigin(8f, 8f)
    }

    private fun testFlipImg(x: Float, y: Float, flip: Boolean = false): Actor = FlipImage().apply {
        drawable = Scene2DSkin.defaultSkin.getDrawable("sword")
        setScaling(Scaling.fill)
        setPosition(x, y)
        setSize(16f, 16f)
        setOrigin(8f, 8f)
        flipX = flip
    }

    override fun show() {
        super.show()
        uiStage.isDebugAll = true

        uiStage.addActor(testImg(16f, 16f))

        uiStage.addActor(testFlipImg(16f, 64f))
        uiStage.addActor(testFlipImg(48f, 64f, flip = true))

        rotatingImg = testImg(128f, 16f)
        rotatingFlipImg1 = testFlipImg(128f, 64f)
        rotatingFlipImg2 = testFlipImg(160f, 64f, flip = true)
        uiStage.addActor(rotatingImg)
        uiStage.addActor(rotatingFlipImg1)
        uiStage.addActor(rotatingFlipImg2)
    }

    override fun render(delta: Float) {
        super.render(delta)
        rotatingImg.rotateBy(90f * delta)
        rotatingFlipImg1.rotateBy(90f * delta)
        rotatingFlipImg2.rotateBy(90f * delta)
    }

}