package com.github.quillraven.mysticwoods.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.assets.disposeSafely
import ktx.scene2d.Scene2DSkin
import ktx.style.label
import ktx.style.set
import ktx.style.skin

enum class Drawables(
    val atlasKey: String,
) {
    CHAR_INFO_BGD("char_info"),
    PLAYER("player"),
    SLIME("slime"),
    LIFE_BAR("life_bar"),
    MANA_BAR("mana_bar"),
    FRAME_BGD("frame_bgd"),
    FRAME_FGD("frame_fgd"),
    INVENTORY_SLOT("inv_slot"),
    INVENTORY_SLOT_HELMET("inv_slot_helmet"),
    INVENTORY_SLOT_ARMOR("inv_slot_armor"),
    INVENTORY_SLOT_WEAPON("inv_slot_weapon"),
    INVENTORY_SLOT_BOOTS("inv_slot_boots"),
}

operator fun Skin.get(drawable: Drawables): Drawable = this.getDrawable(drawable.atlasKey)

enum class Fonts(
    val atlasRegionKey: String,
    val scaling: Float
) {
    DEFAULT("fnt_white", 0.25f),
    BIG("fnt_white", 0.5f);

    val skinKey = "Font_${this.name.lowercase()}"
    val fontPath = "ui/${this.atlasRegionKey}.fnt"
}

operator fun Skin.get(font: Fonts): BitmapFont = this.getFont(font.skinKey)

enum class Labels {
    FRAME,
    TITLE;

    val skinKey = this.name.lowercase()
}

fun loadSkin() {
    Scene2DSkin.defaultSkin = skin(TextureAtlas("ui/ui.atlas")) { skin ->
        Fonts.values().forEach { fnt ->
            skin[fnt.skinKey] =
                BitmapFont(Gdx.files.internal(fnt.fontPath), skin.getRegion(fnt.atlasRegionKey)).apply {
                    data.markupEnabled = true
                    data.setScale(fnt.scaling)
                }
        }

        label(Labels.FRAME.skinKey) {
            font = skin[Fonts.DEFAULT]
            background = skin[Drawables.FRAME_FGD].apply {
                leftWidth = 2f
                rightWidth = 2f
                topHeight = 1f
            }
        }
        label(Labels.TITLE.skinKey) {
            font = skin[Fonts.BIG]
            fontColor = Color.SLATE
            background = skin[Drawables.FRAME_FGD].apply {
                leftWidth = 2f
                rightWidth = 2f
                topHeight = 1f
            }
        }
    }
}

fun disposeSkin() = Scene2DSkin.defaultSkin.disposeSafely()