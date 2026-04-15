package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LmsTestAttemptAnswerRepository extends JpaRepository<LmsTestAttemptAnswer, UUID> {
    List<LmsTestAttemptAnswer> findAllByAttemptIdAndDeletedFalse(UUID attemptId);
}
