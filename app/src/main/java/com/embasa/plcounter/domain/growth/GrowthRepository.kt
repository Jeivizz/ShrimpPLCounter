package com.embasa.plcounter.domain.growth

/** O domínio só conhece este contrato; onde e como se guarda é detalhe da camada data. */
interface GrowthRepository {
    suspend fun load(): List<GrowthLot>
    suspend fun save(lots: List<GrowthLot>)
}