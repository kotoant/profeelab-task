package task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import task.dao.AccountDao;
import task.manager.AccountManager;
import task.service.AccountService;
import task.service.AccountServiceImpl;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@Configuration
@Import(DataSourceConfig.class)
public class AccountServiceConfig {

    @Autowired
    private AccountDao accountDao;

    @Bean
    public AccountService accountService() {
        return new AccountServiceImpl(accountManager(), accountDao);
    }

    @Bean
    public AccountManager accountManager() {
        return new AccountManager(accountDao);
    }
}
