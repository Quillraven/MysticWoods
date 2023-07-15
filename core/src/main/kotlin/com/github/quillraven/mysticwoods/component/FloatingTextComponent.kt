package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.actors.plusAssign
import ktx.math.vec2

data class FloatingTextComponent(
    val txtLocation: Vector2 = vec2(),
    val lifeSpan: Float = 0f,
    val label: Label
) : Component<FloatingTextComponent> {
    var time: Float = 0f
    val txtTarget: Vector2 = vec2()

    override fun type() = FloatingTextComponent

    override fun World.onAddComponent(entity: Entity) {
        label += fadeOut(lifeSpan, Interpolation.pow3OutInverse)
        inject<Stage>("UiStage").addActor(label)
        txtTarget.set(
            txtLocation.x + MathUtils.random(-1.5f, 1.5f),
            txtLocation.y + 1f
        )
    }

    override fun World.onRemoveComponent(entity: Entity) {
        inject<Stage>("UiStage").root.removeActor(label)
    }

    companion object : ComponentType<FloatingTextComponent>()
}