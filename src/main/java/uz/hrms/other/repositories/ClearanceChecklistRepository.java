package uz.hrms.other.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.ClearanceChecklist;

public interface ClearanceChecklistRepository extends JpaRepository<ClearanceChecklist, UUID> {
    Optional<ClearanceChecklist> findByDismissalRequestIdAndDeletedFalse(UUID dismissalRequestId);
}

