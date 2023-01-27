package club.eridani.epsilon.client.util.graphics

enum class HAlign(val displayName: String, val multiplier: Float, val offset: Float) {
    LEFT("Left", 0.0f, -1.0f),
    CENTER("Center", 0.5f, 0.0f),
    RIGHT("Right", 1.0f, 1.0f)
}

enum class VAlign(val displayName: String, val multiplier: Float, val offset: Float) {
    TOP("Top", 0.0f, -1.0f),
    CENTER("Center", 0.5f, 0.0f),
    BOTTOM("Bottom", 1.0f, 1.0f)
}