package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.ExplanationDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ExplanationDocumentRepository extends JpaRepository<ExplanationDocument, UUID> {
    Optional<ExplanationDocument> findByIdAndDeletedFalse(UUID id);

    List<ExplanationDocument> findAllByExplanationIdAndDeletedFalseOrderByCreatedAtDesc(UUID explanationId);

    List<ExplanationDocument> findAllByExplanationIdAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(UUID explanationId, String title);
}
