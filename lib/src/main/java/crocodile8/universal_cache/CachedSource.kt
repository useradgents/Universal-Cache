package crocodile8.universal_cache

import crocodile8.universal_cache.keep.Cache
import crocodile8.universal_cache.keep.MemoryCache
import crocodile8.universal_cache.request.Requester
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedSource<P : Any, T : Any>(
    source: suspend (params: P) -> T,
    private val cache: Cache<P, T> = MemoryCache(1),
) {
    private val requester = Requester(source)
    private val cacheLock = Mutex()

    suspend fun get(
        params: P,
        fromCache: FromCache,
        cacheRequirement: CacheRequirement,
        additionalKey: Any? = null,
    ): Flow<T> =
        when (fromCache) {
            FromCache.NEVER -> {
                getFromSource(params, additionalKey)
            }
            FromCache.IF_FAILED -> {
                getFromSource(params, additionalKey)
                    .catch {
                        val cached = getFromCache(params, additionalKey, cacheRequirement)
                        if (cached != null) {
                            emit(cached)
                        } else {
                            throw it
                        }
                    }
            }
            FromCache.IF_HAVE -> {
                val cached = getFromCache(params, additionalKey, cacheRequirement)
                if (cached != null) {
                    flow { emit(cached) }
                } else {
                    getFromSource(params, additionalKey)
                }
            }
        }

    private suspend fun getFromSource(params: P, additionalKey: Any?): Flow<T> =
        requester.requestShared(params)
            .onEach {
                Logger.log { "getFromSource: $params -> $it" }
                putToCache(it, params, additionalKey)
            }

    private suspend fun getFromCache(params: P, additionalKey: Any?, cacheRequirement: CacheRequirement): T? {
        //TODO check for cacheRequirement
        cacheLock.withLock {
            return cache.get(params, additionalKey)
        }
    }

    private suspend fun putToCache(value: T, params: P, additionalKey: Any?) {
        cacheLock.withLock {
            cache.put(value, params, additionalKey)
        }
    }
}