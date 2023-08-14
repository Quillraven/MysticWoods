package com.github.quillraven.mysticwoods.component

import com.github.quillraven.fleks.Entity
import com.github.quillraven.mysticwoods.dialog.Dialog

enum class DialogId {
    NONE,
    BLOB
}

data class DialogComponent(
    var dialogId: DialogId = DialogId.NONE,
) {
    var interactEntity: Entity? = null
    var currentDialog: Dialog? = null
}