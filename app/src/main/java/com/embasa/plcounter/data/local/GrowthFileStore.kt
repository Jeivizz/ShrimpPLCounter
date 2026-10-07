package com.embasa.plcounter.data.local

import com.embasa.plcounter.domain.growth.GrowthLot
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Leitura e gravação síncronas do arquivo de lotes (a camada suspend fica em FileGrowthRepository).
 * - Grava num arquivo temporário e troca por rename: uma queda no meio não corrompe o arquivo.
 * - Se o arquivo existir mas não puder ser lido, guarda uma cópia ".corrompido" e começa vazio,
 *   em vez de apagar os dados sem aviso.
 */
class GrowthFileStore(private val file: File) {

    fun read(): List<GrowthLot> {
        if (!file.exists()) return emptyList()
        return try {
            GrowthJson.decode(file.readText())
        } catch (e: Exception) {
            runCatching { file.copyTo(File(file.parentFile, file.name + ".corrompido"), overwrite = true) }
            emptyList()
        }
    }

    fun write(lots: List<GrowthLot>) {
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, file.name + ".tmp")
        temp.writeText(GrowthJson.encode(lots))
        try {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}