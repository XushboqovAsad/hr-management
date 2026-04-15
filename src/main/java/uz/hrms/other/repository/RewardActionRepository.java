package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.RewardAction;

import java.util.List;
import java.util.UUID;

interface RewardActionRepository extends JpaRepository<RewardAction, UUID> {
    List<RewardAction> findAllByDeletedFalseOrderByRewardDateDescCreatedAtDesc();

    List<RewardAction> findAllByEmployeeIdAndDeletedFalseOrderByRewardDateDescCreatedAtDesc(UUID employeeId);

    List<RewardAction> findAllByDepartmentIdAndDeletedFalseOrderByRewardDateDescCreatedAtDesc(UUID departmentId);
}
