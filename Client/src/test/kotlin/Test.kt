import java.math.BigDecimal
import java.math.RoundingMode


const val digit = 10000

fun main(){
    var num = BigDecimal("0")
    val k1 = BigDecimal(545140134)
    val k2 = BigDecimal(13591409)
    val k3 = BigDecimal(-640320)
    val k4 = BigDecimal(426880)
    val k5 = BigDecimal(10005)

    var times = 0
    while(true) {
        val k = BigDecimal(times)
        val num1 = factorial(BigDecimal("6").multiply(k)).multiply(k1.multiply(k).add(k2))
        val num2 = factorial(BigDecimal("3").multiply(k)).multiply(factorial(k).pow(3).multiply(k3.pow(3 * times)))
        num = num.add(num1.divide(num2, digit, RoundingMode.HALF_UP))

        val pi = (k4 * sqrt(k5, digit)).divide(num, digit, RoundingMode.HALF_UP)

        println("Calculated $times times pi=$pi")

        times++

        if (times == 1000) break
    }

}

fun sqrt(A: BigDecimal, SCALE: Int): BigDecimal {
    var x0 = BigDecimal("0")
    var x1 = BigDecimal(kotlin.math.sqrt(A.toDouble()))
    while (x0 != x1) {
        x0 = x1
        x1 = A.divide(x0, SCALE, RoundingMode.HALF_UP)
        x1 = x1.add(x0)
        x1 = x1.divide(BigDecimal("2"), SCALE, RoundingMode.HALF_UP)
    }
    return x1
}

fun factorial(a: BigDecimal): BigDecimal {
    var ans = BigDecimal("1")
    var i = a
    while(i.compareTo(BigDecimal("1")) == 1){
        ans = ans.multiply(i)
        i--
    }
    return ans
}