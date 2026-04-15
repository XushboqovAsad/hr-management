package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClearanceChecklistRepository extends JpaRepository<ClearanceChecklist, UUID> {
    Optional<ClearanceChecklist> findByDismissalRequestIdAndDeletedFalse(UUID dismissalRequestId);
}
