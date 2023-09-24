package com.github.quillraven.mysticwoods.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.github.quillraven.mysticwoods.ui.Buttons
import com.github.quillraven.mysticwoods.ui.Drawables
import com.github.quillraven.mysticwoods.ui.Labels
import com.github.quillraven.mysticwoods.ui.get
import com.github.quillraven.mysticwoods.ui.model.DialogModel
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.actors.txt
import ktx.scene2d.*

class DialogView(
    private val model: DialogModel,
    skin: Skin
) : Table(skin), KTable {

    private val dialogTxt: Label
    private val buttonArea: Table

    init {
        setFillParent(true)
        this.alpha = 0f

        table {
            background = skin[Drawables.FRAME_BGD]

            this@DialogView.dialogTxt = label(text = "", style = Labels.FRAME.skinKey) { lblCell ->
                this.setAlignment(Align.topLeft)
                this.wrap = true
                lblCell.expand().fill().pad(8f).row()
            }

            // button area (use table because horizontalGroup could not distribute buttons evenly)
            this@DialogView.buttonArea = table { btnAreaCell ->
                this.defaults().expand() // <- this line evenly distributes the buttons horizontally

                textButton("", Buttons.TEXT_BUTTON.skinKey)
                textButton("", Buttons.TEXT_BUTTON.skinKey)

                btnAreaCell.expandX().fillX().pad(0f, 8f, 8f, 8f)
            }

            // this.alpha = 0f
            it.expand().width(200f).height(130f).center().row()
        }

        // data binding
        model.onPropertyChange(DialogModel::text) {
            dialogTxt.txt = it
            this.alpha = 1f
        }
        model.onPropertyChange(DialogModel::completed) { completed ->
            if (completed) {
                this.alpha = 0f
                this.buttonArea.clearChildren()
            }
        }
        model.onPropertyChange(DialogModel::options) { dialogOptions ->
            buttonArea.clearChildren()

            dialogOptions.forEach {
                // don't use plusAssign KTX operator because it will not properly use the table's defaults
                buttonArea.add(textButton(it.text, Buttons.TEXT_BUTTON.skinKey).apply {
                    onClick { this@DialogView.model.triggerOption(it.idx) }
                })
            }
        }
    }

}

@Scene2dDsl
fun <S> KWidget<S>.dialogView(
    model: DialogModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: DialogView.(S) -> Unit = {}
): DialogView = actor(DialogView(model, skin), init)