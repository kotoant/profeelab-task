package task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import task.dao.AccountDao;
import task.manager.AccountManager;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@Configuration
public class AccountManagerConfig {
    @Autowired
    private AccountDao accountDao;

    @Bean
    public AccountManager accountManager() {
        return new AccountManager(accountDao);
    }
}
