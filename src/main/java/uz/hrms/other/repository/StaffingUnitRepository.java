package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.hrms.other.entity.StaffingUnit;
import uz.hrms.other.enums.StaffingUnitStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffingUnitRepository extends JpaRepository<StaffingUnit, UUID> {
    Optional<StaffingUnit> findByIdAndDeletedFalse(UUID id);

    List<StaffingUnit> findAllByDeletedFalseOrderByCodeAsc();

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdAndDeletedFalse(String code, UUID id);

    @Query(
            "select s from StaffingUnit s " +
                    "join fetch s.department d " +
                    "join fetch s.position p " +
                    "where s.deleted = false " +
                    "and (:departmentIds is null or d.id in :departmentIds) " +
                    "and (:positionId is null or p.id = :positionId) " +
                    "and (:status is null or s.status = :status) " +
                    "order by d.name asc, p.title asc, s.code asc"
    )
    List<StaffingUnit> search(
            @Param("departmentIds") List<UUID> departmentIds,
            @Param("positionId") UUID positionId,
            @Param("status") StaffingUnitStatus status
    );

    @Query(
            "select s from StaffingUnit s " +
                    "join fetch s.department d " +
                    "join fetch s.position p " +
                    "where s.deleted = false " +
                    "and s.status = uz.hrms.other.enums.StaffingUnitStatus.ACTIVE " +
                    "and s.occupiedFte < s.plannedFte " +
                    "and (:departmentIds is null or d.id in :departmentIds) " +
                    "and (:positionId is null or p.id = :positionId) " +
                    "order by d.name asc, p.title asc, s.code asc"
    )
    List<StaffingUnit> findVacancies(
            @Param("departmentIds") List<UUID> departmentIds,
            @Param("positionId") UUID positionId
    );
}
