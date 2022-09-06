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
    world: World,
    private val stage: Stage,
) {

    private val aiCmps: ComponentMapper<AIComponent> = world.mapper()
    private val lifeCmps: ComponentMapper<LifeComponent> = world.mapper()
    private val playerCmps: ComponentMapper<PlayerComponent> = world.mapper()
    private val attackCmps: ComponentMapper<AttackComponent> = world.mapper()
    private val animationCmps: ComponentMapper<AnimationComponent> = world.mapper()
    private val imageCmps: ComponentMapper<ImageComponent> = world.mapper()
    private val physicCmps: ComponentMapper<PhysicComponent> = world.mapper()
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper()

    val location: Vector2
        get() = physicCmps[entity].body.position

    val target: Entity
        get() = aiCmps[entity].target

    fun inTargetRange(range: Float): Boolean {
        val aiCmp = aiCmps[entity]
        if (aiCmp.target == NO_TARGET) {
            return true
        }

        val physicCmp = physicCmps[entity]
        val targetPhysicCmp = physicCmps[aiCmp.target]
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

    fun inRange(range: Float, target: Vector2): Boolean {
        val physicCmp = physicCmps[entity]
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

    fun canAttack(extraRange: Float): Boolean {
        val aiCmp = aiCmps[entity]
        if (aiCmp.target == NO_TARGET) {
            return false
        }

        val attackCmp = attackCmps[entity]
        return attackCmp.isReady() && inTargetRange(extraRange)
    }

    fun findNearbyEnemy(): Boolean {
        with(aiCmps[entity]) {
            target = nearbyEntities.firstOrNull {
                it in playerCmps && !lifeCmps[it].isDead()
            } ?: NO_TARGET
            return target != NO_TARGET
        }
    }

    fun checkTargetStillNearby() {
        with(aiCmps[entity]) {
            if (target !in nearbyEntities) {
                target = NO_TARGET
            }
        }
    }

    fun attack() {
        with(attackCmps[entity]) {
            doAttack = true
            startAttack()
        }
        val x = physicCmps[entity].body.position.x
        val targetX = physicCmps[target].body.position.x
        imageCmps[entity].image.flipX = targetX < x
    }

    fun moveToTarget() {
        val aiCmp = aiCmps[entity]
        if (aiCmp.target == NO_TARGET) {
            with(moveCmps[entity]) {
                cos = 0f
                sin = 0f
            }
            return
        }

        val targetPhysicCmp = physicCmps[aiCmp.target]
        moveToLocation(targetPhysicCmp.body.position)
    }

    fun moveToLocation(target: Vector2) {
        val (targetX, targetY) = target
        val physicCmp = physicCmps[entity]
        val (sourceX, sourceY) = physicCmp.body.position
        with(moveCmps[entity]) {
            val angleRad = MathUtils.atan2(targetY - sourceY, targetX - sourceX)
            cos = MathUtils.cos(angleRad)
            sin = MathUtils.sin(angleRad)
        }
    }

    fun stopMovement() {
        with(moveCmps[entity]) {
            cos = 0f
            sin = 0f
        }
    }

    fun moveSlow(slowed: Boolean) {
        moveCmps[entity].slow = slowed
    }

    fun animation(
        type: AnimationType,
        mode: Animation.PlayMode = Animation.PlayMode.LOOP,
        resetAnimation: Boolean = false
    ) {
        with(animationCmps[entity]) {
            nextAnimation(type)
            this.mode = mode
            if (resetAnimation) {
                stateTime = 0f
            }
        }
    }

    fun isAnimationDone() = animationCmps[entity].isAnimationFinished()

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
) {
    lateinit var behaviorTree: BehaviorTree<AIEntity>
    var target: Entity = NO_TARGET

    companion object {
        val NO_TARGET = Entity(-1)

        class AIComponentListener(
            private val world: World,
            @Qualifier("GameStage") private val stage: Stage,
        ) : ComponentListener<AIComponent> {
            private val bTreeParser = BehaviorTreeParser<AIEntity>()

            override fun onComponentAdded(entity: Entity, component: AIComponent) {
                if (component.treePath.isNotBlank()) {
                    component.behaviorTree = bTreeParser.parse(
                        Gdx.files.internal(component.treePath),
                        AIEntity(entity, world, stage)
                    )
                }
            }

            override fun onComponentRemoved(entity: Entity, component: AIComponent) = Unit
        }
    }
}