package com.github.quillraven.mysticwoods.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.github.quillraven.mysticwoods.component.ItemCategory
import com.github.quillraven.mysticwoods.ui.model.InventoryModel
import com.github.quillraven.mysticwoods.ui.model.UiItemModel
import com.github.quillraven.mysticwoods.ui.view.InventoryView
import com.github.quillraven.mysticwoods.ui.view.inventoryView
import ktx.scene2d.actors

class InventoryTestScreen : TestScreen() {
    private val model = InventoryModel(eWorld, uiStage)
    private lateinit var overlay: InventoryView

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
                overlay.item(4, UiItemModel(ItemCategory.WEAPON, "sword"))
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_2) -> {
                overlay.item(4, null)
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_3) -> {
                overlay.item(0, UiItemModel(ItemCategory.HELMET, "helmet"))
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_4) -> {
                overlay.item(2, UiItemModel(ItemCategory.ARMOR, "armor"))
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_5) -> {
                overlay.item(17, UiItemModel(ItemCategory.BOOTS, "boots"))
            }
        }
    }
}