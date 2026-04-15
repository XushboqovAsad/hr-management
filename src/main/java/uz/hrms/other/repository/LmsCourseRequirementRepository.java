package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LmsCourseRequirementRepository extends JpaRepository<LmsCourseRequirement, UUID> {
    List<LmsCourseRequirement> findAllByCourseIdAndDeletedFalseOrderByCreatedAtAsc(UUID courseId);
    List<LmsCourseRequirement> findAllByActiveTrueAndDeletedFalse();
}
