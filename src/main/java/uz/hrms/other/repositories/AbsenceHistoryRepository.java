package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.AbsenceHistory;

import java.util.List;
import java.util.UUID;

public interface AbsenceHistoryRepository extends JpaRepository<AbsenceHistory, UUID> {
    List<AbsenceHistory> findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(UUID absenceRecordId);
}
