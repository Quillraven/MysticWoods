package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.mysticwoods.actor.FlipImage
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.physicCmpFromImage
import com.github.quillraven.mysticwoods.screen.gdxError
import ktx.box2d.box
import ktx.log.logger

@AllOf([SpawnComponent::class])
class SpawnSystem(
    private val spawnCmps: ComponentMapper<SpawnComponent>,
    private val physicWorld: World,
) : IteratingSystem() {
    private val cachedCfgs = mutableMapOf<SpawnType, SpawnCfg>()

    init {
        world.entity {
            add<SpawnComponent> {
                type = SpawnType.PLAYER
                location.set(1f, 1f)
            }
        }
        world.entity {
            add<SpawnComponent> {
                type = SpawnType.SLIME
                location.set(4f, 1f)
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        with(spawnCmps[entity]) {
            LOG.debug { "Spawning entity of type $type" }
            val cfg = spawnCfg(type)
            world.entity {
                val imageCmp = add<ImageComponent> {
                    image = FlipImage().apply {
                        setScaling(Scaling.fill)
                        setPosition(location.x, location.y)
                        setSize(cfg.size.x, cfg.size.y)
                    }
                }

                add<AnimationComponent> {
                    nextAnimation(cfg.atlasKey, AnimationType.IDLE)
                }

                this.physicCmpFromImage(physicWorld, imageCmp.image) { width, height ->
                    box(width, height)
                }

                if (!cfg.speed.isZero) {
                    add<MoveComponent> { max.set(cfg.speed) }
                }

                if (type == SpawnType.PLAYER) {
                    add<PlayerComponent>()
                }
            }
        }

        world.remove(entity)
    }

    private fun spawnCfg(type: SpawnType): SpawnCfg = cachedCfgs.getOrPut(type) {
        when (type) {
            SpawnType.PLAYER -> SpawnCfg("player", size = Vector2(1.5f, 1.5f), speed = Vector2(5f, 5f))
            SpawnType.SLIME -> SpawnCfg("slime", size = Vector2(1f, 1f), speed = Vector2(2f, 2f))
            SpawnType.UNDEFINED -> gdxError("SpawnType must be specified")
        }
    }

    companion object {
        private val LOG = logger<SpawnSystem>()
    }
}