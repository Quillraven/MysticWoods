package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.mysticwoods.dialog.Dialog

enum class DialogId {
    NONE,
    BLOB
}

data class DialogComponent(val dialogId: DialogId) : Component<DialogComponent> {
    var interactEntity: Entity? = null
    var currentDialog: Dialog? = null

    override fun type() = DialogComponent

    companion object : ComponentType<DialogComponent>()
}