package com.embasa.plcounter.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testa só a lógica pura (sem OpenCV nativo), então roda na JVM. */
class FilteringTest {

    private val params = VisionParams(minArea = 30, maxArea = 500, maxGap = 15, fragRatio = 0.5)

    private fun comp(x: Int, y: Int, w: Int, h: Int, area: Int) =
        Component(x, y, w, h, area, x + w / 2.0, y + h / 2.0)

    @Test
    fun `fragmento pequeno colado em PL maior e absorvido`() {
        val big1 = comp(0, 0, 20, 20, 200)
        val big2 = comp(200, 200, 20, 20, 200)
        val fragment = comp(25, 0, 8, 8, 40)   // a 5 px da big1
        val result = Filtering.filter(listOf(big1, big2, fragment), params)
        assertEquals(2, result.size)
        assertFalse(fragment in result)
    }

    @Test
    fun `blob pequeno isolado continua contando`() {
        val big1 = comp(0, 0, 20, 20, 200)
        val big2 = comp(200, 200, 20, 20, 200)
        val isolated = comp(400, 400, 8, 8, 40)  // longe de todos
        val result = Filtering.filter(listOf(big1, big2, isolated), params)
        assertEquals(3, result.size)
        assertTrue(isolated in result)
    }

    @Test
    fun `areas fora do intervalo sao descartadas`() {
        val ok = comp(0, 0, 20, 20, 200)
        val tooSmall = comp(100, 100, 3, 3, 10)
        val tooBig = comp(300, 300, 40, 40, 600)
        assertEquals(listOf(ok), Filtering.filter(listOf(ok, tooSmall, tooBig), params))
    }

    @Test
    fun `mediana segue a definicao do numpy`() {
        assertEquals(3.0, median(listOf(5, 1, 3)), 1e-9)
        assertEquals(2.5, median(listOf(1, 2, 3, 4)), 1e-9)
    }

    @Test
    fun `lista vazia nao quebra`() {
        assertTrue(Filtering.filter(emptyList(), params).isEmpty())
    }
}