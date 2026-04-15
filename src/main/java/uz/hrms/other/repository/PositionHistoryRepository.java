package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PositionHistoryRepository extends JpaRepository<PositionHistory, UUID> {
    long countByPositionId(UUID positionId);

    List<PositionHistory> findAllByPositionIdOrderByVersionNoDesc(UUID positionId);
}
