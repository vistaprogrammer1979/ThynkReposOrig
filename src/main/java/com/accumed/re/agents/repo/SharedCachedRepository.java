package com.accumed.re.agents.repo;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Maintains the single in-memory {@link CachedRepository} instance that should
 * be reused by every worker.  Centralising the reference avoids accidental
 * per-worker copies when the pool is reinitialised under load.
 */
public final class SharedCachedRepository {

    private static final AtomicReference<CachedRepository> CURRENT = new AtomicReference<>();

    private SharedCachedRepository() {
        // utility class
    }

    public static void set(CachedRepository repo) {
        CURRENT.set(repo);
    }

    public static CachedRepository get() {
        return CURRENT.get();
    }

    public static Date getTimestamp() {
        CachedRepository repo = CURRENT.get();
        return repo != null ? repo.getTimeStamp() : null;
    }
}