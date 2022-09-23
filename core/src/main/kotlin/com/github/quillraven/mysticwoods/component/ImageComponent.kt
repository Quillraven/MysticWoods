package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentHook
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.mysticwoods.actor.FlipImage

class ImageComponent : Component<ImageComponent>, Comparable<ImageComponent> {
    lateinit var image: FlipImage
    var layer = 0

    override fun type() = ImageComponent

    override fun compareTo(other: ImageComponent): Int {
        val layerDiff = layer.compareTo(other.layer)
        return if (layerDiff != 0) {
            layerDiff
        } else {
            val yDiff = other.image.y.compareTo(image.y)
            if (yDiff != 0) {
                yDiff
            } else {
                other.image.x.compareTo(image.x)
            }
        }
    }

    companion object : ComponentType<ImageComponent>() {
        val onImageAdd: ComponentHook<ImageComponent> = { _, component ->
            inject<Stage>("GameStage").addActor(component.image)

        }

        val onImageRemove: ComponentHook<ImageComponent> = { _, component ->
            inject<Stage>("GameStage").root.removeActor(component.image)
        }
    }
}