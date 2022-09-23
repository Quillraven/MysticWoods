package com.github.quillraven.mysticwoods.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class AnimationType {
    IDLE, RUN, ATTACK, DEATH, OPEN;

    val atlasKey: String = this.toString().lowercase()
}

data class AnimationComponent(
    var atlasKey: String = "",
    var stateTime: Float = 0f,
    var mode: Animation.PlayMode = Animation.PlayMode.LOOP
) : Component<AnimationComponent> {
    lateinit var animation: Animation<TextureRegionDrawable>
    var nextAnimation: String = NO_ANIMATION
        private set

    override fun type() = AnimationComponent

    fun nextAnimation(atlasKey: String, type: AnimationType) {
        this.atlasKey = atlasKey
        nextAnimation = "$atlasKey/${type.atlasKey}"
    }

    fun nextAnimation(type: AnimationType) {
        nextAnimation = "$atlasKey/${type.atlasKey}"
    }

    fun clearAnimation() {
        nextAnimation = NO_ANIMATION
    }

    fun isAnimationFinished() = animation.isAnimationFinished(stateTime)

    companion object : ComponentType<AnimationComponent>() {
        const val NO_ANIMATION = ""
    }
}