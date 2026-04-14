package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.BusinessTripDocument;
import uz.hrms.other.enums.BusinessTripDocumentKind;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface BusinessTripDocumentRepository extends JpaRepository<BusinessTripDocument, UUID> {
    Optional<BusinessTripDocument> findByIdAndDeletedFalse(UUID id);

    List<BusinessTripDocument> findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(UUID businessTripId);

    List<BusinessTripDocument> findAllByBusinessTripIdAndDocumentKindAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(UUID businessTripId, BusinessTripDocumentKind documentKind, String title);
}
