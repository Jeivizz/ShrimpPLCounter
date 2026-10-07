package com.embasa.plcounter.data.local

import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Guarda os lotes em um arquivo JSON na pasta privada do app (funciona sem internet). */
class FileGrowthRepository(file: File) : GrowthRepository {
    private val store = GrowthFileStore(file)
    private val lock = Mutex()

    override suspend fun load(): List<GrowthLot> =
        withContext(Dispatchers.IO) { lock.withLock { store.read() } }

    override suspend fun save(lots: List<GrowthLot>) =
        withContext(Dispatchers.IO) { lock.withLock { store.write(lots) } }
}