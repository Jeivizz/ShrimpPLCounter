package com.embasa.plcounter.domain.growth

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

enum class GrowthStatus { Below, Within, Above }

/** Faixa esperada num ponto da curva. [typicalG] é a média geométrica da faixa. */
data class ExpectedRange(val minG: Double, val maxG: Double) {
    val typicalG: Double get() = sqrt(minG * maxG)
}

/**
 * Curva de referência: faixa de peso (min–max, de UM camarão) em semanas inteiras.
 * Entre duas semanas a faixa é interpolada em escala LOGARÍTMICA, que acompanha melhor um
 * crescimento que começa exponencial. Fora da tabela não há expectativa (retorna null).
 */
class GrowthReference(points: List<Point>, val name: String? = null) {

    data class Point(val week: Int, val minG: Double, val maxG: Double)

    val points: List<Point> = points.sortedBy { it.week }

    init {
        require(this.points.size >= 2) { "A curva precisa de pelo menos 2 semanas" }
        require(this.points.map { it.week }.distinct().size == this.points.size) { "Semanas repetidas" }
        require(this.points.all { it.minG > 0 && it.maxG >= it.minG }) { "Pesos precisam ser > 0 e min <= max" }
    }

    val firstWeek: Int get() = points.first().week
    val lastWeek: Int get() = points.last().week

    fun rangeAt(week: Double): ExpectedRange? {
        if (week < firstWeek || week > lastWeek) return null
        val upperIndex = points.indexOfFirst { it.week >= week }
        val upper = points[upperIndex]
        if (upper.week.toDouble() == week || upperIndex == 0) return ExpectedRange(upper.minG, upper.maxG)
        val lower = points[upperIndex - 1]
        val t = (week - lower.week) / (upper.week - lower.week)
        return ExpectedRange(logLerp(lower.minG, upper.minG, t), logLerp(lower.maxG, upper.maxG, t))
    }

    /** Compara uma pesagem com a faixa da semana. Null se a semana está fora da tabela. */
    fun evaluate(week: Double, weightG: Double): Evaluation? {
        val range = rangeAt(week) ?: return null
        val status = when {
            weightG < range.minG -> GrowthStatus.Below
            weightG > range.maxG -> GrowthStatus.Above
            else -> GrowthStatus.Within
        }
        return Evaluation(status, weightG / range.typicalG - 1.0, range)
    }

    /** [deviationFromTypical]: 0,12 = 12% acima do peso típico da semana. */
    data class Evaluation(val status: GrowthStatus, val deviationFromTypical: Double, val range: ExpectedRange)

    private fun logLerp(a: Double, b: Double, t: Double): Double = exp(ln(a) + (ln(b) - ln(a)) * t)

    companion object {
        /**
         * Lê o CSV `semana,min_g,max_g`. Aceita ponto ou vírgula decimal, linhas em branco,
         * comentários (#) e a linha de cabeçalho. Linhas inválidas geram erro com o número da linha.
         */
        fun parseCsv(text: String): GrowthReference {
            val points = mutableListOf<Point>()
            var name: String? = null
            text.lineSequence().forEachIndexed { index, raw ->
                val line = raw.trim()
                if (line.startsWith("#")) {
                    // "# nome: Curva X" dá um título à curva, mostrado no gráfico
                    val m = Regex("""^#\s*nome\s*:\s*(.+)$""", RegexOption.IGNORE_CASE).find(line)
                    if (m != null) name = m.groupValues[1].trim()
                    return@forEachIndexed
                }
                if (line.isEmpty()) return@forEachIndexed
                val parts = splitDecimalSafe(line)
                val week = parts.getOrNull(0)?.trim()?.toIntOrNull()
                if (week == null) {
                    if (points.isEmpty() && parts.firstOrNull()?.trim()?.toDoubleOrNull() == null) return@forEachIndexed // cabeçalho
                    throw IllegalArgumentException("Linha ${index + 1}: semana inválida")
                }
                val min = parts.getOrNull(1)?.trim()?.replace(',', '.')?.toDoubleOrNull()
                val max = parts.getOrNull(2)?.trim()?.replace(',', '.')?.toDoubleOrNull()
                require(min != null && max != null) { "Linha ${index + 1}: pesos inválidos" }
                points += Point(week, min, max)
            }
            return GrowthReference(points, name)
        }

        /**
         * Separador de colunas: ';' se a linha tiver (aí a vírgula pode ser decimal, "0,004"),
         * senão ','. Com ',' como separador, os decimais precisam usar ponto ("0.004").
         */
        private fun splitDecimalSafe(line: String): List<String> =
            if (line.contains(';')) line.split(';') else line.split(',')

        /** Mesma tabela de res/raw/growth_reference.csv, usada se o arquivo faltar ou estiver inválido. */
        fun example(): GrowthReference = parseCsv(EXAMPLE_CSV)

        private const val EXAMPLE_CSV = """
# nome: Curva de exemplo (não oficial)
0,0.0052,0.0124
1,0.012,0.0286
2,0.0276,0.0659
3,0.0637,0.1519
4,0.1468,0.3501
5,0.3384,0.8069
6,0.78,1.86
7,1.56,3.72
8,2.34,5.58
9,3.12,7.44
10,3.9,9.3
11,4.68,11.16
12,5.46,13.02
13,6.24,14.88
14,7.02,16.74
15,7.8,18.6
16,8.58,20.46
17,9.36,22.32
"""
    }
}