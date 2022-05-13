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

@AllOf([PlayerComponent::class])
class PlayerInputSystem(
    private val moveCmps: ComponentMapper<MoveComponent>
) : IteratingSystem(), KtxInputAdapter {
    private var lastPressedKey = UNKNOWN
    private val pressedKeys = mutableSetOf<Int>()
    private var updateMovement = false

    init {
        Gdx.input.inputProcessor = this
    }

    private fun Int.isMovementKey(): Boolean {
        return this == UP || this == DOWN || this == LEFT || this == RIGHT
    }

    private fun isPressed(keycode: Int): Boolean = keycode in pressedKeys

    private fun isJustPressed(keycode: Int): Boolean = lastPressedKey == keycode

    private fun updatePlayerMovement(moveCmp: MoveComponent) = with(moveCmp) {
        val direction = when {
            isJustPressed(UP) -> {
                // player wants to move up
                when {
                    isPressed(LEFT) -> 135f
                    isPressed(RIGHT) -> 45f
                    else -> 90f
                }
            }
            isJustPressed(DOWN) -> {
                // player wants to move down
                when {
                    isPressed(LEFT) -> 225f
                    isPressed(RIGHT) -> 315f
                    else -> 270f
                }
            }
            isJustPressed(LEFT) -> {
                // player wants to move left
                when {
                    isPressed(UP) -> 135f
                    isPressed(DOWN) -> 225f
                    else -> 180f
                }
            }
            isJustPressed(RIGHT) -> {
                // player wants to move right
                when {
                    isPressed(UP) -> 45f
                    isPressed(DOWN) -> 315f
                    else -> 0f
                }
            }
            else -> {
                // player released a movement button -> check which directions are still pressed
                when {
                    isPressed(RIGHT) -> {
                        when {
                            isPressed(UP) -> 45f
                            isPressed(DOWN) -> 315f
                            else -> 0f
                        }
                    }
                    isPressed(UP) -> {
                        when {
                            isPressed(RIGHT) -> 45f
                            isPressed(LEFT) -> 135f
                            else -> 90f
                        }
                    }
                    isPressed(LEFT) -> {
                        when {
                            isPressed(UP) -> 135f
                            isPressed(DOWN) -> 225f
                            else -> 180f
                        }
                    }
                    isPressed(DOWN) -> {
                        when {
                            isPressed(RIGHT) -> 315f
                            isPressed(LEFT) -> 225f
                            else -> 270f
                        }
                    }
                    else -> {
                        // no movement buttons pressed anymore
                        -1f
                    }
                }
            }
        }

        if (direction == -1f) {
            stop = true
        } else {
            stop = false
            direction(direction)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        pressedKeys.add(keycode)
        lastPressedKey = keycode
        if (keycode.isMovementKey()) {
            updateMovement = true
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        pressedKeys.remove(keycode)
        lastPressedKey = UNKNOWN
        if (keycode.isMovementKey()) {
            updateMovement = true
            return true
        }
        return keyDown(keycode)
    }

    override fun onTickEntity(entity: Entity) {
        if (updateMovement) {
            updateMovement = false
            updatePlayerMovement(moveCmps[entity])
        }
    }
}