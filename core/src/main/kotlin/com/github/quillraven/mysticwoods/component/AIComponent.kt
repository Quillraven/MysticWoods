package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibraryManager
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class AIEntity(
    val entity: Entity,
    world: World,
    private val aiCmps: ComponentMapper<AIComponent> = world.mapper(),
    private val lifeCmps: ComponentMapper<LifeComponent> = world.mapper(),
    private val playerCmps: ComponentMapper<PlayerComponent> = world.mapper(),
    private val attackCmps: ComponentMapper<AttackComponent> = world.mapper(),
) {
    val isDead: Boolean
        get() = lifeCmps[entity].isDead()

    val canAttack: Boolean
        get() = attackCmps[entity].isReady()

    fun firstOrNullNearbyEnemy(): Entity? {
        return aiCmps[entity].nearbyEntities.firstOrNull { it in playerCmps }
    }

    fun attack() {
        attackCmps[entity].doAttack = true
    }
}

data class AIComponent(
    val nearbyEntities: MutableSet<Entity> = mutableSetOf(),
    var treePath: String = "",
) {
    lateinit var behaviorTree: BehaviorTree<AIEntity>

    companion object {
        class AIComponentListener(
            private val world: World
        ) : ComponentListener<AIComponent> {
            private val bTreeManager = BehaviorTreeLibraryManager.getInstance()

            override fun onComponentAdded(entity: Entity, component: AIComponent) {
                if (component.treePath.isNotBlank()) {
                    component.behaviorTree = bTreeManager.createBehaviorTree(
                        component.treePath,
                        AIEntity(entity, world)
                    )
                }
            }

            override fun onComponentRemoved(entity: Entity, component: AIComponent) {
                if (component.treePath.isNotBlank()) {
                    bTreeManager.disposeBehaviorTree(component.treePath, component.behaviorTree)
                }
            }
        }
    }
}