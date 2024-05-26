package com.k10tetry.xchange.feature.converter.domain.usecase

import com.k10tetry.xchange.feature.converter.domain.repository.XchangeLocalRepository
import com.k10tetry.xchange.feature.converter.domain.repository.XchangeRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import kotlin.jvm.Throws

class GetCurrenciesUseCase @Inject constructor(
    private val remoteRepository: XchangeRemoteRepository,
    private val localRepository: XchangeLocalRepository
) {

    /**
     * This will return the list of currencies from local storage
     * and in case local storage is empty fetch from remote and store
     * in local storage
     */
    operator fun invoke(): Flow<List<Pair<String, String>>> = flow {
        try {
            localRepository.getCurrencies()?.let {
                emit(jsonToCurrencyList(it))
            } ?: run {
                remoteRepository.getCurrencies()?.let { currencies ->
                    localRepository.saveCurrencies(currencies)
                    localRepository.getCurrencies()?.let {
                        emit(jsonToCurrencyList(it))
                    }
                }
            }
        } catch (jsonEx: JSONException) {
            jsonEx.printStackTrace()
        } catch (io: IOException) {
            io.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun jsonToCurrencyList(jsonString: String): List<Pair<String, String>> {
        val currencies = mutableListOf<Pair<String, String>>()
        val jsonRates = JSONObject(jsonString)
        val keys = jsonRates.keys()
        while (keys.hasNext()) {
            val currency = keys.next()
            currencies.add(currency to jsonRates.getString(currency))
        }
        return currencies
    }

}