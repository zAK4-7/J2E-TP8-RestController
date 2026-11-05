package ma.mlia.banque.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.mlia.banque.entities.Account;
import ma.mlia.banque.entities.AccountType;
import ma.mlia.banque.repositories.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller built with Spring MVC.
 * Every endpoint can answer in JSON or XML depending on the Accept header.
 */
@RestController
@RequestMapping("/banque")
@Tag(name = "Accounts", description = "Bank account management endpoints")
public class AccountController {

    private static final String JSON = "application/json";
    private static final String XML = "application/xml";

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Operation(summary = "List all accounts")
    @ApiResponse(responseCode = "200", description = "Accounts returned")
    @GetMapping(value = "/comptes", produces = {JSON, XML})
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Operation(summary = "Find an account by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping(value = "/comptes/{id}", produces = {JSON, XML})
    public ResponseEntity<Account> getAccountById(
            @Parameter(description = "Identifier of the account") @PathVariable Long id) {
        return accountRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create an account", description = "Returns 201 with a Location header pointing to the new resource")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    @PostMapping(value = "/comptes", consumes = {JSON, XML}, produces = {JSON, XML})
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        Account saved = accountRepository.save(account);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Update an existing account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping(value = "/comptes/{id}", consumes = {JSON, XML}, produces = {JSON, XML})
    public ResponseEntity<Account> updateAccount(
            @PathVariable Long id, @Valid @RequestBody Account accountDetails) {
        return accountRepository.findById(id)
                .map(account -> {
                    account.setBalance(accountDetails.getBalance());
                    account.setCreationDate(accountDetails.getCreationDate());
                    account.setType(accountDetails.getType());
                    return ResponseEntity.ok(accountRepository.save(account));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deleted"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping(value = "/comptes/{id}", produces = {JSON, XML})
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(account -> {
                    accountRepository.delete(account);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "List the accounts of a given type")
    @GetMapping(value = "/comptes/type", produces = {JSON, XML})
    public List<Account> getAccountsByType(@RequestParam AccountType type) {
        return accountRepository.findByType(type);
    }
}
