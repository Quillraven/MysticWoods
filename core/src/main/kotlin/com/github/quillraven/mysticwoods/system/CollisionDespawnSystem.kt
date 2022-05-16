package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.TiledComponent
import com.github.quillraven.mysticwoods.event.CollisionDespawnEvent
import com.github.quillraven.mysticwoods.event.fire

@AllOf([TiledComponent::class])
class CollisionDespawnSystem(
    @Qualifier("GameStage") private val stage: Stage,
    private val tiledCmps: ComponentMapper<TiledComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        // for existing collision tiled entities we check if there are no nearby entities anymore
        // and remove them in that case
        if (tiledCmps[entity].nearbyEntities.isEmpty()) {
            stage.fire(CollisionDespawnEvent(tiledCmps[entity].cell))
            world.remove(entity)
        }
    }
}