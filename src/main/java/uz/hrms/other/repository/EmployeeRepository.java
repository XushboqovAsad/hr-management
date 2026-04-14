package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    Optional<Employee> findByUserIdAndDeletedFalse(UUID userId);

    List<Employee> findAllByDeletedFalseOrderByCreatedAtDesc();

    boolean existsByPersonnelNumberIgnoreCaseAndDeletedFalse(String personnelNumber);

    boolean existsByPersonnelNumberIgnoreCaseAndIdNotAndDeletedFalse(String personnelNumber, UUID id);
}
