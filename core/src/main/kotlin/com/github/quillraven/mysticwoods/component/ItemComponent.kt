package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class ItemCategory {
    UNDEFINED,
    HELMET,
    ARMOR,
    WEAPON,
    BOOTS,
    CONSUMABLE,
}

enum class ItemType(
    val category: ItemCategory,
    val uiAtlasKey: String,
) {
    UNDEFINED(ItemCategory.UNDEFINED, ""),
    HELMET(ItemCategory.HELMET, "helmet"),
    SWORD(ItemCategory.WEAPON, "sword"),
    BIG_SWORD(ItemCategory.WEAPON, "sword2"),
    BOOTS(ItemCategory.BOOTS, "boots"),
    ARMOR(ItemCategory.ARMOR, "armor"),
}

data class ItemComponent(
    var itemType: ItemType = ItemType.UNDEFINED,
    var slotIdx: Int = -1,
    var equipped: Boolean = false,
) : Component<ItemComponent> {
    override fun type() = ItemComponent

    companion object : ComponentType<ItemComponent>()
}
