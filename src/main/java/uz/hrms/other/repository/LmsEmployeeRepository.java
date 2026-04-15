package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LmsEmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    @Query("select e from Employee e left join fetch e.user where e.deleted = false and e.employmentStatus in ('ACTIVE', 'ONBOARDING')")
    List<Employee> findAllForLearningSync();
}
