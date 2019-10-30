package task.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representation class for create account operation.
 *
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
public class CreateAccountRequest {

    @DecimalMin("0.0")
    @Digits(integer = 30, fraction = 8)
    private BigDecimal amount;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(BigDecimal amount) {
        this.amount = amount;
    }

    @JsonProperty
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAccountRequest that = (CreateAccountRequest) o;
        return Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}
