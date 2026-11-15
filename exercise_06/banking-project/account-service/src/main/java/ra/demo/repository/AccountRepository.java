package ra.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.demo.entity.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
}
