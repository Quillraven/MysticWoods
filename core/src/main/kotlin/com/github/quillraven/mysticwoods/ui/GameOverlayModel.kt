package com.github.quillraven.mysticwoods.ui

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.component.LifeComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import com.github.quillraven.mysticwoods.event.EntityReviveEvent
import com.github.quillraven.mysticwoods.event.EntityTakeDamageEvent
import kotlin.reflect.KProperty

abstract class PropertyChangeSource {
    @PublishedApi
    internal val listenersMap = mutableMapOf<KProperty<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> onPropertyChange(property: KProperty<T>, noinline action: (T) -> Unit) {
        val actions = listenersMap.getOrPut(property) { mutableListOf() } as MutableList<(T) -> Unit>
        actions += action
    }

    fun notify(property: KProperty<*>, value: Any) {
        listenersMap[property]?.forEach { it(value) }
    }
}

class GameOverlayModel(
    world: World,
    stage: Stage,
) : PropertyChangeSource(), EventListener {

    private val playerCmps: ComponentMapper<PlayerComponent> = world.mapper()
    private val lifeCmps: ComponentMapper<LifeComponent> = world.mapper()
    var playerLife = 1f
        private set(value) {
            notify(::playerLife, value)
            field = value
        }
    var enemyLife = 1f
        private set(value) {
            notify(::enemyLife, value)
            field = value
        }

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is EntityTakeDamageEvent -> {
                val isPlayer = event.entity in playerCmps
                val lifeCmp = lifeCmps[event.entity]
                if (isPlayer) {
                    playerLife = lifeCmp.life / lifeCmp.max
                } else {
                    enemyLife = lifeCmp.life / lifeCmp.max
                }
            }

            is EntityReviveEvent -> {
                val isPlayer = event.entity in playerCmps
                val lifeCmp = lifeCmps[event.entity]
                if (isPlayer) {
                    playerLife = lifeCmp.life / lifeCmp.max
                }
            }

            else -> return false
        }
        return true
    }
}