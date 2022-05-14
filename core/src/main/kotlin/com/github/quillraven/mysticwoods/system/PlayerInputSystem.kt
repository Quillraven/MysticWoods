package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import ktx.app.KtxInputAdapter
import ktx.math.vec2

@AllOf([PlayerComponent::class])
class PlayerInputSystem(
    private val moveCmps: ComponentMapper<MoveComponent>
) : IteratingSystem(), KtxInputAdapter {
    private var updateMovement = false
    private val angle = vec2()
    private val pressedKeys = mutableSetOf<Int>()

    init {
        Gdx.input.inputProcessor = this
    }

    private fun Int.isMovementKey(): Boolean {
        return this == UP || this == DOWN || this == LEFT || this == RIGHT
    }

    private fun isPressed(keycode: Int): Boolean = keycode in pressedKeys

    override fun keyDown(keycode: Int): Boolean {
        pressedKeys += keycode
        if (keycode.isMovementKey()) {
            updateMovement = true
            when (keycode) {
                UP -> angle.y = 1f
                DOWN -> angle.y = -1f
                RIGHT -> angle.x = 1f
                LEFT -> angle.x = -1f
            }
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        pressedKeys -= keycode
        if (keycode.isMovementKey()) {
            updateMovement = true
            when (keycode) {
                UP -> angle.y = if (isPressed(DOWN)) -1f else 0f
                DOWN -> angle.y = if (isPressed(UP)) 1f else 0f
                RIGHT -> angle.x = if (isPressed(LEFT)) -1f else 0f
                LEFT -> angle.x = if (isPressed(RIGHT)) 1f else 0f
            }
            return true
        }
        return false
    }

    override fun onTickEntity(entity: Entity) {
        if (updateMovement) {
            updateMovement = false
            moveCmps[entity].angle.set(angle)
        }
    }
}