package club.eridani.epsilon.client.setting.impl.number

class DoubleSetting(
    name: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    step: Double,
    visibility: (() -> Boolean) = { true },
    moduleName: String,
    description: String = ""
) : NumberSetting<Double>(name, value, range, step, visibility, description, moduleName, step) {

    override fun setByPercent(percent: Float) {
        value = range.start + ((range.endInclusive - range.start) * percent / step).toInt() * step
    }

    override fun getDisplay(): String {
        return String.format("%.2f", value)
    }

    override fun getPercentBar(): Float {
        return ((value - range.start) / (range.endInclusive - range.start)).toFloat()
    }

}