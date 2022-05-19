package com.github.quillraven.mysticwoods.system


import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.quillraven.fleks.*
import com.github.quillraven.mysticwoods.component.AnimationComponent
import com.github.quillraven.mysticwoods.component.AnimationComponent.Companion.NO_ANIMATION
import com.github.quillraven.mysticwoods.component.ImageComponent
import ktx.app.gdxError
import ktx.collections.map
import ktx.log.logger

@AllOf(components = [AnimationComponent::class, ImageComponent::class])
class AnimationSystem(
    @Qualifier("GameAtlas") private val atlas: TextureAtlas,
    private val animationCmps: ComponentMapper<AnimationComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>,
) : IteratingSystem() {
    private val cachedAnimations = mutableMapOf<String, Animation<TextureRegionDrawable>>()

    override fun onTickEntity(entity: Entity) {
        val aniCmp = animationCmps[entity]

        with(imageCmps[entity]) {
            image.drawable = if (aniCmp.nextAnimation != NO_ANIMATION) {
                aniCmp.run {
                    animation = animation(aniCmp.nextAnimation)
                    clearAnimation()
                    stateTime = 0f
                    animation.playMode = mode
                    animation.getKeyFrame(0f)
                }
            } else {
                aniCmp.run {
                    stateTime += deltaTime
                    animation.playMode = mode
                    animation.getKeyFrame(aniCmp.stateTime)
                }
            }
        }
    }

    private fun animation(atlasKey: String): Animation<TextureRegionDrawable> {
        return cachedAnimations.getOrPut(atlasKey) {
            LOG.debug { "Creating new animation $atlasKey" }
            val regions = atlas.findRegions(atlasKey)
            if (regions.isEmpty) {
                gdxError("There are no texture regions for $atlasKey")
            }
            Animation(DEFAULT_FRAME_DURATION, regions.map { TextureRegionDrawable(it) })
        }
    }

    companion object {
        private val LOG = logger<AnimationSystem>()
        private const val DEFAULT_FRAME_DURATION = 1 / 8f
    }
}