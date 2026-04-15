package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LmsTestAttemptRepository extends JpaRepository<LmsTestAttempt, UUID> {
    List<LmsTestAttempt> findAllByAssignmentIdAndTestIdAndDeletedFalseOrderByAttemptNoDesc(UUID assignmentId, UUID testId);
}
