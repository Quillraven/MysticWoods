package com.github.quillraven.mysticwoods.dialog

import com.badlogic.gdx.utils.GdxRuntimeException
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

internal class DialogTest {

    private val testDialog = dialog("testDialog") {
        node(0, "nodeText") {
            option("optionText") {
                action = { this@dialog.end() }
            }
        }
    }

    @Test
    fun testEmptyDialog() {
        assertThrows<GdxRuntimeException> { dialog("testDialog") {} }
    }

    @Test
    fun testEmptyNode() {
        assertThrows<GdxRuntimeException> {
            dialog("testDialog") {
                node(0, "nodeText") {}
            }
        }
    }

    @Test
    fun testEmptyOption() {
        assertThrows<GdxRuntimeException> {
            dialog("testDialog") {
                node(0, "nodeText") {
                    option("optionText") {}
                }
            }
        }
    }

    @Test
    fun testDialogNotStarted() {
        val dialog = testDialog

        assertThrows<UninitializedPropertyAccessException> { dialog.currentNode }

        dialog.start()
        assertNotNull(dialog.currentNode)
    }

    @Test
    fun testDialogComplete() {
        val dialog = testDialog

        dialog.start()
        assertFalse(dialog.isComplete())

        dialog.triggerOption(0)
        assertTrue(dialog.isComplete())
    }

    @Test
    fun testDialogGoToNode() {
        lateinit var node0: Node
        lateinit var node1: Node
        val dialog = dialog("testDialog") {
            node0 = node(0, "node1") {
                option("next") {
                    action = { this@dialog.goToNode(1) }
                }
            }

            node1 = node(1, "node2") {
                option("back") {
                    action = { this@dialog.goToNode(0) }
                }
            }
        }

        dialog.start()
        assertEquals(node0, dialog.currentNode)

        dialog.triggerOption(0)
        assertEquals(node1, dialog.currentNode)

        dialog.triggerOption(0)
        assertEquals(node0, dialog.currentNode)
    }
}