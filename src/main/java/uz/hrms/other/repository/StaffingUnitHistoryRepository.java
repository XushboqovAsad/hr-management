package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StaffingUnitHistoryRepository extends JpaRepository<StaffingUnitHistory, UUID> {
    long countByStaffingUnitId(UUID staffingUnitId);

    List<StaffingUnitHistory> findAllByStaffingUnitIdOrderByVersionNoDesc(UUID staffingUnitId);
}
