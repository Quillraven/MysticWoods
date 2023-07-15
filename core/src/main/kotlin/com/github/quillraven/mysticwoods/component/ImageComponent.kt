package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.mysticwoods.actor.FlipImage

class ImageComponent : Component<ImageComponent>, Comparable<ImageComponent> {
    lateinit var image: FlipImage
    var layer = 0

    override fun type() = ImageComponent

    override fun World.onAddComponent(entity: Entity) {
        this.inject<Stage>("GameStage").addActor(image)
    }

    override fun World.onRemoveComponent(entity: Entity) {
        this.inject<Stage>("GameStage").root.removeActor(image)
    }

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

    companion object : ComponentType<ImageComponent>()
}