package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    @Query("""
            select rp
            from RolePermission rp
            join fetch rp.role r
            join fetch rp.permission p
            where rp.deleted = false
              and r.id in :roleIds
              and r.deleted = false
              and p.deleted = false
            """)
    List<RolePermission> findAllByRoleIds(@Param("roleIds") List<UUID> roleIds);
}
