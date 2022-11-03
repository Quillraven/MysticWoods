package com.github.quillraven.mysticwoods.ui.model

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.component.InventoryComponent
import com.github.quillraven.mysticwoods.component.ItemComponent
import com.github.quillraven.mysticwoods.component.PlayerComponent
import com.github.quillraven.mysticwoods.event.EntityAddItemEvent
import ktx.log.logger

class InventoryModel(
    private val world: World,
    stage: Stage
) : PropertyChangeSource(), EventListener {

    private val playerEntities = world.family { all(PlayerComponent) }

    var playerItems by propertyNotify(listOf<ItemModel>())

    private val playerInventoryCmp: InventoryComponent
        get() = with(world) { return playerEntities.first()[InventoryComponent] }

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is EntityAddItemEvent -> with(world) {
                if (event.entity has PlayerComponent) {
                    playerItems = event.entity[InventoryComponent].items.map {
                        val itemCmp = it[ItemComponent]
                        ItemModel(
                            it.id,
                            itemCmp.itemType.category,
                            itemCmp.itemType.uiAtlasKey,
                            itemCmp.slotIdx,
                            itemCmp.equipped
                        )
                    }
                }
            }

            else -> return false
        }
        return true
    }

    private fun debugInventory() {
        if (Application.LOG_DEBUG != Gdx.app.logLevel) {
            return
        }

        log.debug { "\nInventory:" }
        playerInventoryCmp.items.forEach { item ->
            log.debug { "${with(world) { item[ItemComponent] }}" }
        }
        log.debug { "\n" }
    }

    private fun playerItemByModel(itemModel: ItemModel): ItemComponent = with(world) {
        return playerInventoryCmp.items.first { it.id == itemModel.itemEntityId }[ItemComponent]
    }

    fun equip(itemModel: ItemModel, equip: Boolean) {
        log.debug { "Equip $equip $itemModel" }
        playerItemByModel(itemModel).equipped = equip
        itemModel.equipped = equip
        debugInventory()
    }

    fun inventoryItem(slotIdx: Int, itemModel: ItemModel) {
        log.debug { "Set item at slot $slotIdx to $itemModel" }

        // update slot index of item
        // itemModel contains the old index while slotIdx contains the new index
        playerItemByModel(itemModel).slotIdx = slotIdx
        itemModel.slotIdx = slotIdx
        debugInventory()
    }

    companion object {
        private val log = logger<InventoryModel>()
    }
}