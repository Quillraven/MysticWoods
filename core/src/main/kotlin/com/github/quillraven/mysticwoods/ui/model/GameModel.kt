package com.github.quillraven.mysticwoods.ui.model

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentMapper
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
    world: World,
    stage: Stage,
) : PropertyChangeSource(), EventListener {

    private val playerCmps: ComponentMapper<PlayerComponent> = world.mapper()
    private val lifeCmps: ComponentMapper<LifeComponent> = world.mapper()
    private val aniCmps: ComponentMapper<AnimationComponent> = world.mapper()

    var playerLife by propertyNotify(1f)

    private var lastEnemy = Entity(-1)
    var enemyType by propertyNotify("")

    var enemyLife by propertyNotify(1f)

    var lootText by propertyNotify("")

    init {
        stage.addListener(this)
    }

    private fun updateEnemy(enemy: Entity) {
        val lifeCmp = lifeCmps[enemy]
        enemyLife = lifeCmp.life / lifeCmp.max
        if (lastEnemy != enemy) {
            // update enemy type
            lastEnemy = enemy
            aniCmps.getOrNull(enemy)?.atlasKey?.let { enemyType ->
                this.enemyType = enemyType
            }
        }
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is EntityTakeDamageEvent -> {
                val isPlayer = event.entity in playerCmps
                val lifeCmp = lifeCmps[event.entity]
                if (isPlayer) {
                    playerLife = lifeCmp.life / lifeCmp.max
                } else {
                    updateEnemy(event.entity)
                }
            }

            is EntityReviveEvent -> {
                val isPlayer = event.entity in playerCmps
                val lifeCmp = lifeCmps[event.entity]
                if (isPlayer) {
                    playerLife = lifeCmp.life / lifeCmp.max
                }
            }

            is EntityLootEvent -> {
                lootText = "You found some [#ff0000]incredible[] stuff!"
            }

            is EntityAggroEvent -> {
                val source = event.aiEntity
                val sourceType = aniCmps.getOrNull(source)?.atlasKey
                val target = event.target
                val isTargetPlayer = target in playerCmps
                if (isTargetPlayer && sourceType != null) {
                    updateEnemy(source)
                }
            }

            else -> return false
        }
        return true
    }
}