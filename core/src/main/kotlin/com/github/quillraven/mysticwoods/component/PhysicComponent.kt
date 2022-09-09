package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateCfg
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.system.CollisionSpawnSystem.Companion.SPAWN_AREA_SIZE
import ktx.app.gdxError
import ktx.box2d.BodyDefinition
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.loop
import ktx.math.vec2

class PhysicComponent(
    val impulse: Vector2 = vec2(),
    val size: Vector2 = vec2(),
    val offset: Vector2 = vec2(),
) {
    lateinit var body: Body
    val prevPos = vec2()

    companion object {
        private val TMP_VEC = vec2()

        fun EntityCreateCfg.physicCmpFromImage(
            world: World,
            image: Image,
            bodyType: BodyType,
            fixtureAction: BodyDefinition.(PhysicComponent, Float, Float) -> Unit
        ): PhysicComponent {
            val x = image.x
            val y = image.y
            val width = image.width
            val height = image.height

            return add {
                body = world.body(bodyType) {
                    position.set(x + width * 0.5f, y + height * 0.5f)
                    fixedRotation = true
                    allowSleep = false
                    this.fixtureAction(this@add, width, height)
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
                        body = world.body(StaticBody) {
                            position.set(bodyX, bodyY)
                            fixedRotation = true
                            allowSleep = false
                            loop(
                                vec2(0f, 0f),
                                vec2(bodyW, 0f),
                                vec2(bodyW, bodyH),
                                vec2(0f, bodyH),
                            )
                            TMP_VEC.set(bodyW * 0.5f, bodyH * 0.5f)
                            box(SPAWN_AREA_SIZE + 4f, SPAWN_AREA_SIZE + 4f, TMP_VEC) {
                                isSensor = true
                            }
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