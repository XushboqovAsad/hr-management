package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.WorkSchedule;

import java.util.Optional;
import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {
    Optional<WorkSchedule> findByIdAndDeletedFalse(UUID id);

    Optional<WorkSchedule> findByCodeAndDeletedFalse(String code);
}
