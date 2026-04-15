package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UUID> {

    @Query("""
            select ura
            from UserRoleAssignment ura
            join fetch ura.role r
            where ura.deleted = false
              and ura.user.id = :userId
              and ura.active = true
              and r.deleted = false
            """)
    List<UserRoleAssignment> findActiveAssignments(@Param("userId") UUID userId);
}
