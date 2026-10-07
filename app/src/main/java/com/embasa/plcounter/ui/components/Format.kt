package com.embasa.plcounter.ui.components

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatação pt-BR. Os formatadores são criados uma vez (instanciar NumberFormat é caro). */
object Fmt {
    private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
    private val integer: NumberFormat = NumberFormat.getIntegerInstance(ptBr)
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val number: NumberFormat = NumberFormat.getNumberInstance(ptBr).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    /** 490000 -> "490.000" */
    fun int(value: Long): String = integer.format(value)

    /** 100.0 -> "100"; 100.5 -> "100,5" (até 2 casas) */
    fun number(value: Double): String = number.format(value)

    /** Casas decimais fixas: fixed(6.734, 2) -> "6,73" */
    fun fixed(value: Double, digits: Int): String = String.format(ptBr, "%.${digits}f", value)

    /** Peso de um camarão: 0,0073 g -> "7,30 mg"; 0,52 -> "0,52 g"; 12,4 -> "12,4 g". */
    fun weightG(grams: Double): String = when {
        grams < 0.1 -> "${fixed(grams * 1000.0, if (grams * 1000.0 < 10) 2 else 1)} mg"
        grams < 10 -> "${fixed(grams, 2)} g"
        else -> "${fixed(grams, 1)} g"
    }

    /** Rótulo de eixo: 0,001 -> "1 mg"; 0,1 -> "0,1 g"; 10 -> "10 g". */
    fun axisWeight(grams: Double): String = when {
        grams == 0.0 -> "0"
        grams < 0.1 - 1e-12 -> "${number(grams * 1000.0)} mg"
        else -> "${number(grams)} g"
    }

    /** Dias desde 1970 -> "03/09/2026" */
    fun date(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dateFormat)

    /** 0.0736 -> "7,4%"; com signed = true -> "+7,4%" */
    fun percent(fraction: Double, signed: Boolean = false): String =
        String.format(ptBr, if (signed) "%+.1f%%" else "%.1f%%", fraction * 100)
}