package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag

class InventoryComponent : Component<InventoryComponent> {
    val items = MutableEntityBag(INVENTORY_CAPACITY)
    val itemsToAdd = mutableListOf<ItemType>()

    override fun type() = InventoryComponent

    companion object : ComponentType<InventoryComponent>() {
        const val INVENTORY_CAPACITY = 18
    }
}
