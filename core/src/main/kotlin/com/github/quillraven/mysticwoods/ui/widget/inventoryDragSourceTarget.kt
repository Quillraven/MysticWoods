package com.github.quillraven.mysticwoods.ui.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source
import com.github.quillraven.mysticwoods.component.ItemCategory
import com.github.quillraven.mysticwoods.ui.model.UiItemModel

class InventoryDragSource(
    val inventorySlot: InventorySlot
) : Source(inventorySlot) {
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
            inventorySlot.item(payload.`object` as UiItemModel)
        }
    }

    companion object {
        const val DRAG_ACTOR_SIZE = 20f
    }
}

class InventoryDragTarget(
    inventorySlot: InventorySlot,
    private val onDrop: (sourceSlot: InventorySlot, targetSlot: InventorySlot, itemModel: UiItemModel) -> Unit,
    private val supportedItemCategory: ItemCategory? = null
) : DragAndDrop.Target(inventorySlot) {
    override fun drag(source: Source, payload: Payload, x: Float, y: Float, pointer: Int): Boolean {
        if (supportedItemCategory == null) {
            // no check necessary -> drop allowed
            return true
        }

        val itemModel = payload.`object` as UiItemModel
        return if (itemModel.category == supportedItemCategory) {
            true
        } else {
            payload.dragActor.color = Color.RED
            false
        }
    }

    override fun reset(source: Source, payload: Payload) {
        if (supportedItemCategory == null) {
            // nothing to do since nothing happens in drag function
            return
        }

        payload.dragActor.color = Color.WHITE
    }

    override fun drop(source: Source, payload: Payload, x: Float, y: Float, pointer: Int) {
        onDrop(
            (source as InventoryDragSource).inventorySlot,
            actor as InventorySlot,
            payload.`object` as UiItemModel
        )
    }
}