package com.embasa.plcounter.domain

import com.embasa.plcounter.domain.model.SampleResult

interface CountingRepository {
    suspend fun countSample(imageBytes: ByteArray, fileName: String = "amostra.jpg"): Result<SampleResult>
}

sealed class CountingError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable) :
        CountingError("Sem conexão com o servidor. Verifique a internet e o endereço configurado.", cause)

    class InvalidImage :
        CountingError("Não foi possível ler a imagem. Tente outra foto.")

    class FileTooLarge :
        CountingError("A imagem é grande demais para envio.")

    class Processing(cause: Throwable) :
        CountingError("Falha ao processar a imagem neste aparelho. Tente outra foto.", cause)

    class Server(val code: Int) :
        CountingError("O servidor retornou um erro (código $code).")
}