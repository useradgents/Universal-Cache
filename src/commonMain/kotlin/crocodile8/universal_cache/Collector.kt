package crocodile8.universal_cache

import crocodile8.universal_cache.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class Collector<P : Any, T : Any>(private val source : CachedSource<P,T>) : Flow<T> {

    private var currentParam : P? = null

    private var flow : Flow<T> = source.updates.filter {
        Logger.log {
            "${it.first} == $currentParam"
        }
        it.first == currentParam
    }.map {
        it.second.value
    }


    suspend fun get(
        params: P,
        fromCache: FromCache,
        shareOngoingRequest: Boolean = true,
        maxAge: Long? = null,
        additionalKey: Any? = null,
    ) {
        currentParam = params
        source.getRaw(params, fromCache, shareOngoingRequest, maxAge, additionalKey)
            .map { it.value }.collect()
    }

    override suspend fun collect(collector: FlowCollector<T>) {
        flow.collect(collector)
    }


}