package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateCfg
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.system.CollisionSpawnSystem.Companion.SPAWN_AREA_SIZE
import com.github.quillraven.mysticwoods.system.EntitySpawnSystem
import ktx.app.gdxError
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
        private val COLLISION_OFFSET = vec2()

        fun PhysicComponent.bodyFromImageAndCfg(world: World, image: Image, cfg: SpawnCfg): Body {
            val x = image.x
            val y = image.y
            val width = image.width
            val height = image.height
            val bodyType = cfg.bodyType
            val physicScaling = cfg.scalePhysic
            val categoryBit = cfg.categoryBit
            val cmp = this

            return world.body(bodyType) {
                position.set(x + width * 0.5f, y + height * 0.5f)
                cmp.prevPos.set(position)
                fixedRotation = true
                allowSleep = false

                val w = width * physicScaling.x
                val h = height * physicScaling.y
                cmp.size.set(w, h)
                cmp.offset.set(cfg.physicOffset)

                // hit box
                box(w, h, cmp.offset) {
                    isSensor = bodyType != StaticBody
                    userData = EntitySpawnSystem.HIT_BOX_SENSOR
                    filter.categoryBits = categoryBit
                }

                if (bodyType != StaticBody) {
                    // collision box
                    val collH = h * 0.4f
                    COLLISION_OFFSET.set(cmp.offset)
                    COLLISION_OFFSET.y -= h * 0.5f - collH * 0.5f
                    box(w, collH, COLLISION_OFFSET) { filter.categoryBits = categoryBit }
                }
            }
        }

        fun EntityCreateCfg.physicCmpFromShape2D(
            world: World,
            x: Int,
            y: Int,
            shape: Shape2D,
            isPortal: Boolean = false,
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
                            ) {
                                filter.categoryBits = LightComponent.b2dEnvironment
                                this.isSensor = isPortal
                            }

                            if (!isPortal) {
                                TMP_VEC.set(bodyW * 0.5f, bodyH * 0.5f)
                                box(SPAWN_AREA_SIZE + 4f, SPAWN_AREA_SIZE + 4f, TMP_VEC) {
                                    this.isSensor = true
                                }
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