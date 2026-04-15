package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LmsLessonRepository extends JpaRepository<LmsLesson, UUID> {
    Optional<LmsLesson> findByIdAndDeletedFalse(UUID id);
    List<LmsLesson> findAllByCourseModuleIdInAndDeletedFalseOrderByLessonOrderAsc(Collection<UUID> moduleIds);
    List<LmsLesson> findAllByCourseModuleIdAndDeletedFalseOrderByLessonOrderAsc(UUID moduleId);
}
