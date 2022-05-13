package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

enum class AnimationType {
    IDLE, RUN, ATTACK, DEATH, FEAR;

    val atlasKey: String = this.toString().lowercase()
}

data class AnimationComponent(
    var stateTime: Float = 0f,
    var mode: Animation.PlayMode = Animation.PlayMode.LOOP
) {
    lateinit var animation: Animation<TextureRegionDrawable>
    var nextAnimation: String = NO_ANIMATION
        private set

    fun nextAnimation(atlasKey: String, type: AnimationType) {
        nextAnimation = "$atlasKey/${type.atlasKey}"
    }

    fun clearAnimation() {
        nextAnimation = NO_ANIMATION
    }

    companion object {
        const val NO_ANIMATION = ""
    }
}