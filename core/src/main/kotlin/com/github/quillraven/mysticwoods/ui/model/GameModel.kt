package com.github.quillraven.mysticwoods.ui.model

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.component.AnimationComponent
import com.github.quillraven.mysticwoods.component.LifeComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import com.github.quillraven.mysticwoods.event.EntityAggroEvent
import com.github.quillraven.mysticwoods.event.EntityLootEvent
import com.github.quillraven.mysticwoods.event.EntityReviveEvent
import com.github.quillraven.mysticwoods.event.EntityTakeDamageEvent

class GameModel(
    private val world: World,
    stage: Stage,
) : PropertyChangeSource(), EventListener {

    var playerLife by propertyNotify(1f)

    private var lastEnemy = Entity(-1)
    var enemyType by propertyNotify("")

    var enemyLife by propertyNotify(1f)

    var lootText by propertyNotify("")

    init {
        stage.addListener(this)
    }

    private fun updateEnemy(enemy: Entity) = with(world) {
        val lifeCmp = enemy[LifeComponent]
        enemyLife = lifeCmp.life / lifeCmp.max
        if (lastEnemy != enemy) {
            // update enemy type
            lastEnemy = enemy
            enemy.getOrNull(AnimationComponent)?.atlasKey?.let { enemyType ->
                this@GameModel.enemyType = enemyType
            }
        }
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is EntityTakeDamageEvent -> {
                with(world) {
                    val isPlayer = event.entity has PlayerComponent
                    val lifeCmp = event.entity[LifeComponent]
                    if (isPlayer) {
                        playerLife = lifeCmp.life / lifeCmp.max
                    } else {
                        updateEnemy(event.entity)
                    }
                }
            }

            is EntityReviveEvent -> {
                with(world) {
                    val isPlayer = event.entity has PlayerComponent
                    val lifeCmp = event.entity[LifeComponent]
                    if (isPlayer) {
                        playerLife = lifeCmp.life / lifeCmp.max
                    }
                }
            }

            is EntityLootEvent -> {
                lootText = "You found some [#ff0000]incredible[] stuff!"
            }

            is EntityAggroEvent -> {
                with(world) {
                    val source = event.aiEntity
                    val sourceType = source.getOrNull(AnimationComponent)?.atlasKey
                    val target = event.target
                    val isTargetPlayer = target has PlayerComponent
                    if (isTargetPlayer && sourceType != null) {
                        updateEnemy(source)
                    }
                }
            }

            else -> return false
        }
        return true
    }
}