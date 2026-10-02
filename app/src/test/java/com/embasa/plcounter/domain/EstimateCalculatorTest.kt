package com.embasa.plcounter.domain

import com.embasa.plcounter.domain.model.BatchConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EstimateCalculatorTest {

    private val config = BatchConfig(sampleVolumeMl = 100.0, totalVolumeLiters = 1000.0)

    @Test
    fun `estima total pela media das amostras vezes o fator de escala`() {
        val r = EstimateCalculator.estimate(config, listOf(45, 50, 52))
        assertEquals(49.0, r.meanCount, 1e-9)
        assertEquals(490_000L, r.estimatedTotal)
        assertEquals(0.0736, r.coefficientOfVariation!!, 1e-3)
        assertFalse(r.highVariability)
    }

    @Test
    fun `amostras iguais nao tem variacao`() {
        val r = EstimateCalculator.estimate(config, listOf(10, 10, 10))
        assertEquals(100_000L, r.estimatedTotal)
        assertEquals(0.0, r.coefficientOfVariation!!, 1e-9)
    }

    @Test
    fun `media zero nao calcula variacao`() {
        val r = EstimateCalculator.estimate(config, listOf(0, 0, 0))
        assertEquals(0L, r.estimatedTotal)
        assertNull(r.coefficientOfVariation)
    }

    @Test
    fun `variacao alta entre amostras gera alerta`() {
        val r = EstimateCalculator.estimate(
            BatchConfig(sampleVolumeMl = 250.0, totalVolumeLiters = 500.0),
            listOf(30, 60, 90),
        )
        assertEquals(120_000L, r.estimatedTotal)
        assertNotNull(r.coefficientOfVariation)
        assertTrue(r.highVariability)
    }

    @Test
    fun `uma amostra so nao calcula variacao`() {
        assertNull(EstimateCalculator.estimate(config, listOf(40)).coefficientOfVariation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `lista vazia e invalida`() {
        EstimateCalculator.estimate(config, emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `volume zero e invalido`() {
        BatchConfig(sampleVolumeMl = 0.0, totalVolumeLiters = 1000.0)
    }
}