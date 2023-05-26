class MathUtils {
    companion object {
        fun isDivisible(value: Double, by: Double): Boolean {
            return value % by == 0.0
        }

        fun ceilByValue(value: Double, by: Double): Double {
            return value + by - value % by
        }
    }
}