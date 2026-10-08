package com.embasa.plcounter.growth

import com.embasa.plcounter.domain.growth.AlertKind
import com.embasa.plcounter.domain.growth.AlertSeverity
import com.embasa.plcounter.domain.growth.GrowthAlertConfig
import com.embasa.plcounter.domain.growth.GrowthAlerts
import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthReference
import com.embasa.plcounter.domain.growth.WeighIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthAlertsTest {

    private val reference = GrowthReference.example()
    private val config = GrowthAlertConfig()
    private val start = 20_000L

    private fun at(week: Int, factor: Double): WeighIn {
        val typical = reference.rangeAt(week.toDouble())!!.typicalG
        return WeighIn(start + week * 7L, typical * factor)
    }

    private fun lot(vararg w: WeighIn) = GrowthLot("x", "T", start, 0.0073, w.toList())

    private fun alerts(l: GrowthLot, today: Long = l.latest()?.epochDay ?: start) =
        GrowthAlerts.evaluate(l, reference, config, today)

    @Test
    fun `lote saudavel nao gera alerta`() {
        val l = lot(at(4, 1.0), at(5, 1.0), at(6, 1.1), at(7, 0.9))
        assertTrue(alerts(l).isEmpty())
    }

    @Test
    fun `uma unica pesagem abaixo da faixa nao alerta`() {
        val l = lot(at(4, 1.0), at(5, 1.0), at(6, 0.4))
        assertTrue(alerts(l).none { it.kind == AlertKind.BelowRange })
    }

    @Test
    fun `duas pesagens seguidas abaixo geram atencao`() {
        val l = lot(at(4, 1.0), at(5, 0.4), at(6, 0.4))
        val a = alerts(l).first { it.kind == AlertKind.BelowRange }
        assertEquals(AlertSeverity.Attention, a.severity)
        assertEquals(2, a.count)
    }

    @Test
    fun `tres pesagens seguidas abaixo viram alerta critico`() {
        val l = lot(at(4, 1.0), at(5, 0.4), at(6, 0.4), at(7, 0.4))
        val a = alerts(l).first { it.kind == AlertKind.BelowRange }
        assertEquals(AlertSeverity.Critical, a.severity)
        assertEquals(3, a.count)
    }

    @Test
    fun `uma pesagem normal no meio zera a sequencia`() {
        val l = lot(at(3, 0.4), at(4, 0.4), at(5, 1.0), at(6, 0.4))
        assertTrue(alerts(l).none { it.kind == AlertKind.BelowRange })
    }

    @Test
    fun `acima da faixa so gera aviso informativo`() {
        val l = lot(at(4, 1.0), at(5, 2.5), at(6, 2.5))
        val a = alerts(l).first { it.kind == AlertKind.AboveRange }
        assertEquals(AlertSeverity.Info, a.severity)
        assertEquals(2, a.count)
    }

    @Test
    fun `varias pesagens na mesma semana contam como uma`() {
        val w = at(5, 0.4)
        val l = lot(at(4, 1.0), w, WeighIn(w.epochDay + 1, w.weightG * 1.01))   // mesma semana 5
        assertTrue(alerts(l).none { it.kind == AlertKind.BelowRange })           // só 1 semana abaixo
    }

    @Test
    fun `estagnacao dentro da faixa e detectada pelo ritmo`() {
        // pesos parados em ~1,0 g das semanas 6 a 9: dentro da faixa no começo, mas sem crescer
        val l = lot(at(6, 1.0), WeighIn(start + 7 * 7L, 1.0), WeighIn(start + 8 * 7L, 1.0))
        val a = alerts(l).first { it.kind == AlertKind.SlowGrowth }
        assertEquals(2, a.count)
        assertEquals(AlertSeverity.Attention, a.severity)
    }

    @Test
    fun `perda de peso conta como crescimento lento`() {
        val l = lot(at(6, 1.0), WeighIn(start + 7 * 7L, 0.9), WeighIn(start + 8 * 7L, 0.8))
        assertTrue(alerts(l).any { it.kind == AlertKind.SlowGrowth })
    }

    @Test
    fun `crescimento normal nao e lento`() {
        val l = lot(at(6, 1.0), at(7, 1.0), at(8, 1.0), at(9, 1.0))
        assertTrue(alerts(l).none { it.kind == AlertKind.SlowGrowth })
    }

    @Test
    fun `pesagem atrasada gera aviso`() {
        val l = lot(at(4, 1.0))
        val a = GrowthAlerts.evaluate(l, reference, config, l.latest()!!.epochDay + 11)
        assertEquals(AlertKind.OverdueWeighing, a.single().kind)
        assertEquals(11, a.single().count)
        assertTrue(GrowthAlerts.evaluate(l, reference, config, l.latest()!!.epochDay + 10).isEmpty())
    }

    @Test
    fun `lote novo sem pesagens so avisa depois do prazo`() {
        val l = GrowthLot("x", "T", start, null)
        assertTrue(GrowthAlerts.evaluate(l, reference, config, start + 5).isEmpty())
        assertEquals(AlertKind.OverdueWeighing, GrowthAlerts.evaluate(l, reference, config, start + 12).single().kind)
    }

    @Test
    fun `semanas fora da curva sao ignoradas`() {
        val l = lot(WeighIn(start + 7 * 30L, 0.001), WeighIn(start + 7 * 31L, 0.001))
        assertTrue(alerts(l).isEmpty())
    }

    @Test
    fun `alertas vem ordenados do mais grave para o mais leve`() {
        val l = lot(at(3, 1.0), at(4, 0.4), at(5, 0.4), at(6, 0.4))
        val a = alerts(l)
        assertTrue(a.zipWithNext().all { (x, y) -> x.severity >= y.severity })
        assertEquals(AlertSeverity.Critical, a.first().severity)
    }

    @Test
    fun `configuracao le diretivas do csv e usa padrao no que falta`() {
        val c = GrowthAlertConfig.parse("# nome: X\n# alerta_semanas: 3\n# alerta_critico_semanas: 4  pesagens\n# alerta_ritmo: 0,4\n0,0.1,0.2")
        assertEquals(3, c.weeksToAlert)
        assertEquals(4, c.weeksCritical)
        assertEquals(0.4, c.slowRateRatio, 1e-12)
        assertEquals(10, c.overdueDays)          // padrão
        assertEquals(GrowthAlertConfig(), GrowthAlertConfig.parse("0,0.1,0.2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `configuracao com valor invalido gera erro`() {
        GrowthAlertConfig.parse("# alerta_semanas: dois")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `critico menor que alerta e invalido`() {
        GrowthAlertConfig(weeksToAlert = 3, weeksCritical = 2)
    }
}