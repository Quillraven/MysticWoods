package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.FloatingTextComponent
import ktx.math.vec2

@AllOf([FloatingTextComponent::class])
class FloatingTextSystem(
    private val textCmps: ComponentMapper<FloatingTextComponent>,
    @Qualifier("GameStage") private val gameStage: Stage,
    @Qualifier("UiStage") private val uiStage: Stage,
) : IteratingSystem() {
    private val uiLocation = vec2()
    private val uiTarget = vec2()

    override fun onTickEntity(entity: Entity) {
        with(textCmps[entity]) {
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