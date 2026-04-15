package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeSelfServiceUserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByIdAndDeletedFalse(UUID id);
}
