package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.github.quillraven.mysticwoods.component.InventoryComponent
import com.github.quillraven.mysticwoods.component.ItemType
import com.github.quillraven.mysticwoods.component.PlayerComponent
import com.github.quillraven.mysticwoods.ui.model.InventoryModel
import com.github.quillraven.mysticwoods.ui.view.InventoryView
import com.github.quillraven.mysticwoods.ui.view.inventoryView
import ktx.scene2d.actors

class InventoryTestScreen : TestScreen() {
    private val model = InventoryModel(eWorld, gameStage)
    private lateinit var overlay: InventoryView
    private val player = eWorld.entity {
        add<PlayerComponent>()
        add<InventoryComponent>()
    }
    private val inventoryCmps = eWorld.mapper<InventoryComponent>()

    override fun show() {
        super.show()
        uiStage.clear()
        uiStage.actors {
            overlay = inventoryView(model)
        }
        uiStage.isDebugAll = false
    }

    override fun render(delta: Float) {
        super.render(delta)
        when {
            Gdx.input.isKeyJustPressed(Keys.R) -> {
                hide()
                show()
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_1) -> {
                inventoryCmps[player].items.clear()
                overlay.clearInventoryAndGear()
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_2) -> {
                inventoryCmps[player].itemsToAdd += ItemType.SWORD
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_3) -> {
                inventoryCmps[player].itemsToAdd += ItemType.HELMET
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_4) -> {
                inventoryCmps[player].itemsToAdd += ItemType.ARMOR
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_5) -> {
                inventoryCmps[player].itemsToAdd += ItemType.BOOTS
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_6) -> {
                inventoryCmps[player].itemsToAdd += ItemType.BIG_SWORD
            }
        }
    }
}