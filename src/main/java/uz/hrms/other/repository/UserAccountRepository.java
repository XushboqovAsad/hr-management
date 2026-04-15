package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByIdAndDeletedFalse(UUID id);
    Optional<UserAccount> findByUsernameIgnoreCaseAndDeletedFalse(String username);
}
