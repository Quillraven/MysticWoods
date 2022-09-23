package com.github.quillraven.mysticwoods.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.component.AttackComponent
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import ktx.app.KtxInputAdapter

class PlayerInputProcessor(
    private val world: World,
) : KtxInputAdapter {
    private val playerEntities = world.family { all(PlayerComponent) }
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

    private fun updatePlayerMovement() {
        playerEntities.forEach { player ->
            with(player[MoveComponent]) {
                cos = playerCos
                sin = playerSin
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        pressedKeys += keycode
        if (keycode.isMovementKey()) {
            when (keycode) {
                UP -> playerSin = 1f
                DOWN -> playerSin = -1f
                RIGHT -> playerCos = 1f
                LEFT -> playerCos = -1f
            }
            updatePlayerMovement()
            return true
        } else if (keycode == SPACE) {
            playerEntities.forEach { it[AttackComponent].doAttack = true }
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        pressedKeys -= keycode
        if (keycode.isMovementKey()) {
            when (keycode) {
                UP -> playerSin = if (isPressed(DOWN)) -1f else 0f
                DOWN -> playerSin = if (isPressed(UP)) 1f else 0f
                RIGHT -> playerCos = if (isPressed(LEFT)) -1f else 0f
                LEFT -> playerCos = if (isPressed(RIGHT)) 1f else 0f
            }
            updatePlayerMovement()
            return true
        }
        return false
    }
}