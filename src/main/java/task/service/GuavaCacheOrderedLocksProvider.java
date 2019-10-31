package task.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
public class GuavaCacheOrderedLocksProvider implements OrderedLocksProvider {

    // This cache keep locks per account id. Weak values guarantee that once the lock is unused the corresponding entity
    // will be removed from the cache.
    private final LoadingCache<Long, Lock> locksByAccountId = CacheBuilder.newBuilder()
            .weakValues() // to avoid memory leak
            .build(new CacheLoader<Long, Lock>() {
                @Override
                public Lock load(Long key) throws Exception {
                    return new ReentrantLock();
                }
            });

    @Override
    public Pair<Lock, Lock> getOrderedLocks(long fromAccountId, long toAccountId) {
        // locks are ordered to avoid deadlocks
        final long firstId;
        final long secondId;
        if (fromAccountId < toAccountId) {
            firstId = fromAccountId;
            secondId = toAccountId;
        } else {
            firstId = toAccountId;
            secondId = fromAccountId;
        }
        return Pair.of(getOrCreateLock(firstId), getOrCreateLock(secondId));
    }

    private Lock getOrCreateLock(long accountId) {
        return locksByAccountId.getUnchecked(accountId);
    }
}
