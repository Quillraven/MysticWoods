package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.mysticwoods.component.DialogComponent
import com.github.quillraven.mysticwoods.component.DialogId
import com.github.quillraven.mysticwoods.component.DisarmComponent
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.dialog.Dialog
import com.github.quillraven.mysticwoods.dialog.dialog
import com.github.quillraven.mysticwoods.event.EntityDialogEvent
import com.github.quillraven.mysticwoods.event.fire
import ktx.app.gdxError

class DialogSystem(
    private val stage: Stage = inject("GameStage")
) : IteratingSystem(family { all(DialogComponent) }) {
    private val dialogCache = mutableMapOf<DialogId, Dialog>()

    override fun onTickEntity(entity: Entity) {
        with(entity[DialogComponent]) {
            val triggerEntity = interactEntity
            var dialog = currentDialog

            if (triggerEntity == null) {
                return
            } else if (dialog != null) {
                if (dialog.isComplete()) {
                    triggerEntity.getOrNull(MoveComponent)?.let { it.root = false }
                    triggerEntity.configure { it -= DisarmComponent }
                    currentDialog = null
                    interactEntity = null
                }
                return
            }

            dialog = getDialog(dialogId).also { it.start() }
            currentDialog = dialog
            triggerEntity.getOrNull(MoveComponent)?.let { it.root = true }
            triggerEntity.configure { it += DisarmComponent }

            stage.fire(EntityDialogEvent(dialog))
        }
    }

    private fun getDialog(id: DialogId): Dialog {
        return dialogCache.getOrPut(id) {
            when (id) {
                DialogId.BLOB -> dialog(id.name) {
                    node(0, "Hello adventurer! Can you please take care of my crazy blue brothers?") {
                        option("But why?") {
                            action = { this@dialog.goToNode(1) }
                        }
                    }
                    node(1, "A dark magic has possessed them. There is no cure - KILL EM ALL!!!") {
                        option("Again?") {
                            action = { this@dialog.goToNode(0) }
                        }

                        option("Ok, ok") {
                            action = { this@dialog.end() }
                        }
                    }
                }

                else -> gdxError("No dialog configured for $id.")
            }
        }
    }
}