package com.github.quillraven.mysticwoods.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.github.quillraven.mysticwoods.ui.Drawables
import com.github.quillraven.mysticwoods.ui.Labels
import com.github.quillraven.mysticwoods.ui.get
import com.github.quillraven.mysticwoods.ui.model.InventoryModel
import com.github.quillraven.mysticwoods.ui.widget.InventorySlot
import com.github.quillraven.mysticwoods.ui.widget.inventorySlot
import ktx.scene2d.*

class InventoryView(
    private val model: InventoryModel,
    skin: Skin
) : KTable, Table(skin) {

    private val invSlots = mutableListOf<InventorySlot>()

    init {
        // UI
        setFillParent(true)
        val titlePadding = 15f

        table { outerTableCell ->
            background = skin[Drawables.FRAME_BGD]

            label(text = "Inventory", style = Labels.TITLE.skinKey, skin) {
                this.setAlignment(Align.center)
                it.expandX().fill()
                    .pad(8f, titlePadding, 0f, titlePadding)
                    .top()
                    .row()
            }

            table { invTableCell ->
                for (i in 1..18) {
                    this@InventoryView.invSlots += inventorySlot(null, skin) { slotCell ->
                        slotCell.padBottom(2f)
                        if (i % 6 == 0) {
                            slotCell.row()
                        } else {
                            slotCell.padRight(2f)
                        }
                    }
                }

                invTableCell.expand().fill()
            }

            outerTableCell.expand().width(150f).height(120f).left().center()
        }

        table { gearTableCell ->
            background = skin[Drawables.FRAME_BGD]

            label(text = "Gear", style = Labels.TITLE.skinKey, skin) {
                this.setAlignment(Align.center)
                it.expandX().fill()
                    .pad(8f, titlePadding, 0f, titlePadding)
                    .top()
                    .row()
            }

            table { invTableCell ->
                inventorySlot(Drawables.INVENTORY_SLOT_HELMET, skin) { it.padBottom(2f).colspan(2).row() }
                inventorySlot(Drawables.INVENTORY_SLOT_WEAPON, skin) { it.padBottom(2f).padRight(2f) }
                inventorySlot(Drawables.INVENTORY_SLOT_ARMOR, skin) { it.padBottom(2f).padRight(2f).row() }
                inventorySlot(Drawables.INVENTORY_SLOT_BOOTS, skin) { it.colspan(2).row() }
                invTableCell.expand().fill()
            }

            gearTableCell.expand().width(90f).height(120f).left().center()
        }

        // data binding
    }

    fun item(index: Int, itemAtlasKey: String?) {
        if (itemAtlasKey == null) {
            invSlots[index].item(null)
        } else {
            skin.getDrawable(itemAtlasKey)?.let { invSlots[index].item(it) }
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.inventoryView(
    model: InventoryModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: InventoryView.(S) -> Unit = {}
): InventoryView = actor(InventoryView(model, skin), init)