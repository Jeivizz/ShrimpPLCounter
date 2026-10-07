package com.embasa.plcounter.data.local

import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.WeighIn
import org.json.JSONArray
import org.json.JSONObject

/** Formato do arquivo: {"version":1,"lots":[...]}. A versão permite migrar o formato no futuro. */
object GrowthJson {
    private const val VERSION = 1

    fun encode(lots: List<GrowthLot>): String {
        val array = JSONArray()
        lots.forEach { lot ->
            val weighIns = JSONArray()
            lot.weighIns.forEach { w ->
                weighIns.put(JSONObject().put("epochDay", w.epochDay).put("weightG", w.weightG))
            }
            array.put(
                JSONObject()
                    .put("id", lot.id)
                    .put("name", lot.name)
                    .put("startEpochDay", lot.startEpochDay)
                    .put("baselineWeightG", lot.baselineWeightG ?: JSONObject.NULL)
                    .put("weighIns", weighIns),
            )
        }
        return JSONObject().put("version", VERSION).put("lots", array).toString()
    }

    fun decode(text: String): List<GrowthLot> {
        val root = JSONObject(text)
        val array = root.getJSONArray("lots")
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            val ws = o.getJSONArray("weighIns")
            GrowthLot(
                id = o.getString("id"),
                name = o.getString("name"),
                startEpochDay = o.getLong("startEpochDay"),
                baselineWeightG = if (o.isNull("baselineWeightG")) null else o.getDouble("baselineWeightG"),
                weighIns = (0 until ws.length()).map { j ->
                    val w = ws.getJSONObject(j)
                    WeighIn(epochDay = w.getLong("epochDay"), weightG = w.getDouble("weightG"))
                },
            )
        }
    }
}