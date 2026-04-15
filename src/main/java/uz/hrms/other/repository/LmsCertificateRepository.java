package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LmsCertificateRepository extends JpaRepository<LmsCertificate, UUID> {
    Optional<LmsCertificate> findByIdAndDeletedFalse(UUID id);
    Optional<LmsCertificate> findByAssignmentIdAndDeletedFalse(UUID assignmentId);
}
