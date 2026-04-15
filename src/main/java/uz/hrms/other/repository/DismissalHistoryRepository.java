package uz.hrms.other.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.DismissalHistory;

public interface DismissalHistoryRepository extends JpaRepository<DismissalHistory, UUID> {
    List<DismissalHistory> findAllByDismissalRequestIdAndDeletedFalseOrderByCreatedAtDesc(UUID dismissalRequestId);
}

