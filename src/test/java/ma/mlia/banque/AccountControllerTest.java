package ma.mlia.banque;

import java.util.Date;
import ma.mlia.banque.entities.Account;
import ma.mlia.banque.entities.AccountType;
import ma.mlia.banque.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests of {@link ma.mlia.banque.controllers.AccountController}.
 * Each test calls the real HTTP endpoints and checks the JSON and XML bodies.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanDatabase() {
        accountRepository.deleteAll();
    }

    private Account newAccount(double balance, AccountType type) {
        return new Account(null, balance, new Date(), type);
    }

    @Test
    void getAllAccountsShouldReturnJson() {
        accountRepository.save(newAccount(1500.0, AccountType.CURRENT));

        ResponseEntity<String> response = restTemplate.getForEntity("/banque/comptes", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("CURRENT"));
    }

    @Test
    void getAllAccountsShouldReturnXmlWhenRequested() {
        accountRepository.save(newAccount(2500.0, AccountType.SAVINGS));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_XML));

        ResponseEntity<String> response = restTemplate.exchange(
                "/banque/comptes", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Jackson XML wraps the collection in a <List> element with one <item> per account
        assertTrue(response.getBody().contains("<item>") || response.getBody().contains("<account>"),
                "The XML body must contain an account element, got: " + response.getBody());
    }

    @Test
    void createAccountShouldReturnCreatedAndALocationHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"balance\":800.0,\"creationDate\":\"2024-11-01\",\"type\":\"SAVINGS\"}";

        ResponseEntity<Account> response = restTemplate.postForEntity(
                "/banque/comptes", new HttpEntity<>(payload, headers), Account.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getHeaders().getLocation());
        assertEquals(1, accountRepository.count());
    }

    @Test
    void createAccountShouldAcceptXmlPayload() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        String payload = "<account><balance>1200.0</balance>"
                + "<creationDate>2024-11-02</creationDate><type>CURRENT</type></account>";

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/banque/comptes", new HttpEntity<>(payload, headers), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, accountRepository.count());
    }

    @Test
    void getAccountByIdShouldReturnNotFoundForAnUnknownId() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/banque/comptes/{id}", String.class, 9999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateAccountShouldReplaceTheBalance() {
        Account saved = accountRepository.save(newAccount(100.0, AccountType.CURRENT));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"balance\":999.0,\"creationDate\":\"2024-11-01\",\"type\":\"SAVINGS\"}";

        ResponseEntity<Account> response = restTemplate.exchange("/banque/comptes/{id}", HttpMethod.PUT,
                new HttpEntity<>(payload, headers), Account.class, saved.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(999.0, accountRepository.findById(saved.getId()).orElseThrow().getBalance());
        assertEquals(AccountType.SAVINGS, accountRepository.findById(saved.getId()).orElseThrow().getType());
    }

    @Test
    void deleteAccountShouldRemoveIt() {
        Account saved = accountRepository.save(newAccount(50.0, AccountType.CURRENT));

        restTemplate.delete("/banque/comptes/{id}", saved.getId());

        assertEquals(0, accountRepository.count());
    }

    @Test
    void getAccountsByTypeShouldFilterOnTheEnum() {
        accountRepository.save(newAccount(100.0, AccountType.CURRENT));
        accountRepository.save(newAccount(200.0, AccountType.SAVINGS));
        accountRepository.save(newAccount(300.0, AccountType.CURRENT));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/banque/comptes/type?type=CURRENT", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("100.0") && response.getBody().contains("300.0"));
    }
}
