package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.utils.random.ConstantFloatDistribution
import com.badlogic.gdx.ai.utils.random.UniformFloatDistribution
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.behavior.CanAttack
import com.github.quillraven.mysticwoods.behavior.IsEnemyNearby
import com.github.quillraven.mysticwoods.behavior.slime.AttackTask
import com.github.quillraven.mysticwoods.behavior.slime.IdleTask
import com.github.quillraven.mysticwoods.behavior.slime.MoveTask
import com.github.quillraven.mysticwoods.behavior.slime.WanderTask
import com.github.quillraven.mysticwoods.event.fire
import ktx.ai.*
import ktx.app.gdxError
import ktx.math.component1
import ktx.math.component2

class AIEntity(
    val entity: Entity,
    private val world: World,
    private val stage: Stage,
) {
    val location: Vector2
        get() = with(world) { entity[PhysicComponent].body.position }

    val target: Entity
        get() = with(world) { entity[AIComponent].target }

    fun inTargetRange(range: Float): Boolean = with(world) {
        val aiCmp = entity[AIComponent]
        if (aiCmp.target == AIComponent.NO_TARGET) {
            return true
        }

        val physicCmp = entity[PhysicComponent]
        val targetPhysicCmp = aiCmp.target[PhysicComponent]
        val (sourceX, sourceY) = physicCmp.body.position
        val (sourceOffX, sourceOffY) = physicCmp.offset
        var (sourceSizeX, sourceSizeY) = physicCmp.size
        sourceSizeX += range
        sourceSizeY += range
        val (targetX, targetY) = targetPhysicCmp.body.position
        val (targetOffX, targetOffY) = targetPhysicCmp.offset
        val (targetSizeX, targetSizeY) = targetPhysicCmp.size

        TMP_RECT1.set(
            sourceOffX + sourceX - sourceSizeX * 0.5f,
            sourceOffY + sourceY - sourceSizeY * 0.5f,
            sourceSizeX,
            sourceSizeY
        )
        TMP_RECT2.set(
            targetOffX + targetX - targetSizeX * 0.5f,
            targetOffY + targetY - targetSizeY * 0.5f,
            targetSizeX,
            targetSizeY
        )
        return TMP_RECT1.overlaps(TMP_RECT2)
    }

    fun inRange(range: Float, target: Vector2): Boolean = with(world) {
        val physicCmp = entity[PhysicComponent]
        val (sourceX, sourceY) = physicCmp.body.position
        val (sourceOffX, sourceOffY) = physicCmp.offset
        var (sourceSizeX, sourceSizeY) = physicCmp.size
        sourceSizeX += range
        sourceSizeY += range

        TMP_RECT1.set(
            sourceOffX + sourceX - sourceSizeX * 0.5f,
            sourceOffY + sourceY - sourceSizeY * 0.5f,
            sourceSizeX,
            sourceSizeY
        )
        return TMP_RECT1.contains(target)
    }

    fun canAttack(extraRange: Float): Boolean = with(world) {
        val aiCmp = entity[AIComponent]
        if (aiCmp.target == AIComponent.NO_TARGET) {
            return false
        }

        val attackCmp = entity[AttackComponent]
        return attackCmp.isReady() && inTargetRange(extraRange)
    }

    fun findNearbyEnemy(): Boolean = with(world) {
        val aiCmp = entity[AIComponent]
        aiCmp.target = aiCmp.nearbyEntities.firstOrNull {
            it has PlayerComponent && !it[LifeComponent].isDead()
        } ?: AIComponent.NO_TARGET
        return aiCmp.target != AIComponent.NO_TARGET
    }

    fun checkTargetStillNearby() = with(world) {
        val aiCmp = entity[AIComponent]
        if (aiCmp.target !in aiCmp.nearbyEntities) {
            aiCmp.target = AIComponent.NO_TARGET
        }
    }

    fun attack() = with(world) {
        with(entity[AttackComponent]) {
            doAttack = true
            startAttack()
        }
        val x = entity[PhysicComponent].body.position.x
        val targetX = target[PhysicComponent].body.position.x
        entity[ImageComponent].image.flipX = targetX < x
    }

    fun moveToTarget() = with(world) {
        val aiCmp = entity[AIComponent]
        if (aiCmp.target == AIComponent.NO_TARGET) {
            with(entity[MoveComponent]) { cosSin.setZero() }
            return@with
        }

        val targetPhysicCmp = aiCmp.target[PhysicComponent]
        moveToLocation(targetPhysicCmp.body.position)
    }

    fun moveToLocation(target: Vector2): Vector2 = with(world) {
        val (targetX, targetY) = target
        val physicCmp = entity[PhysicComponent]
        val (sourceX, sourceY) = physicCmp.body.position
        with(entity[MoveComponent]) {
            val angleRad = MathUtils.atan2(targetY - sourceY, targetX - sourceX)
            cosSin.set(MathUtils.cos(angleRad), MathUtils.sin(angleRad))
        }
    }

    fun stopMovement(): Vector2 = with(world) {
        with(entity[MoveComponent]) { cosSin.setZero() }
    }

    fun moveSlow(slowed: Boolean) = with(world) {
        entity[MoveComponent].slow = slowed
    }

    fun animation(
        type: AnimationType,
        mode: Animation.PlayMode = Animation.PlayMode.LOOP,
        resetAnimation: Boolean = false
    ) = with(world) {
        with(entity[AnimationComponent]) {
            nextAnimation(type)
            this.mode = mode
            if (resetAnimation) {
                stateTime = 0f
            }
        }
    }

    fun isAnimationDone() = with(world) { entity[AnimationComponent].isAnimationFinished() }

    fun fireEvent(event: Event) {
        stage.fire(event)
    }

    companion object {
        val TMP_RECT1 = Rectangle()
        val TMP_RECT2 = Rectangle()
    }
}

enum class AIType {
    NONE, SLIME
}

data class AIComponent(
    val nearbyEntities: MutableSet<Entity> = mutableSetOf(),
    var type: AIType
) : Component<AIComponent> {
    lateinit var behaviorTree: BehaviorTree<AIEntity>
    var target: Entity = NO_TARGET

    override fun type() = AIComponent

    override fun World.onAdd(entity: Entity) {
        behaviorTree = newBehaviorTree(entity, this, this@AIComponent)
    }

    private fun World.newBehaviorTree(entity: Entity, world: World, component: AIComponent): BehaviorTree<AIEntity> {
        if (component.type == AIType.SLIME) {
            return behaviorTree {
                `object` = AIEntity(entity, world, inject("GameStage"))

                selector {
                    sequence {
                        guard = GdxAiSequence(IsEnemyNearby(), CanAttack(range = 1f))
                        add(AttackTask())
                        waitLeaf(UniformFloatDistribution(1.25f, 2.1f))
                    }

                    sequence {
                        guard = GdxAiSequence(IsEnemyNearby())
                        add(MoveTask(2f))
                    }

                    sequence {
                        guard = GdxAiRandom(ConstantFloatDistribution(0.25f))
                        add(IdleTask(UniformFloatDistribution(2f, 3.5f)))
                    }

                    add(WanderTask(6f))
                }
            }
        } else {
            gdxError("No behavior defined for ${component.type}")
        }
    }

    companion object : ComponentType<AIComponent>() {
        val NO_TARGET = Entity.NONE
    }
}