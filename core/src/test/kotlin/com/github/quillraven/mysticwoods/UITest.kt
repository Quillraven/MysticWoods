package com.github.quillraven.mysticwoods

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.github.quillraven.mysticwoods.actor.FlipImageTest
import com.github.quillraven.mysticwoods.app.TestApplication

fun main() {
    Lwjgl3Application(TestApplication(::FlipImageTest), Lwjgl3ApplicationConfiguration().apply {
        setTitle("Test UI")
        setWindowedMode(1280, 720)
    })
}
