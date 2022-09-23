package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

class LootComponent : Component<LootComponent> {
    var interactEntity: Entity? = null

    override fun type() = LootComponent

    companion object : ComponentType<LootComponent>()
}