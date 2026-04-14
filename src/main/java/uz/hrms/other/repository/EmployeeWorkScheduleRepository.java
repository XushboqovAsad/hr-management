package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeWorkScheduleRepository extends JpaRepository<EmployeeWorkSchedule, UUID> {
    Optional<EmployeeWorkSchedule> findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(UUID employeeId, LocalDate dateStart, LocalDate dateEnd);

    Optional<EmployeeWorkSchedule> findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(UUID employeeId, LocalDate date);
}
