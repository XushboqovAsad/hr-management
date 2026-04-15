package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DismissalHistoryRepository extends JpaRepository<DismissalHistory, UUID> {
    List<DismissalHistory> findAllByDismissalRequestIdAndDeletedFalseOrderByCreatedAtDesc(UUID dismissalRequestId);
}
