package com.github.quillraven.mysticwoods

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.github.quillraven.mysticwoods.screen.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class MysticWoods : KtxGame<KtxScreen>() {
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        addScreen(GameScreen())
        setScreen<GameScreen>()
    }

    companion object {
        // 32px = 1m in our physic world
        const val UNIT_SCALE = 1 / 32f
    }
}
