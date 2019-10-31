package task.manager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import task.config.AccountManagerConfig;
import task.config.DataSourceConfig;
import task.dao.AccountDao;
import task.exception.LimitExceededException;
import task.exception.NoSuchAccountException;
import task.model.Account;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataSourceConfig.class, AccountManagerConfig.class})
public class AccountManagerTest {

    @Autowired
    private AccountDao accountDao;
    @Autowired
    private AccountManager accountManager;

    @Test
    public void test_transfer_it_must_rollback_transaction_when_first_account_does_not_exist() throws Exception {
        // Given
        final Account fromAccount = new Account().setId(300).setAmount(BigDecimal.TEN);
        final Account toAccount = spy(accountDao.select(1));

        // When
        final Throwable exception = catchThrowable(() -> accountManager.transfer(fromAccount, toAccount, BigDecimal.ONE));

        // Then
        assertThat(exception)
                .isInstanceOf(NoSuchAccountException.class)
                .hasMessage("No such account: 300");

        verifyZeroInteractions(toAccount);

        assertThat(accountDao.select(1)).isEqualTo(new Account().setId(1).setAmount(new BigDecimal("123.45")));
    }

    @Test
    public void test_transfer_it_must_rollback_transaction_when_second_account_does_not_exist() throws Exception {
        // Given
        final Account fromAccount = spy(accountDao.select(2));
        final Account toAccount = new Account().setId(400).setAmount(BigDecimal.TEN);

        // When
        final Throwable exception = catchThrowable(() -> accountManager.transfer(fromAccount, toAccount, BigDecimal.ONE));

        // Then
        assertThat(exception)
                .isInstanceOf(NoSuchAccountException.class)
                .hasMessage("No such account: 400");

        verify(fromAccount).withdraw(eq(BigDecimal.ONE));

        assertThat(accountDao.select(2)).isEqualTo(new Account().setId(2).setAmount(new BigDecimal("678.90")));
    }

    @Test
    public void test_transfer_it_must_rollback_transaction_when_limit_exceeded() throws Exception {
        // Given
        final Account fromAccount = spy(accountDao.select(1));
        final Account toAccount = spy(accountDao.select(2));

        // When
        final Throwable exception = catchThrowable(() -> accountManager.transfer(fromAccount, toAccount, new BigDecimal("9999999999")));

        // Then
        assertThat(exception)
                .isInstanceOf(LimitExceededException.class)
                .hasMessageStartingWith("Failed to withdraw from account: 1");

        verify(fromAccount).withdraw(eq(new BigDecimal("9999999999")));

        verifyZeroInteractions(toAccount);
    }

    @Test
    public void test_transfer_it_must_tranfer_when_accounts_exist_and_money_is_enough() throws Exception {
        // Given
        final Account account1 = accountDao.select(1);
        accountDao.insert(account1);
        final Account account2 = accountDao.select(2);
        accountDao.insert(account2);

        final Account fromAccount = spy(accountDao.select(account1.getId()));
        final Account toAccount = spy(accountDao.select(account2.getId()));

        // When
        accountManager.transfer(fromAccount, toAccount, BigDecimal.TEN);

        // Then
        verify(fromAccount).withdraw(eq(BigDecimal.TEN));
        verify(toAccount).deposit(eq(BigDecimal.TEN));

        assertThat(accountDao.select(account1.getId())).isEqualTo(new Account().setId(account1.getId()).setAmount(new BigDecimal("113.45")));
        assertThat(accountDao.select(account2.getId())).isEqualTo(new Account().setId(account2.getId()).setAmount(new BigDecimal("688.90")));
    }
}