package club.eridani.epsilon.client.setting.impl.number

class FloatSetting(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    visibility: (() -> Boolean) = { true },
    moduleName: String,
    description: String = ""
) : NumberSetting<Float>(name, value, range, step, visibility, moduleName, description, step) {

    override fun setByPercent(percent: Float) {
        value = range.start + ((range.endInclusive - range.start) * percent / step).toInt() * step
    }

    override fun getDisplay(): String {
        return String.format("%.2f", value)
    }

    override fun getPercentBar(): Float {
        return ((value - range.start) / (range.endInclusive - range.start))
    }

}