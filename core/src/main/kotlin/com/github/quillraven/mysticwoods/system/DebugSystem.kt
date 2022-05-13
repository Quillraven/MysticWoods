package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.profiling.GLProfiler
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.Qualifier

class DebugSystem(
    private val physicWorld: World,
    @Qualifier("GameStage") stage: Stage,
) : IntervalSystem() {
    private val physicRenderer = Box2DDebugRenderer()
    private val profiler = GLProfiler(Gdx.graphics)
    private val camera = stage.camera

    init {
        stage.isDebugAll = true
        profiler.enable()
    }

    override fun onTick() {
        Gdx.graphics.setTitle("FPS:${Gdx.graphics.framesPerSecond}, DrawCalls:${profiler.drawCalls}, Binds:${profiler.textureBindings}")
        physicRenderer.render(physicWorld, camera.combined)
        profiler.reset()
    }

    override fun onDispose() {
        physicRenderer.dispose()
    }
}