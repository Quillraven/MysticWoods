package com.github.quillraven.mysticwoods.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.mysticwoods.event.EntityAttackEvent
import com.github.quillraven.mysticwoods.event.EntityDeathEvent
import com.github.quillraven.mysticwoods.event.MapChangeEvent
import ktx.assets.disposeSafely
import ktx.log.logger
import ktx.tiled.propertyOrNull

class AudioSystem : EventListener, IntervalSystem() {
    private val soundCache = mutableMapOf<String, Sound>()
    private val musicCache = mutableMapOf<String, Music>()
    private val soundRequests = mutableMapOf<String, Sound>()

    override fun onTick() {
        if (soundRequests.isEmpty()) {
            return
        }

        soundRequests.values.forEach { it.play(1f) }
        soundRequests.clear()
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is MapChangeEvent -> {
                event.map.propertyOrNull<String>("music")?.let { path ->
                    log.debug { "Changing music to '$path'" }
                    val music = musicCache.getOrPut(path) {
                        Gdx.audio.newMusic(Gdx.files.internal("audio/$path")).apply {
                            isLooping = true
                        }
                    }
                    music.play()
                }
            }
            is EntityAttackEvent -> queueSound("audio/${event.atlasKey}_attack.wav")
            is EntityDeathEvent -> queueSound("audio/${event.atlasKey}_death.wav")
            else -> return false
        }
        return true
    }

    private fun queueSound(path: String) {
        log.debug { "Queueing new sound '$path'" }
        if (soundRequests.containsKey(path)) {
            // already requested in this frame -> do not add it again
            return
        }

        val snd = soundCache.computeIfAbsent(path) {
            Gdx.audio.newSound(Gdx.files.internal(it))
        }
        soundRequests[path] = snd
    }

    override fun onDispose() {
        soundCache.values.forEach { it.disposeSafely() }
        musicCache.values.forEach { it.disposeSafely() }
    }

    companion object {
        private val log = logger<AudioSystem>()
    }
}