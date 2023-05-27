fun calculateBeta(t1: Double, t2: Double, n1: Int = 2, n2: Int = 6): Double {
    return (2 * Math.PI * (n1 * t2 - n2 * t1)) / (t1 * t1 * t2 - t2 * t2 * t1)
}

fun calculateJ(betaA: Double, betaB: Double): Double {
    val m = 0.05148
    val g = 9.794
    val r = 0.025
    val j = (m * g * r) / (betaA - betaB)
    return j
}

fun main() {

//    val betas = mutableListOf<Double>()
//    arrayOf(
//        1.822 to 3.855,
//        1.736 to 3.733,
//        1.774 to 3.826,
//
//        0.803 to 2.425,
//        0.802 to 2.421,
//        0.819 to 2.472,
//
//        1.784 to 3.839,
//        1.836 to 3.906,
//        1.778 to 3.821,
//
//        0.795 to 2.403,
//        0.818 to 2.473,
//        0.818 to 2.472
//    ).forEachIndexed { index, pair ->
//        val beta = calculateBeta(pair.first, pair.second)
//        betas.add(beta)
//
//        println("${pair.first.format()} ${pair.second.format()} --> ${beta.format()}")
//
//        if ((index + 1) % 3 == 0) {
//            val format = betas.joinToString(", ", "[ ", " ]") { it.format() }
//            val average = betas.average().format()
//            println("-------------------------------------------")
//            println("$format --> $average")
//            println("-------------------------------------------")
//            betas.clear()
//        }
//    } x

    arrayOf(
        1.400 to -0.060,
        1.359 to -0.071
    ).forEach { (a, b) ->
        println("$a $b --> ${calculateJ(a, b)}")
    }
}

fun Double.format(): String {
    return String.format("%.3f", this)
}