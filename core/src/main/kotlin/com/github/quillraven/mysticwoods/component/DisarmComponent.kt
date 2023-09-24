package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class DisarmComponent : Component<DisarmComponent> {
    override fun type() = DisarmComponent

    companion object : ComponentType<DisarmComponent>()
}