package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.FloatingTextComponent
import ktx.math.vec2

class FloatingTextSystem(
    private val gameStage: Stage = inject("GameStage"),
    private val uiStage: Stage = inject("UiStage"),
) : IteratingSystem(family { all(FloatingTextComponent) }) {
    private val uiLocation = vec2()
    private val uiTarget = vec2()

    override fun onTickEntity(entity: Entity) {
        with(entity[FloatingTextComponent]) {
            if (time >= lifeSpan) {
                world.remove(entity)
                return
            }

            /**
             * convert game coordinates to UI coordinates
             * 1) project = stage to screen coordinates
             * 2) unproject = screen to stage coordinates
             */
            uiLocation.set(txtLocation)
            gameStage.viewport.project(uiLocation)
            uiStage.viewport.unproject(uiLocation)
            uiTarget.set(txtTarget)
            gameStage.viewport.project(uiTarget)
            uiStage.viewport.unproject(uiTarget)

            // interpolate
            uiLocation.interpolate(uiTarget, (time / lifeSpan).coerceAtMost(1f), Interpolation.smooth2)
            label.setPosition(uiLocation.x, uiStage.viewport.worldHeight - uiLocation.y)

            time += deltaTime
        }
    }
}