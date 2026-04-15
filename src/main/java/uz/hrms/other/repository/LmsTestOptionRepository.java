package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LmsTestOptionRepository extends JpaRepository<LmsTestOption, UUID> {
    List<LmsTestOption> findAllByQuestionIdAndDeletedFalseOrderByOptionOrderAsc(UUID questionId);
    List<LmsTestOption> findAllByQuestionIdInAndDeletedFalseOrderByOptionOrderAsc(Collection<UUID> questionIds);
}
