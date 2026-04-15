package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.HrNotification;

import java.util.List;
import java.util.UUID;

public interface HrNotificationRepository extends JpaRepository<HrNotification, UUID> {
    List<HrNotification> findAllByRecipientEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);
}
