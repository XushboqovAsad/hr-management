package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LmsLearningHistoryRepository extends JpaRepository<LmsLearningHistory, UUID> {
    List<LmsLearningHistory> findAllByEmployeeIdAndDeletedFalseOrderByActionAtDesc(UUID employeeId);
}
