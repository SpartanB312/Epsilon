package club.eridani.epsilon.client.gui.def.theme.impl

import club.eridani.epsilon.client.gui.def.components.Panel
import club.eridani.epsilon.client.gui.def.components.elements.*
import club.eridani.epsilon.client.gui.def.components.elements.other.SBBox
import club.eridani.epsilon.client.gui.def.theme.IDefaultBothTheme

object HyperTheme : IDefaultBothTheme {

    override val name = "Hyper"

    override fun saturationBrightnessBox(box: SBBox, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun actionButton(actionButton: ActionButton, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun bindButton(bindButton: BindButton, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun booleanButton(booleanButton: BooleanButton, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun colorPicker(colorPicker: ColorPicker, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun <T : Enum<T>> enumButton(enumButton: EnumButton<T>, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun moduleButton(moduleButton: ModuleButton, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun <T> numberSlider(
        numberSlider: NumberSlider<T>,
        mouseX: Int, mouseY: Int, partialTicks: Float
    ) where T : Comparable<T>, T : Number {
        TODO("Not yet implemented")
    }

    override fun stringField(stringField: StringField, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

    override fun panel(panel: Panel, mouseX: Int, mouseY: Int, partialTicks: Float) {
        TODO("Not yet implemented")
    }

}