package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.mysticwoods.component.InventoryComponent
import com.github.quillraven.mysticwoods.component.ItemComponent
import com.github.quillraven.mysticwoods.event.EntityAddItemEvent
import com.github.quillraven.mysticwoods.event.fire

class InventorySystem(
    private val gameStage: Stage = World.inject("GameStage"),
) : IteratingSystem(family = family { all(InventoryComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val inventory = entity[InventoryComponent]
        if (inventory.itemsToAdd.isEmpty()) {
            return
        }

        inventory.itemsToAdd.forEach { itemType ->
            val slotIdx: Int = emptySlotIndex(inventory)
            if (slotIdx == -1) {
                // inventory is full -> cannot add more items
                return
            }

            val newItem = world.entity { it += ItemComponent(itemType, slotIdx) }
            inventory.items += newItem
            gameStage.fire(EntityAddItemEvent(entity, newItem))
        }
        inventory.itemsToAdd.clear()
    }

    private fun emptySlotIndex(inventory: InventoryComponent): Int {
        for (i in 0 until InventoryComponent.INVENTORY_CAPACITY) {
            if (inventory.items.none { it[ItemComponent].slotIdx == i }) {
                return i
            }
        }

        return -1
    }
}