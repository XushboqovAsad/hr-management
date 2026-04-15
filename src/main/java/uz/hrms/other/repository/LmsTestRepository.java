package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LmsTestRepository extends JpaRepository<LmsTest, UUID> {
    Optional<LmsTest> findByIdAndDeletedFalse(UUID id);
    Optional<LmsTest> findByLessonIdAndDeletedFalse(UUID lessonId);
    List<LmsTest> findAllByLessonIdInAndDeletedFalse(Collection<UUID> lessonIds);
}
