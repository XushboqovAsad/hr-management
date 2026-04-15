package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepartmentHistoryRepository extends JpaRepository<DepartmentHistory, UUID> {
    long countByDepartmentId(UUID departmentId);

    List<DepartmentHistory> findAllByDepartmentIdOrderByVersionNoDesc(UUID departmentId);
}
