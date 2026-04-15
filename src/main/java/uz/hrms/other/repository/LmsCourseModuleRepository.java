package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LmsCourseModuleRepository extends JpaRepository<LmsCourseModule, UUID> {
    List<LmsCourseModule> findAllByCourseIdAndDeletedFalseOrderByModuleOrderAsc(UUID courseId);
}
