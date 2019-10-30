package task.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import task.config.DataSourceConfig;
import task.model.Account;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DataSourceConfig.class)
public class AccountDaoTest {

    @Autowired
    private AccountDao dao;

    @Test
    public void test_select() throws Exception {
        final Account account0 = dao.select(0);
        final Account account1 = dao.select(1);
        final Account account2 = dao.select(2);

        assertThat(account0).isNull();
        assertThat(account1).isEqualTo(new Account().setId(1).setAmount(new BigDecimal("123.45")));
        assertThat(account2).isEqualTo(new Account().setId(2).setAmount(new BigDecimal("678.90")));
    }

    @Test
    public void test_insert() throws Exception {
        final Account account = new Account().setAmount(new BigDecimal("4.5"));

        dao.insert(account);

        assertThat(account.getId()).isGreaterThan(2);
        assertThat(account.getAmount()).isEqualByComparingTo(new BigDecimal(4.5));
    }

    @Test
    public void test_update() throws Exception {
        final Account account = new Account().setAmount(new BigDecimal("123.45"));
        dao.insert(account);
        account.setAmount(BigDecimal.ZERO);

        dao.update(account);

        assertThat(dao.select(account.getId())).isEqualTo(new Account().setId(account.getId()).setAmount(BigDecimal.ZERO));
    }

}