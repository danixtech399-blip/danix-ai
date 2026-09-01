package com.danix.ai

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class MathEngine {
    private val ctx = MathContext(10, RoundingMode.HALF_UP)

    fun calculate(expr: String): String {
        return try {
            val cleaned = expr.replace("×", "*").replace("÷", "/").replace(" ", "")
            evaluate(cleaned).stripTrailingZeros().toPlainString()
        } catch (e: Exception) {
            "Unable to calculate"
        }
    }

    private fun evaluate(e: String): BigDecimal {
        return when {
            e.contains("+") -> {
                val p = e.split("+", limit = 2)
                evaluate(p[0]).add(evaluate(p[1]), ctx)
            }
            e.contains("-") && !e.startsWith("-") -> {
                val p = e.split("-", limit = 2)
                evaluate(p[0]).subtract(evaluate(p[1]), ctx)
            }
            e.contains("*") -> {
                val p = e.split("*", limit = 2)
                evaluate(p[0]).multiply(evaluate(p[1]), ctx)
            }
            e.contains("/") -> {
                val p = e.split("/", limit = 2)
                val divisor = evaluate(p[1])
                if (divisor.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException()
                evaluate(p[0]).divide(divisor, ctx)
            }
            else -> BigDecimal(e)
        }
    }
}
