package com.github.quillraven.mysticwoods.component

import com.github.quillraven.mysticwoods.system.CollisionDespawnSystem
import com.github.quillraven.mysticwoods.system.CollisionSpawnSystem

/**
 * This component identifies entities that should be part of object
 * collisions of the game world.
 * Entities with such a component will spawn collision objects based on
 * the TiledMap around them.
 *
 * For more details refer to [CollisionSpawnSystem] and [CollisionDespawnSystem].
 */
class CollisionComponent