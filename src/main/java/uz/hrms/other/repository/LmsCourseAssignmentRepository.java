package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LmsCourseAssignmentRepository extends JpaRepository<LmsCourseAssignment, UUID> {
    Optional<LmsCourseAssignment> findByIdAndDeletedFalse(UUID id);

    @Query(
            "select a from LmsCourseAssignment a join fetch a.course c left join fetch a.currentDepartment d left join fetch a.currentPosition p " +
                    "where a.deleted = false and (:employeeId is null or a.employee.id = :employeeId) " +
                    "and (:status is null or a.status = :status) " +
                    "and (:departmentId is null or d.id = :departmentId) " +
                    "and (:positionId is null or p.id = :positionId) " +
                    "and (:dueBefore is null or a.dueDate <= :dueBefore) order by a.assignedAt desc"
    )
    List<LmsCourseAssignment> search(
            @Param("employeeId") UUID employeeId,
            @Param("status") LmsAssignmentStatus status,
            @Param("departmentId") UUID departmentId,
            @Param("positionId") UUID positionId,
            @Param("dueBefore") LocalDate dueBefore
    );

    @Query(
            "select a from LmsCourseAssignment a join fetch a.course c where a.deleted = false and a.employee.id = :employeeId order by a.assignedAt desc"
    )
    List<LmsCourseAssignment> findAllByEmployeeIdAndDeletedFalseOrderByAssignedAtDesc(@Param("employeeId") UUID employeeId);

    @Query(
            "select case when count(a) > 0 then true else false end from LmsCourseAssignment a where a.deleted = false and a.employee.id = :employeeId and a.course.id = :courseId and a.status in :statuses"
    )
    boolean existsByEmployeeIdAndCourseIdAndStatuses(
            @Param("employeeId") UUID employeeId,
            @Param("courseId") UUID courseId,
            @Param("statuses") Collection<LmsAssignmentStatus> statuses
    );

    List<LmsCourseAssignment> findAllByStatusInAndDueDateBeforeAndDeletedFalse(Collection<LmsAssignmentStatus> statuses, LocalDate dueDate);
}
