package task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import task.dao.AccountDao;
import task.manager.AccountManager;
import task.service.AccountService;
import task.service.AccountServiceImpl;
import task.service.GuavaCacheOrderedLocksProvider;
import task.service.LocksArrayOrderedLocksProvider;
import task.service.OrderedLocksProvider;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@Configuration
public class AccountServiceConfig {

    @Value("${account.service.useGuavaCacheOrderedLocksProvider:false}")
    private boolean useGuavaCacheOrderedLocksProvider;

    @Autowired
    private AccountDao accountDao;
    @Autowired
    private AccountManager accountManager;

    @Bean
    public AccountService accountService() {
        return new AccountServiceImpl(accountManager, accountDao, orderedLocksProvider());
    }

    @Bean
    public OrderedLocksProvider orderedLocksProvider() {
        if (useGuavaCacheOrderedLocksProvider) {
            return new GuavaCacheOrderedLocksProvider();
        } else {
            return new LocksArrayOrderedLocksProvider();
        }
    }
}
