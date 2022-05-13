package com.github.quillraven.mysticwoods.actor

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable

/**
 * [Image] does not support flipping of graphics which we need for the MysticWoods assets
 * because we need to flip the graphics to make the characters look left or right.
 *
 * The [draw] function is the same as the one of [Image] but with support to draw a flipped version of the drawable.
 */
class FlipImage : Image() {
    var flipX = false

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        val toDraw = drawable
        if (toDraw is TransformDrawable && (scaleX != 1f || scaleY != 1f || rotation != 0f)) {
            toDraw.draw(
                batch,
                if (flipX) x + imageX + imageWidth * scaleX else x + imageX,
                y + imageY,
                originX - imageX,
                originY - imageY,
                imageWidth,
                imageHeight,
                if (flipX) -scaleX else scaleX,
                scaleY,
                rotation
            )
        } else {
            toDraw?.draw(
                batch,
                if (flipX) x + imageX + imageWidth * scaleX else x + imageX,
                y + imageY,
                if (flipX) -imageWidth * scaleX else imageWidth * scaleX,
                imageHeight * scaleY
            )
        }
    }
}