package uz.hrms.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClearanceChecklistItemRepository extends JpaRepository<ClearanceChecklistItem, UUID> {
    Optional<ClearanceChecklistItem> findByIdAndDeletedFalse(UUID id);

    List<ClearanceChecklistItem> findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(UUID checklistId);
}
