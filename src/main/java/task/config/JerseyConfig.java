package task.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.context.annotation.Configuration;
import task.rest.AccountExceptionMapper;
import task.rest.AccountResource;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {

        register(AccountResource.class);
        register(AccountExceptionMapper.class);

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }
}
