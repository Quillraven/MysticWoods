package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.MysticWoods.Companion.UNIT_SCALE
import com.github.quillraven.mysticwoods.component.*
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.bodyFromImageAndCfg
import com.github.quillraven.mysticwoods.component.PhysicComponent.Companion.physicCmpFromShape2D
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import com.github.quillraven.mysticwoods.event.fire
import com.github.quillraven.mysticwoods.system.EntitySpawnSystem.Companion.PLAYER_CFG
import ktx.app.gdxError
import ktx.assets.disposeSafely
import ktx.log.logger
import ktx.tiled.*

@AllOf([PortalComponent::class])
class PortalSystem(
    private val physicWorld: World,
    @Qualifier("GameStage") private val gameStage: Stage,
    private val portalCmps: ComponentMapper<PortalComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val lightCmps: ComponentMapper<LightComponent>,
) : IteratingSystem(), EventListener {

    private var currentMap: TiledMap? = null

    override fun onTickEntity(entity: Entity) {
        val portalCmp = portalCmps[entity]
        val (id, toMap, toPortal, triggerEntities) = portalCmp

        if (triggerEntities.isNotEmpty()) {
            val triggerEntity = triggerEntities.first()
            triggerEntities.clear()

            log.debug { "Entity $triggerEntity entered portal $id" }
            setMap("maps/$toMap.tmx", toPortal)
        }
    }

    fun setMap(path: String, targetPortalID: Int = -1) {
        currentMap?.disposeSafely()

        // in a real game you might want to remember which entities are left in a specific map
        // -> e.g. store their TiledIDs in a preference
        // for now, we keep it simple and just remove all non-player units.
        world.family(noneOf = arrayOf(PlayerComponent::class)).forEach { world.remove(it) }

        val newMap = TmxMapLoader().load(path)
        currentMap = newMap

        if (targetPortalID >= 0) {
            // teleport player to target
            world.family(allOf = arrayOf(PlayerComponent::class)).forEach { playerEntity ->
                val targetPortal = getTargetPortalObj(newMap, targetPortalID)
                val image = imageCmps[playerEntity].image
                image.setPosition(
                    targetPortal.x * UNIT_SCALE - image.width * 0.5f + targetPortal.width * 0.5f * UNIT_SCALE,
                    targetPortal.y * UNIT_SCALE - targetPortal.height * 0.5f * UNIT_SCALE
                )

                configureEntity(playerEntity) {
                    physicCmps.remove(it)
                    physicCmps.add(it) {
                        body = bodyFromImageAndCfg(physicWorld, image, PLAYER_CFG)
                    }
                }

                lightCmps[playerEntity].light.attachToBody(physicCmps[playerEntity].body)
            }
        }

        gameStage.fire(MapChangeEvent(newMap))
    }

    private fun getTargetPortalObj(map: TiledMap, portalID: Int): MapObject {
        return map.layer("portals").objects.firstOrNull { it.id == portalID }
            ?: gdxError("There is no portal with id $portalID")
    }

    override fun handle(event: Event): Boolean {
        if (event is MapChangeEvent) {
            val portalLayer = event.map.layer("portals")
            portalLayer.objects.forEach { mapObj ->
                val toMap = mapObj.property("toMap", "")
                val toPortal = mapObj.property("toPortal", -1)

                if (toMap.isBlank()) {
                    // object is a portal target -> nothing to do
                    return@forEach
                } else if (toPortal == -1) {
                    gdxError("Portal ${mapObj.id} does not have a 'toPortal' property")
                }

                // spawn portal
                log.debug { "Spawning portal ${mapObj.id}: toMap=$toMap, toPortal=$toPortal" }
                world.entity {
                    physicCmpFromShape2D(physicWorld, 0, 0, mapObj.shape, true)
                    add<PortalComponent> {
                        this.id = mapObj.id
                        this.toMap = toMap
                        this.toPortal = toPortal
                    }
                }
            }
            return true
        }
        return false
    }

    override fun onDispose() {
        currentMap?.disposeSafely()
    }

    companion object {
        private val log = logger<PortalSystem>()
    }

}