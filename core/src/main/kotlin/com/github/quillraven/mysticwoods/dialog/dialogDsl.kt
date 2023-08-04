package com.github.quillraven.mysticwoods.dialog

import ktx.app.gdxError

fun main() {
    val introDialog = dialog("intro") {
        node(0, "i18n.txt") {
            imageId = "image"
            option("txt") {
                targetNode = 1
            }
        }

        node(1, "id") {
            option("ok") {
                action = DialogAction.END
            }
            option("repeat") {
                targetNode = 0
            }
        }
    }

    introDialog.start()
    println(introDialog)
    println(introDialog.currentNode)
    println(introDialog.currentNode.options)

    println("\ntrigger next on first node")
    introDialog.triggerOption(0)
    println(introDialog.complete)
    println(introDialog.currentNode)

    println("\ntrigger repeat on second node")
    introDialog.triggerOption(1)
    println(introDialog.complete)
    println(introDialog.currentNode)

    println("\ntrigger next on first node")
    introDialog.triggerOption(0)
    println(introDialog.complete)
    println(introDialog.currentNode)

    println("\ntrigger ok on second node")
    introDialog.triggerOption(0)
    println(introDialog.complete)
    println(introDialog.currentNode)
}

fun dialog(id: String, cfg: Dialog.() -> Unit): Dialog {
    return Dialog(id).apply {
        this.cfg()

        if (nodes.isEmpty()) {
            gdxError("Dialog '$id' has no nodes. At least one node is required.")
        }
    }
}

@DslMarker
annotation class DialogDslMarker

@DialogDslMarker
data class Dialog(val id: String, val nodes: MutableList<Node> = mutableListOf(), var complete: Boolean = false) {
    lateinit var currentNode: Node

    fun node(id: Int, textId: String, cfg: Node.() -> Unit) {
        nodes += Node(id, textId).apply {
            this.cfg()
            if (options.isEmpty()) {
                gdxError("Dialog node '$id' has no options. At least one option is required.")
            }
        }
    }

    fun start() {
        complete = false
        currentNode = nodes.first()
    }

    fun triggerOption(optionId: Int) {
        val option = currentNode.options.getOrNull(optionId)
            ?: gdxError("There is no option with idx '$optionId' in node `$currentNode`.")

        if (option.action == DialogAction.END) {
            complete = true
        }

        if (option.hasTargetNode()) {
            currentNode = nodes.firstOrNull { it.id == option.targetNode }
                ?: gdxError("There is no node with id '${option.targetNode}' in dialog $this.")
        }
    }
}

@DialogDslMarker
data class Node(
    val id: Int,
    val textId: String,
    var imageId: String = "",
    var options: MutableList<Option> = mutableListOf()
) {
    fun option(textId: String, cfg: Option.() -> Unit) {
        options += Option(textId).apply {
            this.cfg()
            if (targetNode == -1 && action == DialogAction.NONE) {
                gdxError("Option '$textId' of node '${this@Node.id}' has no targetNode and no action.")
            }
        }
    }
}

enum class DialogAction {
    NONE,
    END
}

@DialogDslMarker
data class Option(
    val textId: String,
    var targetNode: Int = -1,
    var action: DialogAction = DialogAction.NONE
) {
    fun hasTargetNode(): Boolean = targetNode != -1
}