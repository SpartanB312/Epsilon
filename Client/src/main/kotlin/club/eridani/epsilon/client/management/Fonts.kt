package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.util.graphics.font.renderer.OtherFontRenderer

object Fonts {
    val logoFont = OtherFontRenderer("Microsoft YaHei", inJar = false, size = 128, textureSize = 8192)
    //OtherFontRenderer("Microsoft YaHei", inJar = false, size = 128, textureSize = 8192)
    val smallFont = OtherFontRenderer("Microsoft YaHei", inJar = false)
    val boldFont = OtherFontRenderer("Microsoft YaHei UI Bold", inJar = false, size = 64)
//    val logoFont = OtherFontRenderer("sfuiregular", size = 128, textureSize = 8192)
//    val smallFont = OtherFontRenderer("sfuiregular")

    val icon = OtherFontRenderer("IconFont")
    val badaboom = OtherFontRenderer("Badaboom")
    val osaka = OtherFontRenderer("OsakaChips")
    val knight = OtherFontRenderer("FoughtKnight")
}