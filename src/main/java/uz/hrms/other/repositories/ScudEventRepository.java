package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.ScudEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScudEventRepository extends JpaRepository<ScudEvent, UUID> {
    Optional<ScudEvent> findByExternalEventIdAndDeletedFalse(String externalEventId);

    List<ScudEvent> findAllByEmployeeIdAndDeletedFalseAndEventAtBetweenOrderByEventAtAsc(UUID employeeId, OffsetDateTime from, OffsetDateTime to);

    List<ScudEvent> findAllByDeletedFalseAndEventAtBetweenOrderByEventAtAsc(OffsetDateTime from, OffsetDateTime to);
}
