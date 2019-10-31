package task.service;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.dao.AccountDao;
import task.exception.LimitExceededException;
import task.exception.NoSuchAccountException;
import task.manager.AccountManager;
import task.model.Account;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

/**
 * Thread safe implementation of {@link AccountService}. If several threads want to modify the same account they will be
 * synchronized by lock for that particular account to prevent inconsistent state.
 *
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@ThreadSafe
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountManager accountManager;
    private final AccountDao accountDao;
    private final OrderedLocksProvider orderedLocksProvider;

    @Inject
    public AccountServiceImpl(AccountManager accountManager, AccountDao accountDao,
                              OrderedLocksProvider orderedLocksProvider) {
        this.accountManager = accountManager;
        this.accountDao = accountDao;
        this.orderedLocksProvider = orderedLocksProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account create(@Nullable BigDecimal amount) {
        log.info("Creating account [amount: {}]", amount);
        try {
            final Account account = new Account().setAmount(Optional.ofNullable(amount).orElse(BigDecimal.ZERO));
            accountDao.insert(account);
            log.info("Account has been successfully created [account: {}]", account);
            return account;
        } catch (RuntimeException e) {
            log.error("Failed to create account [amount: {}, error message: {}]", amount, e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account getAccount(long accountId) {
        log.info("Getting account [accountId: {}]", accountId);
        try {
            final Account account = Optional.ofNullable(accountDao.select(accountId))
                    .orElseThrow(() -> new NoSuchAccountException(accountId));
            log.info("Account has been successfully found [account: {}]", account);
            return account;
        } catch (RuntimeException e) {
            log.error("Failed to get account [accountId: {}, error message: {}]", accountId, e.getMessage());
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(long fromAccountId, long toAccountId, BigDecimal amount) {
        log.info("Transferring amount [fromAccountId: {}, toAccountId: {}, amount:{}]", fromAccountId, toAccountId, amount);
        try {
            if (fromAccountId == toAccountId) {
                throw new IllegalArgumentException("fromAccountId == toAccountId: " + fromAccountId);
            }
            if (Objects.requireNonNull(amount, "amount is null").compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("amount is not positive: " + amount);
            }
            threadSafeTransfer(fromAccountId, toAccountId, amount);
            log.info("Amount has been successfully transferred [fromAccountId: {}, toAccountId: {}, amount:{}]",
                    fromAccountId, toAccountId, amount);
        } catch (RuntimeException e) {
            log.error("Failed to transfer amount [fromAccountId: {}, toAccountId: {}, amount:{}, error message: {}]",
                    fromAccountId, toAccountId, amount, e.getMessage());
            throw e;
        }
    }

    private void threadSafeTransfer(long fromAccountId, long toAccountId, BigDecimal amount) {
        // locks are ordered to avoid deadlocks
        final Pair<Lock, Lock> orderedLocks = orderedLocksProvider.getOrderedLocks(fromAccountId, toAccountId);

        final Lock firstLock = orderedLocks.getLeft();
        final Lock secondLock = orderedLocks.getRight();

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                transferInternal(fromAccountId, toAccountId, amount);
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    @GuardedBy("threadSafeTransfer()")
    private void transferInternal(long fromAccountId, long toAccountId, BigDecimal amount) {
        final Account fromAccount = getAccount(fromAccountId);
        final Account toAccount = getAccount(toAccountId);

        // this pre-check allows us to avoid extra database transaction: begin -> rollback in case of exceeded limit
        if (!fromAccount.canWithdraw(amount)) {
            throw new LimitExceededException(fromAccountId, amount, fromAccount.getAmount());
        }

        accountManager.transfer(fromAccount, toAccount, amount);
    }
}
