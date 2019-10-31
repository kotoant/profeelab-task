package task.service;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.locks.Lock;

/**
 * This interface provides pair of locks ordered by specified from- and to-account ids.
 *
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
public interface OrderedLocksProvider {
    Pair<Lock, Lock> getOrderedLocks(long fromAccountId, long toAccountId);
}
