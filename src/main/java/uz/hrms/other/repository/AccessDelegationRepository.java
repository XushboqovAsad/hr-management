package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.hrms.other.entity.AccessDelegation;

import java.util.List;
import java.util.UUID;

public interface AccessDelegationRepository extends JpaRepository<AccessDelegation, UUID> {

    @Query("""
            select ad
            from AccessDelegation ad
            where ad.deleted = false
              and ad.granteeUser.id = :granteeUserId
              and ad.moduleCode = :moduleCode
              and ad.actionCode = :actionCode
              and ad.active = true
            """)
    List<AccessDelegation> findActiveDelegations(@Param("granteeUserId") UUID granteeUserId,
                                                 @Param("moduleCode") String moduleCode,
                                                 @Param("actionCode") String actionCode);
}
