package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.DisciplinaryAction;

import java.util.List;
import java.util.UUID;

interface DisciplinaryActionRepository extends JpaRepository<DisciplinaryAction, UUID> {
    List<DisciplinaryAction> findAllByDeletedFalseOrderByActionDateDescCreatedAtDesc();

    List<DisciplinaryAction> findAllByEmployeeIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(UUID employeeId);

    List<DisciplinaryAction> findAllByDepartmentIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(UUID departmentId);
}
