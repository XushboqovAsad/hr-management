package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.AbsenceDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AbsenceDocumentRepository extends JpaRepository<AbsenceDocument, UUID> {
    Optional<AbsenceDocument> findByIdAndDeletedFalse(UUID id);

    List<AbsenceDocument> findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(UUID absenceRecordId);

    List<AbsenceDocument> findAllByAbsenceRecordIdAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(UUID absenceRecordId, String title);
}
