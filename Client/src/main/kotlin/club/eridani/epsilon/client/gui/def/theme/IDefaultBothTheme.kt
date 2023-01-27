package club.eridani.epsilon.client.gui.def.theme

import club.eridani.epsilon.client.gui.ITheme
import club.eridani.epsilon.client.gui.ThemeCategory
import club.eridani.epsilon.client.gui.def.components.Panel
import club.eridani.epsilon.client.gui.def.components.elements.*
import club.eridani.epsilon.client.gui.def.components.elements.other.SBBox

interface IDefaultBothTheme : ITheme {

    override val category: ThemeCategory
        get() = ThemeCategory.Root

    fun actionButton(actionButton: ActionButton, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun bindButton(bindButton: BindButton, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun booleanButton(booleanButton: BooleanButton, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun colorPicker(colorPicker: ColorPicker, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun <T : Enum<T>> enumButton(enumButton: EnumButton<T>, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun moduleButton(moduleButton: ModuleButton, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun <T> numberSlider(
        numberSlider: NumberSlider<T>,
        mouseX: Int, mouseY: Int, partialTicks: Float
    ) where T : Comparable<T>, T : Number

    fun stringField(stringField: StringField, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun saturationBrightnessBox(box: SBBox, mouseX: Int, mouseY: Int, partialTicks: Float)

    fun panel(panel: Panel, mouseX: Int, mouseY: Int, partialTicks: Float)

}