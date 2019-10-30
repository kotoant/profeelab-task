package task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import task.api.CreateAccountRequest;
import task.api.CreateAccountResponse;
import task.api.GetAccountResponse;
import task.api.TransferRequest;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Anton Kotov (kotov-anton@yandex.ru)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountServiceApplicationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_getAccount_it_must_return_GetAccountResponse_when_account_exists() throws Exception {
        // Given, when
        final GetAccountResponse response = getAccount(1);

        // Then
        assertThat(response.getAccountId()).isEqualTo(1);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    private GetAccountResponse getAccount(long accountId) {
        final String url = "http://localhost:" + port + "/accounts" + "/" + accountId;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<GetAccountResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, GetAccountResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    @Test
    public void test_getAccount_it_must_return_NOT_FOUND_when_account_does_not_exist() throws Exception {
        // Given, when
        final String url = "http://localhost:" + port + "/accounts/100500";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No such account: 100500");
    }

    @Test
    public void test_create_account_it_must_create_account_when_request_is_correct() throws Exception {
        // Given, when
        final BigDecimal amount = new BigDecimal("100.500");
        final CreateAccountResponse createResponse = createResponseEntity(amount);

        final long accountId = createResponse.getAccountId();
        assertThat(accountId).isGreaterThan(2);

        final GetAccountResponse getResponse = getAccount(accountId);

        // Then
        assertThat(getResponse.getAccountId()).isEqualTo(accountId);
        assertThat(getResponse.getAmount()).isEqualByComparingTo(amount);
    }

    private CreateAccountResponse createResponseEntity(BigDecimal amount) {
        final String url = "http://localhost:" + port + "/accounts/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(new CreateAccountRequest(amount), headers);

        ResponseEntity<CreateAccountResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, CreateAccountResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private ResponseEntity<String> createResponse(BigDecimal amount) {
        final String url = "http://localhost:" + port + "/accounts/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(new CreateAccountRequest(amount), headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    @Test
    public void test_create_account_it_must_return_BAD_REQUEST_when_amount_is_negative() throws Exception {
        // Given
        final BigDecimal amount = new BigDecimal("-100.500");

        // When
        final ResponseEntity<String> response = createResponse(amount);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("0.0");
    }

    @Test
    public void test_transfer_it_must_return_BAD_REQUEST_when_amount_is_negative() throws Exception {
        // Given
        final CreateAccountResponse from = createResponseEntity(new BigDecimal("100.500"));
        final CreateAccountResponse to = createResponseEntity(new BigDecimal("100000000"));

        // When
        final ResponseEntity<String> response = transferResponse(from.getAccountId(), to.getAccountId(), BigDecimal.TEN.negate());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("0.0");
    }

    private ResponseEntity<String> transferResponse(long fromAccountId, long toAccountId, BigDecimal amount) {
        final String url = "http://localhost:" + port + "/accounts/transfer";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> entity = new HttpEntity<>(new TransferRequest(fromAccountId, toAccountId, amount), headers);

        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    @Test
    public void test_transfer_it_must_return_BAD_REQUEST_when_amount_is_zero() throws Exception {
        // Given
        final CreateAccountResponse from = createResponseEntity(new BigDecimal("100.500"));
        final CreateAccountResponse to = createResponseEntity(new BigDecimal("100000000"));

        // When
        final ResponseEntity<String> response = transferResponse(from.getAccountId(), to.getAccountId(), BigDecimal.ZERO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("amount is not positive: 0");
    }

    @Test
    public void test_transfer_it_must_return_BAD_REQUEST_when_transfer_amount_is_greater_than_source_account_amount() throws Exception {
        // Given
        final CreateAccountResponse from = createResponseEntity(new BigDecimal("100.500"));
        final CreateAccountResponse to = createResponseEntity(new BigDecimal("100000000"));

        // When
        final ResponseEntity<String> response = transferResponse(from.getAccountId(), to.getAccountId(), new BigDecimal("200"));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains(
                "Failed to withdraw from account: ",
                "delta: 200 is greater than amount: 100.50000000"
        );
    }

    @Test
    public void test_transfer_it_must_transfer_when_request_is_correct() throws Exception {
        // Given
        final CreateAccountResponse from = createResponseEntity(new BigDecimal("100.500"));
        final CreateAccountResponse to = createResponseEntity(new BigDecimal("100000000"));

        // When
        final ResponseEntity<String> response = transferResponse(from.getAccountId(), to.getAccountId(), new BigDecimal("100"));

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("OK");

        final GetAccountResponse from2 = getAccount(from.getAccountId());
        final GetAccountResponse to2 = getAccount(to.getAccountId());

        assertThat(from2.getAmount()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(to2.getAmount()).isEqualByComparingTo(new BigDecimal("100000100"));
    }
}