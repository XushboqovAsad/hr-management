package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.ExplanationHistory;

import java.util.List;
import java.util.UUID;

interface ExplanationHistoryRepository extends JpaRepository<ExplanationHistory, UUID> {
    List<ExplanationHistory> findAllByExplanationIncidentIdAndDeletedFalseOrderByCreatedAtDesc(UUID explanationIncidentId);
}
