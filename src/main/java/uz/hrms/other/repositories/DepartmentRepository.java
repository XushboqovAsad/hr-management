package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.hrms.other.entity.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByIdAndDeletedFalse(UUID id);

    List<Department> findAllByDeletedFalseOrderByNameAsc();

    List<Department> findAllByParentDepartmentIdAndDeletedFalseOrderByNameAsc(UUID parentDepartmentId);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdAndDeletedFalse(String code, UUID id);

    boolean existsByNameIgnoreCaseAndParentDepartmentIdAndDeletedFalse(String name, UUID parentDepartmentId);

    @Query("select d from Department d where d.deleted = false and d.parentDepartment is null order by d.name asc")
    List<Department> findRootDepartments();
}