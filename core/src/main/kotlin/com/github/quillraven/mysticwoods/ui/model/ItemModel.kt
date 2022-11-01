package com.github.quillraven.mysticwoods.ui.model

import com.github.quillraven.mysticwoods.component.ItemCategory

data class ItemModel(
    val itemEntityId: Int,
    val category: ItemCategory,
    val atlasKey: String,
    var slotIdx: Int,
    var equipped: Boolean
)