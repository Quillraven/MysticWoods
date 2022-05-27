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
) : IntervalSystem(enabled = false) {
    private lateinit var physicRenderer: Box2DDebugRenderer
    private lateinit var profiler: GLProfiler
    private val camera = stage.camera

    init {
        if (enabled) {
            physicRenderer = Box2DDebugRenderer()
            profiler = GLProfiler(Gdx.graphics)
            stage.isDebugAll = true
            profiler.enable()
        }
    }

    override fun onTick() {
        Gdx.graphics.setTitle(
            buildString {
                append("FPS:${Gdx.graphics.framesPerSecond},")
                append("DrawCalls:${profiler.drawCalls},")
                append("Binds:${profiler.textureBindings},")
                append("Entities:${world.numEntities}")
            }
        )
        physicRenderer.render(physicWorld, camera.combined)
        profiler.reset()
    }

    override fun onDispose() {
        if (enabled) {
            physicRenderer.dispose()
        }
    }
}