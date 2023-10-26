package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class PortalComponent(
    var id: Int = -1,
    var toMap: String = "",
    var toPortal: Int = 0,
    var triggerEntities: MutableSet<Entity> = hashSetOf(),
) : Component<PortalComponent> {
    override fun type() = PortalComponent

    companion object : ComponentType<PortalComponent>()
}