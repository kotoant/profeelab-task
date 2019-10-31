package task.service;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
public class LocksArrayOrderedLocksProvider implements OrderedLocksProvider {

    private static final int LOCKS_SIZE = Integer.getInteger("LocksArrayOrderedLocksProvider.locksSize", 8192);

    private final Lock[] locks = new Lock[LOCKS_SIZE];

    {
        for (int i = 0; i < LOCKS_SIZE; ++i) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public Pair<Lock, Lock> getOrderedLocks(long fromAccountId, long toAccountId) {
        // locks indexes are ordered to avoid deadlocks
        final int fromLockIndex = getLockIndex(fromAccountId);
        final int toLockIndex = getLockIndex(toAccountId);
        final int firstIndex;
        final int secondIndex;
        if (fromLockIndex < toLockIndex) {
            firstIndex = fromLockIndex;
            secondIndex = toLockIndex;
        } else {
            firstIndex = toLockIndex;
            secondIndex = fromLockIndex;
        }
        return Pair.of(locks[firstIndex], locks[secondIndex]);
    }

    private int getLockIndex(long accountId) {
        return (int) (accountId % LOCKS_SIZE);
    }
}
