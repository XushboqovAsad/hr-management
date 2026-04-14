package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.Explanation;

import java.util.Optional;
import java.util.UUID;

interface ExplanationRepository extends JpaRepository<Explanation, UUID> {
    Optional<Explanation> findByIdAndDeletedFalse(UUID id);

    Optional<Explanation> findByExplanationIncidentIdAndDeletedFalse(UUID explanationIncidentId);
}
