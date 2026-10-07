package com.embasa.plcounter.ui

import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthReference
import com.embasa.plcounter.domain.growth.GrowthStatus
import com.embasa.plcounter.domain.growth.WeighIn
import com.embasa.plcounter.ui.growth.GrowthChartModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthChartModelTest {

    private val reference = GrowthReference.example()
    private val start = 20_000L

    @Test
    fun `ponto inicial vem da estimativa e as pesagens viram semanas`() {
        val lot = GrowthLot("x", "T1", start, 0.0073, listOf(WeighIn(start + 7, 0.015), WeighIn(start + 14, 0.20)))
        val m = GrowthChartModel.build(lot, reference, log = true)
        assertEquals(3, m.points.size)
        assertTrue(m.points[0].isBaseline)
        assertEquals(1.0, m.points[1].week, 1e-12)
        assertEquals(2.0, m.points[2].week, 1e-12)
        assertEquals(GrowthStatus.Within, m.points[0].status)
        assertEquals(GrowthStatus.Within, m.points[1].status)
        assertEquals(GrowthStatus.Above, m.points[2].status)   // 0,20 g na semana 2: faixa 0,028–0,066
    }

    @Test
    fun `eixos cobrem a faixa e os pontos`() {
        val lot = GrowthLot("x", "T1", start, 0.0073, listOf(WeighIn(start + 7, 0.015)))
        val m = GrowthChartModel.build(lot, reference, log = true)
        assertEquals(8.0, m.xAxis.max, 1e-12)          // mínimo de 8 semanas
        assertEquals(0.001, m.yAxis.min, 1e-12)
        assertEquals(10.0, m.yAxis.max, 1e-9)          // faixa vai a 5,58 g na semana 8
    }

    @Test
    fun `lote sem pesagens e sem baseline ainda mostra a faixa esperada`() {
        val m = GrowthChartModel.build(GrowthLot("x", "T1", start, null), reference, log = false)
        assertTrue(m.points.isEmpty())
        assertTrue(m.bandWeeks.isNotEmpty())
        assertEquals(m.bandWeeks.size, m.bandMin.size)
        assertEquals(m.bandWeeks.size, m.typical.size)
    }

    @Test
    fun `pesagem anterior ao inicio nao entra no grafico`() {
        val lot = GrowthLot("x", "T1", start, null, listOf(WeighIn(start - 3, 0.01), WeighIn(start + 7, 0.02)))
        val m = GrowthChartModel.build(lot, reference, log = true)
        assertEquals(1, m.points.size)
    }

    @Test
    fun `fora da tabela o ponto fica sem status`() {
        val lot = GrowthLot("x", "T1", start, null, listOf(WeighIn(start + 7 * 30, 20.0)))
        val m = GrowthChartModel.build(lot, reference, log = true)
        assertNull(m.points[0].status)
    }

    @Test
    fun `eixo das semanas cresce quando ha pesagens tardias`() {
        val lot = GrowthLot("x", "T1", start, null, listOf(WeighIn(start + 7 * 15, 12.0)))
        val m = GrowthChartModel.build(lot, reference, log = false)
        assertEquals(16.0, m.xAxis.max, 1e-12)
    }

    @Test
    fun `curva traz nome vindo do comentario`() {
        assertEquals("Curva de exemplo (não oficial)", GrowthReference.example().name)
        assertEquals("X", GrowthReference.parseCsv("# Nome: X\n0,0.1,0.2\n1,0.2,0.4").name)
        assertNull(GrowthReference.parseCsv("0,0.1,0.2\n1,0.2,0.4").name)
    }
}