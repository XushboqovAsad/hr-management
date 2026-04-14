package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.BusinessTripApproval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessTripApprovalRepository extends JpaRepository<BusinessTripApproval, UUID> {
    Optional<BusinessTripApproval> findByIdAndDeletedFalse(UUID id);

    List<BusinessTripApproval> findAllByBusinessTripIdAndDeletedFalseOrderByStepNoAsc(UUID businessTripId);
}

