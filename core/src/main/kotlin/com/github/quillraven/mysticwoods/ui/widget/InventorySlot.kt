package com.github.quillraven.mysticwoods.ui.widget

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.mysticwoods.ui.Drawables
import com.github.quillraven.mysticwoods.ui.get
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.scene2d.*

@Scene2dDsl
class InventorySlot(
    slotItemBgd: Drawables?,
    private val skin: Skin
) : WidgetGroup(), KGroup {

    private val background = Image(skin[Drawables.INVENTORY_SLOT])
    private val slotItemInfo: Image? = if (slotItemBgd == null) null else Image(skin[slotItemBgd])
    private val item = Image()

    init {
        this += background
        slotItemInfo?.let { info ->
            this += info.apply {
                alpha = 0.75f
                setPosition(3f, 3f)
                setSize(14f, 14f)
                setScaling(Scaling.contain)
            }
        }
        this += item.apply {
            setPosition(3f, 3f)
            setSize(14f, 14f)
            setScaling(Scaling.contain)
        }
    }

    override fun getPrefWidth() = background.drawable.minWidth

    override fun getPrefHeight() = background.drawable.minHeight

    fun item(itemDrawable: Drawable?) {
        if (itemDrawable == null) {
            item.drawable = null
        } else {
            item.drawable = itemDrawable
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.inventorySlot(
    slotItemBgd: Drawables?,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: InventorySlot.(S) -> Unit = {}
): InventorySlot = actor(InventorySlot(slotItemBgd, skin), init)