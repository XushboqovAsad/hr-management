package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeSelfServiceEmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query("select e from Employee e where e.deleted = false and e.user.id = :userId")
    Optional<Employee> findByUserIdAndDeletedFalse(@Param("userId") UUID userId);

    @Query(
            "select distinct e from Employee e " +
                    "left join fetch e.user u " +
                    "where e.deleted = false " +
                    "and e.employmentStatus in ('ACTIVE', 'ONBOARDING', 'ON_LEAVE') " +
                    "order by u.lastName asc, u.firstName asc"
    )
    List<Employee> findAllActiveForDirectory();

    @Query(
            "select distinct e from Employee e " +
                    "left join fetch e.user u " +
                    "where e.deleted = false " +
                    "and e.employmentStatus in ('ACTIVE', 'ONBOARDING', 'ON_LEAVE') " +
                    "and (:query is null or lower(coalesce(u.lastName, '')) like lower(concat('%', :query, '%')) " +
                    "or lower(coalesce(u.firstName, '')) like lower(concat('%', :query, '%')) " +
                    "or lower(coalesce(u.middleName, '')) like lower(concat('%', :query, '%')) " +
                    "or lower(coalesce(u.email, '')) like lower(concat('%', :query, '%')) " +
                    "or lower(e.personnelNumber) like lower(concat('%', :query, '%'))) " +
                    "order by u.lastName asc, u.firstName asc"
    )
    List<Employee> searchDirectory(@Param("query") String query);
}
