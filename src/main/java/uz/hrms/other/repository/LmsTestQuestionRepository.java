package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.LmsTestQuestion;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LmsTestQuestionRepository extends JpaRepository<LmsTestQuestion, UUID> {
    List<LmsTestQuestion> findAllByTestIdAndDeletedFalseOrderByQuestionOrderAsc(UUID testId);
    List<LmsTestQuestion> findAllByTestIdInAndDeletedFalseOrderByQuestionOrderAsc(Collection<UUID> testIds);
}
