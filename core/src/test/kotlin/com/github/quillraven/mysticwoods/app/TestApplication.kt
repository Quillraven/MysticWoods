package com.github.quillraven.mysticwoods.app

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.github.quillraven.mysticwoods.screen.TestScreen
import ktx.app.KtxGame

class TestApplication(private val testScreenFactory: () -> TestScreen) : KtxGame<TestScreen>() {
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        val screen = testScreenFactory()
        addScreen(screen)
        setScreen(TestScreen::class.java)
    }
}