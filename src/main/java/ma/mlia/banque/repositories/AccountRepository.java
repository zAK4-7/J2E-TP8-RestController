package ma.mlia.banque.repositories;

import ma.mlia.banque.entities.Account;
import ma.mlia.banque.entities.AccountType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for the bank accounts.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Returns the accounts of a given type.
     */
    List<Account> findByType(AccountType type);
}
