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
        it += PlayerComponent()
        it += InventoryComponent()
    }

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
                with(eWorld) { player[InventoryComponent].items.clear() }
                overlay.clearInventoryAndGear()
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_2) -> {
                with(eWorld) { player[InventoryComponent].itemsToAdd += ItemType.SWORD }
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_3) -> {
                with(eWorld) { player[InventoryComponent].itemsToAdd += ItemType.HELMET }
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_4) -> {
                with(eWorld) { player[InventoryComponent].itemsToAdd += ItemType.ARMOR }
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_5) -> {
                with(eWorld) { player[InventoryComponent].itemsToAdd += ItemType.BOOTS }
            }

            Gdx.input.isKeyJustPressed(Keys.NUM_6) -> {
                with(eWorld) { player[InventoryComponent].itemsToAdd += ItemType.BIG_SWORD }
            }
        }
    }
}