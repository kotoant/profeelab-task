package task.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents response for get account operation.
 *
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
public class GetAccountResponse {

    private final long accountId;
    private final BigDecimal amount;

    @JsonCreator
    public GetAccountResponse(@JsonProperty("accountId") long accountId, @JsonProperty("amount") BigDecimal amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    public long getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetAccountResponse that = (GetAccountResponse) o;
        return accountId == that.accountId &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, amount);
    }
}
