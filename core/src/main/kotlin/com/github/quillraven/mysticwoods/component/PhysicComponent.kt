package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateCfg
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.screen.gdxError
import com.github.quillraven.mysticwoods.system.CollisionSpawnSystem.Companion.SPAWN_AREA_SIZE
import ktx.box2d.BodyDefinition
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.math.vec2

class PhysicComponent(
    val impulse: Vector2 = vec2()
) {
    lateinit var body: Body
    val prevPos = vec2()

    companion object {
        fun EntityCreateCfg.physicCmpFromImage(
            world: World,
            image: Image,
            fixtureAction: BodyDefinition.(Float, Float) -> Unit
        ): PhysicComponent {
            val x = image.x
            val y = image.y
            val width = image.width
            val height = image.height

            return add {
                body = world.body(BodyDef.BodyType.DynamicBody) {
                    position.set(x + width * 0.5f, y + height * 0.5f)
                    fixedRotation = true
                    allowSleep = false
                    this.fixtureAction(width, height)
                }
                prevPos.set(body.position)
            }
        }

        fun EntityCreateCfg.physicCmpFromShape2D(
            world: World,
            x: Int,
            y: Int,
            shape: Shape2D
        ): PhysicComponent {
            when (shape) {
                is Rectangle -> {
                    val bodyX = x + shape.x * UNIT_SCALE
                    val bodyY = y + shape.y * UNIT_SCALE
                    val bodyW = shape.width * UNIT_SCALE
                    val bodyH = shape.height * UNIT_SCALE

                    return add {
                        body = world.body(BodyDef.BodyType.StaticBody) {
                            position.set(
                                bodyX + bodyW * 0.5f,
                                bodyY + bodyH * 0.5f
                            )
                            fixedRotation = true
                            allowSleep = false
                            box(bodyW, bodyH)
                            circle(SPAWN_AREA_SIZE + 2f) { isSensor = true }
                        }
                    }
                }
                else -> gdxError("Shape $shape not supported")
            }
        }

        class PhysicComponentListener : ComponentListener<PhysicComponent> {
            override fun onComponentAdded(entity: Entity, component: PhysicComponent) {
                component.body.userData = entity
            }

            override fun onComponentRemoved(entity: Entity, component: PhysicComponent) {
                val body = component.body
                body.world.destroyBody(body)
                body.userData = null
            }
        }
    }
}