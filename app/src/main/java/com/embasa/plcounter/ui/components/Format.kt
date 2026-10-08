package com.embasa.plcounter.ui.components

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Fmt {
    private val ptBr: Locale = Locale.forLanguageTag("pt-BR")
    private val integer: NumberFormat = NumberFormat.getIntegerInstance(ptBr)
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val number: NumberFormat = NumberFormat.getNumberInstance(ptBr).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    fun int(value: Long): String = integer.format(value)

    fun number(value: Double): String = number.format(value)

    fun fixed(value: Double, digits: Int): String = String.format(ptBr, "%.${digits}f", value)

    fun weightG(grams: Double): String = when {
        grams < 0.1 -> "${fixed(grams * 1000.0, if (grams * 1000.0 < 10) 2 else 1)} mg"
        grams < 10 -> "${fixed(grams, 2)} g"
        else -> "${fixed(grams, 1)} g"
    }

    fun date(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dateFormat)

    fun percent(fraction: Double, signed: Boolean = false): String =
        String.format(ptBr, if (signed) "%+.1f%%" else "%.1f%%", fraction * 100)
}