package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.AttendanceIncident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AttendanceIncidentRepository extends JpaRepository<AttendanceIncident, UUID> {
    Optional<AttendanceIncident> findByIdAndDeletedFalse(UUID id);

    Optional<AttendanceIncident> findFirstByAttendanceSummaryIdAndIncidentTypeAndDeletedFalse(UUID attendanceSummaryId, String incidentType);

    List<AttendanceIncident> findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(UUID attendanceSummaryId);
}
