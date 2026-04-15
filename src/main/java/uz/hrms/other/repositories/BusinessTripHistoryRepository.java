package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.BusinessTripHistory;

import java.util.List;
import java.util.UUID;

public interface BusinessTripHistoryRepository extends JpaRepository<BusinessTripHistory, UUID> {
    List<BusinessTripHistory> findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(UUID businessTripId);
}

