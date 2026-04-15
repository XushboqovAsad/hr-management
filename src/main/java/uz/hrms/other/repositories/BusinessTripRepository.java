package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.BusinessTrip;
import uz.hrms.other.enums.BusinessTripStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessTripRepository extends JpaRepository<BusinessTrip, UUID> {
    Optional<BusinessTrip> findByIdAndDeletedFalse(UUID id);

    List<BusinessTrip> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<BusinessTrip> findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);

    List<BusinessTrip> findAllByStatusInAndEndDateBeforeAndDeletedFalse(List<BusinessTripStatus> statuses, LocalDate date);
}

