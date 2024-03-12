package crocodile8.universal_cache

import crocodile8.universal_cache.keep.Cache
import crocodile8.universal_cache.keep.MemoryCache

class CachedSourceNoParams<T : Any>(
    source: suspend () -> T,
    cache: Cache<Int, T> = MemoryCache(1),
) : CachedSource<Int, T>(
    source = { source() },
    cache = cache
) {
    fun get(
        fromCache: FromCache,
        shareOngoingRequest: Boolean = true,
        maxAge: Long? = null,
        additionalKey: Any? = null
    ) = get(
        0,
        fromCache,
        shareOngoingRequest,
        maxAge,
        additionalKey,
    )
}