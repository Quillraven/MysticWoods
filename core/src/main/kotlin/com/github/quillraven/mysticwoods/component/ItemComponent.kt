package com.github.quillraven.mysticwoods.component

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
    BOOTS(ItemCategory.BOOTS, "boots"),
    ARMOR(ItemCategory.ARMOR, "armor"),
}

data class ItemComponent(
    var itemType: ItemType = ItemType.UNDEFINED,
)