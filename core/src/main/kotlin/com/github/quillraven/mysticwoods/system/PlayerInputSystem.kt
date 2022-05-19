package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.component.AttackComponent
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import ktx.app.KtxInputAdapter

@AllOf([PlayerComponent::class])
class PlayerInputSystem(
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val attackCmps: ComponentMapper<AttackComponent>
) : IteratingSystem(), KtxInputAdapter {
    private var updateMovement = false
    private var triggerAttack = false
    private var playerCos = 0f
    private var playerSin = 0f
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
                UP -> playerSin = 1f
                DOWN -> playerSin = -1f
                RIGHT -> playerCos = 1f
                LEFT -> playerCos = -1f
            }
            return true
        } else if (keycode == SPACE) {
            triggerAttack = true
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        pressedKeys -= keycode
        if (keycode.isMovementKey()) {
            updateMovement = true
            when (keycode) {
                UP -> playerSin = if (isPressed(DOWN)) -1f else 0f
                DOWN -> playerSin = if (isPressed(UP)) 1f else 0f
                RIGHT -> playerCos = if (isPressed(LEFT)) -1f else 0f
                LEFT -> playerCos = if (isPressed(RIGHT)) 1f else 0f
            }
            return true
        }
        return false
    }

    override fun onTickEntity(entity: Entity) {
        if (updateMovement) {
            updateMovement = false
            with(moveCmps[entity]) {
                cos = playerCos
                sin = playerSin
            }
        }
        if (triggerAttack) {
            triggerAttack = false
            attackCmps[entity].doAttack = true
        }
    }
}