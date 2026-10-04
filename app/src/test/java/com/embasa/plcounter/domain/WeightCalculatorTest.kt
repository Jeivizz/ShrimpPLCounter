package com.embasa.plcounter.domain

import com.embasa.plcounter.domain.model.Agreement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WeightCalculatorTest {

    // 3 amostras: 45, 50 e 52 PLs, pesando 0,30 / 0,33 / 0,36 g  ->  0,99 g em 147 PLs
    private val counts = listOf(45, 50, 52)
    private val weights = listOf(0.30, 0.33, 0.36)
    private val estimatedTotal = 490_000L

    @Test
    fun `peso por PL e a razao entre soma dos pesos e soma das PLs`() {
        val e = WeightCalculator.estimate(counts, weights, estimatedTotal)
        assertEquals(0.99 / 147, e.weightPerPlG, 1e-12)          // ~6,73 mg
        assertEquals(3300.0, e.expectedTotalWeightG, 1e-6)       // 3,3 kg
    }

    @Test
    fun `contagem por peso concorda com a imagem`() {
        val e = WeightCalculator.estimate(counts, weights, estimatedTotal)
        val c = WeightCalculator.compare(e, estimatedTotal, measuredTotalWeightG = 3465.0)
        assertEquals(514_500L, c.countByWeight)
        assertEquals(0.05, c.deviation, 1e-9)
        assertEquals(Agreement.GOOD, c.agreement)
    }

    @Test
    fun `concordancia moderada e baixa`() {
        val e = WeightCalculator.estimate(counts, weights, estimatedTotal)
        assertEquals(Agreement.MODERATE, WeightCalculator.compare(e, estimatedTotal, 3900.0).agreement)
        val low = WeightCalculator.compare(e, estimatedTotal, 2400.0)
        assertEquals(Agreement.LOW, low.agreement)
        assertEquals(true, low.deviation < 0)   // pesou menos que o esperado
    }

    @Test
    fun `variacao entre amostras so existe com duas ou mais`() {
        assertNull(WeightCalculator.estimate(listOf(40), listOf(0.27), 100_000).coefficientOfVariation)
        val e = WeightCalculator.estimate(counts, weights, estimatedTotal)
        assertEquals(3, e.perSampleWeightPerPlG.size)
        assertNotNull(e.coefficientOfVariation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `peso zero e invalido`() {
        WeightCalculator.estimate(counts, listOf(0.3, 0.0, 0.36), estimatedTotal)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `sem nenhuma PL contada e invalido`() {
        WeightCalculator.estimate(listOf(0, 0, 0), weights, estimatedTotal)
    }
}