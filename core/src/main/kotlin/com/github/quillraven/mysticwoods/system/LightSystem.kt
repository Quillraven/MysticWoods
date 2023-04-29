package com.github.quillraven.mysticwoods.system

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.LightComponent

@AllOf([LightComponent::class])
class LightSystem(
    private val lightCmps: ComponentMapper<LightComponent>,
    private val rayHandler: RayHandler,
) : IteratingSystem() {

    private var ambientTransitionTime = 1f
    private var ambientColor = Color(1f, 1f, 1f, 1f)
    private var ambientFrom = dayLightColor
    private var ambientTo = nightLightColor

    override fun onTick() {
        super.onTick()

        if (Gdx.input.isKeyJustPressed(Input.Keys.N) && ambientTransitionTime == 1f) {
            ambientTransitionTime = 0f
            ambientTo = nightLightColor
            ambientFrom = dayLightColor
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.D) && ambientTransitionTime == 1f) {
            ambientTransitionTime = 0f
            ambientTo = dayLightColor
            ambientFrom = nightLightColor
        }

        if (ambientTransitionTime < 1f) {
            ambientTransitionTime = (ambientTransitionTime + deltaTime * 0.5f).coerceAtMost(1f)

            ambientColor.r = distanceInterpolation.apply(ambientFrom.r, ambientTo.r, ambientTransitionTime)
            ambientColor.g = distanceInterpolation.apply(ambientFrom.g, ambientTo.g, ambientTransitionTime)
            ambientColor.b = distanceInterpolation.apply(ambientFrom.b, ambientTo.b, ambientTransitionTime)
            ambientColor.a = distanceInterpolation.apply(ambientFrom.a, ambientTo.a, ambientTransitionTime)

            rayHandler.setAmbientLight(ambientColor)
        }
    }

    override fun onTickEntity(entity: Entity) {
        val lightCmp = lightCmps[entity]
        val (distance, time, direction, b2dLight) = lightCmp

        lightCmp.distanceTime = (time + direction * deltaTime).coerceIn(0f, 1f)
        if (lightCmp.distanceTime == 0f || lightCmp.distanceTime == 1f) {
            // change light expand direction (from in to out and vice versa)
            lightCmp.distanceDirection *= -1
        }

        b2dLight.distance = distanceInterpolation.apply(distance.start, distance.endInclusive, lightCmp.distanceTime)
    }

    companion object {
        private val distanceInterpolation: Interpolation = Interpolation.smoother
        val dayLightColor: Color = Color.WHITE
        private val nightLightColor: Color = Color.ROYAL
    }

}