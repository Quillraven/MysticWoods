package com.github.quillraven.mysticwoods.dialog

import ktx.app.gdxError

/**
 * This is just a "quick&dirt" draft of a conversation/dialog DSL to
 * demonstrate the concept in the YouTube tutorial series.
 * One downside of this approach is that you have the normal class functions
 * and properties available inside the 'cfg' lambdas. One solution is to have
 * separate classes for the configuration and for the real model.
 * I used this approach in Fleks ECS for the WorldConfiguration.
 * But to keep it simple for the tutorial, we stick to this approach.
 */

fun dialog(id: String, cfg: Dialog.() -> Unit): Dialog {
    return Dialog(id).apply {
        this.cfg()

        if (this.hasNoNodes()) {
            gdxError("Dialog '$id' has no nodes. At least one node is required.")
        }
    }
}

@DslMarker
annotation class DialogDslMarker

@DialogDslMarker
data class Dialog(
    val id: String,
    private val nodes: MutableList<Node> = mutableListOf(),
    private var complete: Boolean = false
) {
    lateinit var currentNode: Node

    fun hasNoNodes(): Boolean = nodes.isEmpty()

    fun node(id: Int, text: String, cfg: Node.() -> Unit): Node {
        return Node(id, text).apply {
            this.cfg()
            if (this.hasNoOptions()) {
                gdxError("Dialog node '$id' has no options. At least one option is required.")
            }

            this@Dialog.nodes += this
        }
    }

    fun start() {
        complete = false
        currentNode = nodes.first()
    }

    fun end() {
        complete = true
    }

    fun goToNode(nodeId: Int) {
        currentNode = nodes.firstOrNull { it.id == nodeId }
            ?: gdxError("There is no node with id '${nodeId}' in dialog '$this'.")
    }

    fun isComplete(): Boolean = complete

    fun triggerOption(optionIdx: Int) {
        val option = currentNode[optionIdx]
            ?: gdxError("There is no option with idx '$optionIdx' in node `$currentNode`.")

        option.action()
    }
}

@DialogDslMarker
data class Node(
    val id: Int,
    val text: String,
) {
    var options: MutableList<Option> = mutableListOf()
        private set

    fun hasNoOptions(): Boolean = options.isEmpty()

    fun option(text: String, cfg: Option.() -> Unit) {
        options += Option(options.size, text).apply {
            this.cfg()
            if (!this.hasAction()) {
                gdxError("Option '$text' of node '${this@Node.id}' has no action.")
            }
        }
    }

    operator fun get(optionIdx: Int): Option? = options.getOrNull(optionIdx)
}

@DialogDslMarker
data class Option(
    val idx: Int,
    val text: String,
    var action: () -> Unit = noAction,
) {

    fun hasAction(): Boolean = action != noAction

    companion object {
        private val noAction: () -> Unit = {}
    }
}
