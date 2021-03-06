package task.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represent response for {@link CreateAccountRequest} operation.
 *
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
public class CreateAccountResponse {

    private final long accountId;

    @JsonCreator
    public CreateAccountResponse(@JsonProperty("accountId") long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAccountResponse response = (CreateAccountResponse) o;
        return accountId == response.accountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
