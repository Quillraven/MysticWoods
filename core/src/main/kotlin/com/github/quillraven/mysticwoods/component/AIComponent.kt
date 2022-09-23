package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.AIComponent.Companion.NO_TARGET
import com.github.quillraven.mysticwoods.event.fire
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
        if (aiCmp.target == NO_TARGET) {
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
        if (aiCmp.target == NO_TARGET) {
            return false
        }

        val attackCmp = entity[AttackComponent]
        return attackCmp.isReady() && inTargetRange(extraRange)
    }

    fun findNearbyEnemy(): Boolean = with(world) {
        val aiCmp = entity[AIComponent]
        aiCmp.target = aiCmp.nearbyEntities.firstOrNull {
            it has PlayerComponent && !it[LifeComponent].isDead()
        } ?: NO_TARGET
        return aiCmp.target != NO_TARGET
    }

    fun checkTargetStillNearby() = with(world) {
        val aiCmp = entity[AIComponent]
        if (aiCmp.target !in aiCmp.nearbyEntities) {
            aiCmp.target = NO_TARGET
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
        if (aiCmp.target == NO_TARGET) {
            with(entity[MoveComponent]) {
                cos = 0f
                sin = 0f
            }
            return
        }

        val targetPhysicCmp = aiCmp.target[PhysicComponent]
        moveToLocation(targetPhysicCmp.body.position)
    }

    fun moveToLocation(target: Vector2) = with(world) {
        val (targetX, targetY) = target
        val physicCmp = entity[PhysicComponent]
        val (sourceX, sourceY) = physicCmp.body.position
        with(entity[MoveComponent]) {
            val angleRad = MathUtils.atan2(targetY - sourceY, targetX - sourceX)
            cos = MathUtils.cos(angleRad)
            sin = MathUtils.sin(angleRad)
        }
    }

    fun stopMovement() = with(world) {
        with(entity[MoveComponent]) {
            cos = 0f
            sin = 0f
        }
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

data class AIComponent(
    val nearbyEntities: MutableSet<Entity> = mutableSetOf(),
    var treePath: String = "",
) : Component<AIComponent> {
    lateinit var behaviorTree: BehaviorTree<AIEntity>
    var target: Entity = NO_TARGET

    override fun type() = AIComponent

    companion object : ComponentType<AIComponent>() {
        private val bTreeParser = BehaviorTreeParser<AIEntity>()
        val NO_TARGET = Entity(-1)

        val onAiAdd: ComponentHook<AIComponent> = { entity, component ->
            if (component.treePath.isNotBlank()) {
                component.behaviorTree = bTreeParser.parse(
                    Gdx.files.internal(component.treePath),
                    AIEntity(entity, this, inject("GameStage"))
                )
            }
        }
    }
}