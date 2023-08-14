package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.DialogComponent
import com.github.quillraven.mysticwoods.component.DialogId
import com.github.quillraven.mysticwoods.component.DisarmComponent
import com.github.quillraven.mysticwoods.component.MoveComponent
import com.github.quillraven.mysticwoods.dialog.Dialog
import com.github.quillraven.mysticwoods.dialog.dialog
import com.github.quillraven.mysticwoods.event.EntityDialogEvent
import com.github.quillraven.mysticwoods.event.fire
import ktx.app.gdxError

@AllOf([DialogComponent::class])
class DialogSystem(
    private val dialogCmps: ComponentMapper<DialogComponent>,
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val disarmCmps: ComponentMapper<DisarmComponent>,
    @Qualifier("GameStage") private val stage: Stage
) : IteratingSystem() {
    private val dialogCache = mutableMapOf<DialogId, Dialog>()

    override fun onTickEntity(entity: Entity) {
        with(dialogCmps[entity]) {
            val triggerEntity = interactEntity
            var dialog = currentDialog

            if (triggerEntity == null) {
                return
            } else if (dialog != null) {
                if (dialog.isComplete()) {
                    moveCmps.getOrNull(triggerEntity)?.let { it.root = false }
                    configureEntity(triggerEntity) { disarmCmps.remove(it) }
                    currentDialog = null
                    interactEntity = null
                }
                return
            }

            dialog = getDialog(dialogId).also { it.start() }
            currentDialog = dialog
            moveCmps.getOrNull(triggerEntity)?.let { it.root = true }
            configureEntity(triggerEntity) { disarmCmps.add(it) }

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