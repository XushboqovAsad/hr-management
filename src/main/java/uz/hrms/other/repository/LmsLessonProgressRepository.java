package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LmsLessonProgressRepository extends JpaRepository<LmsLessonProgress, UUID> {
    Optional<LmsLessonProgress> findByAssignmentIdAndLessonIdAndDeletedFalse(UUID assignmentId, UUID lessonId);
    List<LmsLessonProgress> findAllByAssignmentIdAndDeletedFalseOrderByCreatedAtAsc(UUID assignmentId);
}
