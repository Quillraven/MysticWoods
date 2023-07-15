package com.github.quillraven.mysticwoods.component

import box2dLight.Light
import com.badlogic.gdx.graphics.Color
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

data class LightComponent(
    var distance: ClosedFloatingPointRange<Float> = 2f..3.5f,
    var distanceTime: Float = 0f,
    var distanceDirection: Int = -1,
    val light: Light
) : Component<LightComponent> {

    override fun type() = LightComponent

    override fun World.onRemoveComponent(entity: Entity) {
        light.remove()
    }

    companion object : ComponentType<LightComponent>() {
        const val b2dPlayer: Short = 2
        const val b2dSlime: Short = 4
        const val b2dEnvironment: Short = 8
        val lightColor = Color(1f, 1f, 1f, 0.7f)
    }
}
