package com.github.quillraven.mysticwoods.component

import box2dLight.Light
import com.badlogic.gdx.graphics.Color
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity

data class LightComponent(
    var distance: ClosedFloatingPointRange<Float> = 2f..3.5f,
    var distanceTime: Float = 0f,
    var distanceDirection: Int = -1
) {
    lateinit var light: Light

    operator fun component4(): Light = light

    companion object {
        const val b2dPlayer: Short = 2
        const val b2dSlime: Short = 4
        const val b2dEnvironment: Short = 8
        val lightColor = Color(1f, 1f, 1f, 0.7f)

        class LightComponentListener : ComponentListener<LightComponent> {
            override fun onComponentAdded(entity: Entity, component: LightComponent) = Unit

            override fun onComponentRemoved(entity: Entity, component: LightComponent) {
                component.light.remove()
            }

        }
    }
}