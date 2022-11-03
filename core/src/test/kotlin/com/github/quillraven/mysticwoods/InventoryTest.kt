package com.github.quillraven.mysticwoods

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.github.quillraven.mysticwoods.app.TestApplication
import com.github.quillraven.mysticwoods.screen.InventoryTestScreen

fun main() {
    Lwjgl3Application(TestApplication(::InventoryTestScreen), Lwjgl3ApplicationConfiguration().apply {
        setTitle("Test UI")
        setWindowedMode(1280, 720)
    })
}
