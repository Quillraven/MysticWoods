package com.github.quillraven.mysticwoods

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.github.quillraven.mysticwoods.screen.GameScreen
import com.github.quillraven.mysticwoods.service.MapService
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.disposeSafely

class MysticWoods : KtxGame<KtxScreen>() {
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        addScreen(GameScreen())
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        MapService.disposeSafely()
    }

    companion object {
        // 16px = 1m in our physic world
        const val UNIT_SCALE = 1 / 16f
    }
}
