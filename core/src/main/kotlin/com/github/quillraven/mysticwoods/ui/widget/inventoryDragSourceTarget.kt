package com.github.quillraven.mysticwoods.ui.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source
import com.github.quillraven.mysticwoods.component.ItemCategory
import com.github.quillraven.mysticwoods.ui.model.ItemModel

class InventoryDragSource(
    val inventorySlot: InventorySlot
) : Source(inventorySlot) {

    val isGear: Boolean
        get() = inventorySlot.isGear

    val supportedCategory: ItemCategory
        get() = inventorySlot.supportedCategory

    override fun dragStart(
        event: InputEvent,
        x: Float,
        y: Float,
        pointer: Int
    ): Payload? {
        if (inventorySlot.itemModel == null) {
            // no item in slot -> return payload of null to ignore drag&drop
            return null
        }

        return Payload().apply {
            `object` = inventorySlot.itemModel
            dragActor = Image(inventorySlot.itemDrawable).apply {
                setSize(DRAG_ACTOR_SIZE, DRAG_ACTOR_SIZE)
            }
            // hide item in source slot during drag & drop
            inventorySlot.item(null)
        }
    }

    override fun dragStop(
        event: InputEvent,
        x: Float,
        y: Float,
        pointer: Int,
        payload: Payload,
        target: DragAndDrop.Target?
    ) {
        if (target == null) {
            // drop on invalid or no target -> reset item on source slot
            inventorySlot.item(payload.`object` as ItemModel)
        }
    }

    companion object {
        const val DRAG_ACTOR_SIZE = 20f
    }
}

class InventoryDragTarget(
    private val inventorySlot: InventorySlot,
    private val onDrop: (sourceSlot: InventorySlot, targetSlot: InventorySlot, itemModel: ItemModel) -> Unit,
    private val supportedItemCategory: ItemCategory? = null
) : DragAndDrop.Target(inventorySlot) {

    private val isGear: Boolean
        get() = supportedItemCategory != null

    private fun isSupported(category: ItemCategory) = supportedItemCategory == category

    override fun drag(source: Source, payload: Payload, x: Float, y: Float, pointer: Int): Boolean {
        val itemModel = payload.`object` as ItemModel
        val dragSource = source as InventoryDragSource
        val srcCategory = dragSource.supportedCategory

        return if (isGear && isSupported(itemModel.category)) {
            // gear slot target that allows the item category of the dragged item
            true
        } else if (!isGear && dragSource.isGear && (inventorySlot.isEmpty || inventorySlot.itemCategory == srcCategory)) {
            // inventory slot target and item gets dragged from gear slot
            // -> only allowed if inventory slot is empty or contains an item of supported category
            true
        } else if (!isGear && !dragSource.isGear) {
            // drag and drop between inventory slots
            true
        } else {
            payload.dragActor.color = Color.RED
            false
        }
    }

    override fun reset(source: Source, payload: Payload) {
        payload.dragActor.color = Color.WHITE
    }

    override fun drop(source: Source, payload: Payload, x: Float, y: Float, pointer: Int) {
        onDrop(
            (source as InventoryDragSource).inventorySlot,
            actor as InventorySlot,
            payload.`object` as ItemModel
        )
    }
}