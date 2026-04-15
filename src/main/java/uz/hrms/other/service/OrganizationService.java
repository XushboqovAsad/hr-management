package uz.hrms.other.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.DepartmentHistoryRepository;
import uz.hrms.other.PositionHistoryRepository;
import uz.hrms.other.StaffingUnitHistoryRepository;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.repository.AuditLogRepository;

@Service
@Validated
@Transactional
class OrganizationService {

    private final uz.hrms.other.repository.DepartmentRepository departmentRepository;
    private final uz.hrms.other.repository.PositionRepository positionRepository;
    private final uz.hrms.other.repository.StaffingUnitRepository staffingUnitRepository;
    private final uz.hrms.other.repository.EmployeeRepository employeeRepository;
    private final uz.hrms.other.repository.EmployeeAssignmentRepository employeeAssignmentRepository;
    private final DepartmentHistoryRepository departmentHistoryRepository;
    private final PositionHistoryRepository positionHistoryRepository;
    private final StaffingUnitHistoryRepository staffingUnitHistoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    OrganizationService(
        DepartmentRepository departmentRepository,
        PositionRepository positionRepository,
        StaffingUnitRepository staffingUnitRepository,
        EmployeeRepository employeeRepository,
        EmployeeAssignmentRepository employeeAssignmentRepository,
        DepartmentHistoryRepository departmentHistoryRepository,
        PositionHistoryRepository positionHistoryRepository,
        StaffingUnitHistoryRepository staffingUnitHistoryRepository,
        AuditLogRepository auditLogRepository,
        ObjectMapper objectMapper
    ) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.staffingUnitRepository = staffingUnitRepository;
        this.employeeRepository = employeeRepository;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.departmentHistoryRepository = departmentHistoryRepository;
        this.positionHistoryRepository = positionHistoryRepository;
        this.staffingUnitHistoryRepository = staffingUnitHistoryRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    List<DepartmentResponse> getDepartments() {
        List<Department> departments = departmentRepository.findAllByDeletedFalseOrderByNameAsc();
        List<StaffingUnit> staffingUnits = staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc();
        return departments.stream().map(department -> toDepartmentResponse(department, departments, staffingUnits)).toList();
    }

    @Transactional(readOnly = true)
    DepartmentResponse getDepartment(UUID departmentId) {
        Department department = getDepartmentEntity(departmentId);
        List<Department> departments = departmentRepository.findAllByDeletedFalseOrderByNameAsc();
        List<StaffingUnit> staffingUnits = staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc();
        return toDepartmentResponse(department, departments, staffingUnits);
    }

    DepartmentResponse createDepartment(DepartmentRequest request) {
        validateDepartmentRequest(request, null);
        Department department = new Department();
        applyDepartment(department, request);
        Department saved = departmentRepository.save(department);
        writeDepartmentHistory(saved, "CREATED");
        writeAudit("DEPARTMENT_CREATED", "departments", saved.getId(), null, departmentSnapshot(saved));
        return getDepartment(saved.getId());
    }

    DepartmentResponse updateDepartment(UUID departmentId, DepartmentRequest request) {
        validateDepartmentRequest(request, departmentId);
        Department department = getDepartmentEntity(departmentId);
        String before = departmentSnapshot(department);
        applyDepartment(department, request);
        Department saved = departmentRepository.save(department);
        writeDepartmentHistory(saved, "UPDATED");
        writeAudit("DEPARTMENT_UPDATED", "departments", saved.getId(), before, departmentSnapshot(saved));
        return getDepartment(saved.getId());
    }

    @Transactional(readOnly = true)
    List<DepartmentTreeNode> getDepartmentTree() {
        List<Department> departments = departmentRepository.findAllByDeletedFalseOrderByNameAsc();
        Map<UUID, DepartmentTreeNodeMutable> nodes = new HashMap<>();
        for (Department department : departments) {
            nodes.put(department.getId(), new DepartmentTreeNodeMutable(department));
        }
        List<DepartmentTreeNodeMutable> roots = new ArrayList<>();
        for (Department department : departments) {
            DepartmentTreeNodeMutable current = nodes.get(department.getId());
            if (department.getParentDepartment() == null) {
                roots.add(current);
            } else {
                DepartmentTreeNodeMutable parent = nodes.get(department.getParentDepartment().getId());
                if (parent == null) {
                    roots.add(current);
                } else {
                    parent.children().add(current);
                }
            }
        }
        roots.sort(Comparator.comparing(node -> node.department().getName(), String.CASE_INSENSITIVE_ORDER));
        return roots.stream().map(DepartmentTreeNodeMutable::toResponse).toList();
    }

    @Transactional(readOnly = true)
    List<HistoryResponse> getDepartmentHistory(UUID departmentId) {
        getDepartmentEntity(departmentId);
        return departmentHistoryRepository.findAllByDepartmentIdOrderByVersionNoDesc(departmentId)
            .stream()
            .map(history -> new HistoryResponse(history.getVersionNo(), history.getActionType(), history.getChangedAt(), history.getPayloadJson()))
            .toList();
    }

    @Transactional(readOnly = true)
    List<PositionResponse> getPositions() {
        List<Position> positions = positionRepository.findAllByDeletedFalseOrderByTitleAsc();
        List<StaffingUnit> staffingUnits = staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc();
        return positions.stream().map(position -> toPositionResponse(position, staffingUnits)).toList();
    }

    @Transactional(readOnly = true)
    PositionResponse getPosition(UUID positionId) {
        Position position = getPositionEntity(positionId);
        List<StaffingUnit> staffingUnits = staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc();
        return toPositionResponse(position, staffingUnits);
    }

    PositionResponse createPosition(PositionRequest request) {
        validatePositionRequest(request, null);
        Position position = new Position();
        applyPosition(position, request);
        Position saved = positionRepository.save(position);
        writePositionHistory(saved, "CREATED");
        writeAudit("POSITION_CREATED", "positions", saved.getId(), null, positionSnapshot(saved));
        return getPosition(saved.getId());
    }

    PositionResponse updatePosition(UUID positionId, PositionRequest request) {
        validatePositionRequest(request, positionId);
        Position position = getPositionEntity(positionId);
        String before = positionSnapshot(position);
        applyPosition(position, request);
        Position saved = positionRepository.save(position);
        writePositionHistory(saved, "UPDATED");
        writeAudit("POSITION_UPDATED", "positions", saved.getId(), before, positionSnapshot(saved));
        return getPosition(saved.getId());
    }

    @Transactional(readOnly = true)
    List<HistoryResponse> getPositionHistory(UUID positionId) {
        getPositionEntity(positionId);
        return positionHistoryRepository.findAllByPositionIdOrderByVersionNoDesc(positionId)
            .stream()
            .map(history -> new HistoryResponse(history.getVersionNo(), history.getActionType(), history.getChangedAt(), history.getPayloadJson()))
            .toList();
    }

    @Transactional(readOnly = true)
    StaffingFilterResponse getStaffingUnits(UUID branchId, UUID departmentId, UUID positionId, StaffingUnitStatus status) {
        List<UUID> departmentScope = resolveScope(branchId, departmentId);
        List<StaffingUnitResponse> items = staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc()
            .stream()
            .filter(unit -> departmentScope == null || departmentScope.contains(unit.getDepartment().getId()))
            .filter(unit -> positionId == null || Objects.equals(unit.getPosition().getId(), positionId))
            .filter(unit -> status == null || unit.getStatus() == status)
            .sorted(Comparator.comparing((StaffingUnit unit) -> unit.getDepartment().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(unit -> unit.getPosition().getTitle(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(StaffingUnit::getCode, String.CASE_INSENSITIVE_ORDER))
            .map(this::toStaffingResponse)
            .toList();
        return new StaffingFilterResponse(items, items.size());
    }

    @Transactional(readOnly = true)
    StaffingUnitResponse getStaffingUnit(UUID staffingUnitId) {
        return toStaffingResponse(getStaffingUnitEntity(staffingUnitId));
    }

    StaffingUnitResponse createStaffingUnit(StaffingUnitRequest request) {
        validateStaffingRequest(request, null);
        StaffingUnit staffingUnit = new StaffingUnit();
        applyStaffingUnit(staffingUnit, request);
        StaffingUnit saved = staffingUnitRepository.save(staffingUnit);
        writeStaffingHistory(saved, "CREATED");
        writeAudit("STAFFING_UNIT_CREATED", "staffing_units", saved.getId(), null, staffingSnapshot(saved));
        return getStaffingUnit(saved.getId());
    }

    StaffingUnitResponse updateStaffingUnit(UUID staffingUnitId, StaffingUnitRequest request) {
        validateStaffingRequest(request, staffingUnitId);
        StaffingUnit staffingUnit = getStaffingUnitEntity(staffingUnitId);
        String before = staffingSnapshot(staffingUnit);
        applyStaffingUnit(staffingUnit, request);
        refreshOccupiedFte(staffingUnit);
        StaffingUnit saved = staffingUnitRepository.save(staffingUnit);
        writeStaffingHistory(saved, "UPDATED");
        writeAudit("STAFFING_UNIT_UPDATED", "staffing_units", saved.getId(), before, staffingSnapshot(saved));
        return getStaffingUnit(saved.getId());
    }

    @Transactional(readOnly = true)
    List<HistoryResponse> getStaffingUnitHistory(UUID staffingUnitId) {
        getStaffingUnitEntity(staffingUnitId);
        return staffingUnitHistoryRepository.findAllByStaffingUnitIdOrderByVersionNoDesc(staffingUnitId)
            .stream()
            .map(history -> new HistoryResponse(history.getVersionNo(), history.getActionType(), history.getChangedAt(), history.getPayloadJson()))
            .toList();
    }

    @Transactional(readOnly = true)
    StaffingFilterResponse getDepartmentStaffing(UUID departmentId, StaffingUnitStatus status) {
        getDepartmentEntity(departmentId);
        List<StaffingUnitResponse> items = staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc()
            .stream()
            .filter(unit -> Objects.equals(unit.getDepartment().getId(), departmentId))
            .filter(unit -> status == null || unit.getStatus() == status)
            .sorted(Comparator.comparing((StaffingUnit unit) -> unit.getPosition().getTitle(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(StaffingUnit::getCode, String.CASE_INSENSITIVE_ORDER))
            .map(this::toStaffingResponse)
            .toList();
        return new StaffingFilterResponse(items, items.size());
    }

    @Transactional(readOnly = true)
    List<VacancyResponse> getVacancies(UUID branchId, UUID departmentId, UUID positionId) {
        List<UUID> departmentScope = resolveScope(branchId, departmentId);
        return staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc()
            .stream()
            .filter(unit -> unit.getStatus() == StaffingUnitStatus.ACTIVE)
            .filter(unit -> unit.getOccupiedFte().compareTo(unit.getPlannedFte()) < 0)
            .filter(unit -> departmentScope == null || departmentScope.contains(unit.getDepartment().getId()))
            .filter(unit -> positionId == null || Objects.equals(unit.getPosition().getId(), positionId))
            .sorted(Comparator.comparing((StaffingUnit unit) -> unit.getDepartment().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(unit -> unit.getPosition().getTitle(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(StaffingUnit::getCode, String.CASE_INSENSITIVE_ORDER))
            .map(unit -> new VacancyResponse(
                unit.getId(),
                unit.getCode(),
                unit.getDepartment().getId(),
                unit.getDepartment().getName(),
                unit.getPosition().getId(),
                unit.getPosition().getTitle(),
                unit.getPlannedFte(),
                unit.getOccupiedFte(),
                unit.getPlannedFte().subtract(unit.getOccupiedFte())
            ))
            .toList();
    }

    AssignmentResponse createAssignment(AssignmentRequest request) {
        validateAssignmentRequest(request, null);
        EmployeeAssignment assignment = new EmployeeAssignment();
        applyAssignment(assignment, request);
        EmployeeAssignment saved = employeeAssignmentRepository.save(assignment);
        refreshOccupiedFte(saved.getStaffingUnit());
        writeAudit("EMPLOYEE_ASSIGNMENT_CREATED", "employee_assignments", saved.getId(), null, assignmentSnapshot(saved));
        return toAssignmentResponse(saved);
    }

    AssignmentResponse updateAssignment(UUID assignmentId, AssignmentRequest request) {
        validateAssignmentRequest(request, assignmentId);
        EmployeeAssignment assignment = employeeAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        String before = assignmentSnapshot(assignment);
        StaffingUnit oldStaffingUnit = assignment.getStaffingUnit();
        applyAssignment(assignment, request);
        EmployeeAssignment saved = employeeAssignmentRepository.save(assignment);
        if (oldStaffingUnit == null) {
        } else {
            refreshOccupiedFte(oldStaffingUnit);
        }
        refreshOccupiedFte(saved.getStaffingUnit());
        writeAudit("EMPLOYEE_ASSIGNMENT_UPDATED", "employee_assignments", saved.getId(), before, assignmentSnapshot(saved));
        return toAssignmentResponse(saved);
    }

    private void validateDepartmentRequest(DepartmentRequest request, UUID currentId) {
        List<Department> departments = departmentRepository.findAllByDeletedFalseOrderByNameAsc();
        boolean duplicateCode = departments.stream()
            .anyMatch(item -> item.getCode().equalsIgnoreCase(request.code()) && Objects.equals(item.getId(), currentId) == false);
        if (duplicateCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department code already exists");
        }
        UUID parentId = request.parentDepartmentId();
        boolean duplicateName = departments.stream()
            .anyMatch(item -> {
                UUID itemParentId = item.getParentDepartment() == null ? null : item.getParentDepartment().getId();
                return item.getName().equalsIgnoreCase(request.name())
                    && Objects.equals(itemParentId, parentId)
                    && Objects.equals(item.getId(), currentId) == false;
            });
        if (duplicateName) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name already exists in selected parent");
        }
        if (currentId != null && Objects.equals(currentId, parentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department cannot be its own parent");
        }
        if (parentId != null) {
            Department parent = getDepartmentEntity(parentId);
            if (currentId != null && isChildOf(parent, currentId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Circular department hierarchy is not allowed");
            }
        }
        if (request.managerEmployeeId() != null) {
            getEmployeeEntity(request.managerEmployeeId());
        }
    }

    private void validatePositionRequest(PositionRequest request, UUID currentId) {
        boolean duplicateCode = positionRepository.findAllByDeletedFalseOrderByTitleAsc()
            .stream()
            .anyMatch(item -> item.getCode().equalsIgnoreCase(request.code()) && Objects.equals(item.getId(), currentId) == false);
        if (duplicateCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position code already exists");
        }
    }

    private void validateStaffingRequest(StaffingUnitRequest request, UUID currentId) {
        List<StaffingUnit> units = staffingUnitRepository.findAllByDeletedFalseOrderByCodeAsc();
        boolean duplicateCode = units.stream()
            .anyMatch(item -> item.getCode().equalsIgnoreCase(request.code()) && Objects.equals(item.getId(), currentId) == false);
        if (duplicateCode) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staffing unit code already exists");
        }
        Department department = getDepartmentEntity(request.departmentId());
        if (department.isActive() == false || department.getClosedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Closed department cannot own staffing unit");
        }
        Position position = getPositionEntity(request.positionId());
        if (position.isActive() == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactive position cannot be used in staffing");
        }
        if (request.closedAt() != null && request.openedAt() != null && request.closedAt().isBefore(request.openedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Closed date cannot be before opened date");
        }
    }

    private void validateAssignmentRequest(AssignmentRequest request, UUID assignmentId) {
        getEmployeeEntity(request.employeeId());
        Department department = getDepartmentEntity(request.departmentId());
        Position position = getPositionEntity(request.positionId());
        StaffingUnit staffingUnit = getStaffingUnitEntity(request.staffingUnitId());
        if (department.isActive() == false || department.getClosedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee cannot be assigned to closed department");
        }
        if (Objects.equals(staffingUnit.getDepartment().getId(), department.getId()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staffing unit belongs to another department");
        }
        if (Objects.equals(staffingUnit.getPosition().getId(), position.getId()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staffing unit belongs to another position");
        }
        if (staffingUnit.getStatus() != StaffingUnitStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee cannot be assigned to inactive staffing unit");
        }
        long occupied = employeeAssignmentRepository.countByStaffingUnitIdAndDeletedFalseAndEndedAtIsNull(staffingUnit.getId());
        if (assignmentId == null) {
            BigDecimal nextOccupied = BigDecimal.valueOf(occupied + 1L);
            if (nextOccupied.compareTo(staffingUnit.getPlannedFte()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staffing unit has no vacant slots");
            }
        }
        if (request.managerEmployeeId() != null) {
            getEmployeeEntity(request.managerEmployeeId());
        }
        if (request.endedAt() != null && request.endedAt().isBefore(request.startedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment end date cannot be before start date");
        }
    }

    private void applyDepartment(Department department, DepartmentRequest request) {
        Department parent = request.parentDepartmentId() == null ? null : getDepartmentEntity(request.parentDepartmentId());
        Employee manager = request.managerEmployeeId() == null ? null : getEmployeeEntity(request.managerEmployeeId());
        department.setCode(request.code().trim());
        department.setName(request.name().trim());
        department.setUnitType(request.unitType());
        department.setParentDepartment(parent);
        department.setManagerEmployee(manager);
        department.setPhone(trimToNull(request.phone()));
        department.setEmail(trimToNull(request.email()));
        department.setLocation(trimToNull(request.location()));
        boolean active = request.active() == null || request.active();
        department.setActive(active);
        department.setClosedAt(active ? null : OffsetDateTime.now());
    }

    private void applyPosition(Position position, PositionRequest request) {
        position.setCode(request.code().trim());
        position.setTitle(request.title().trim());
        position.setCategory(trimToNull(request.category()));
        position.setActive(request.active() == null || request.active());
    }

    private void applyStaffingUnit(StaffingUnit staffingUnit, StaffingUnitRequest request) {
        staffingUnit.setCode(request.code().trim());
        staffingUnit.setDepartment(getDepartmentEntity(request.departmentId()));
        staffingUnit.setPosition(getPositionEntity(request.positionId()));
        staffingUnit.setPlannedFte(request.plannedFte());
        staffingUnit.setStatus(request.status());
        staffingUnit.setOpenedAt(request.openedAt());
        staffingUnit.setClosedAt(request.closedAt());
        staffingUnit.setNotes(trimToNull(request.notes()));
        refreshOccupiedFte(staffingUnit);
    }

    private void applyAssignment(EmployeeAssignment assignment, AssignmentRequest request) {
        assignment.setEmployee(getEmployeeEntity(request.employeeId()));
        assignment.setDepartment(getDepartmentEntity(request.departmentId()));
        assignment.setPosition(getPositionEntity(request.positionId()));
        assignment.setStaffingUnit(getStaffingUnitEntity(request.staffingUnitId()));
        assignment.setManagerEmployee(request.managerEmployeeId() == null ? null : getEmployeeEntity(request.managerEmployeeId()));
        assignment.setStartedAt(request.startedAt());
        assignment.setEndedAt(request.endedAt());
    }

    private void refreshOccupiedFte(StaffingUnit staffingUnit) {
        if (staffingUnit == null || staffingUnit.getId() == null) {
            staffingUnit.setOccupiedFte(BigDecimal.ZERO);
            return;
        }
        long occupied = employeeAssignmentRepository.countByStaffingUnitIdAndDeletedFalseAndEndedAtIsNull(staffingUnit.getId());
        staffingUnit.setOccupiedFte(BigDecimal.valueOf(occupied));
    }

    private DepartmentResponse toDepartmentResponse(Department department, List<Department> departments, List<StaffingUnit> staffingUnits) {
        long directChildrenCount = departments.stream()
            .filter(item -> item.getParentDepartment() != null)
            .filter(item -> Objects.equals(item.getParentDepartment().getId(), department.getId()))
            .count();
        long staffingUnitsCount = staffingUnits.stream()
            .filter(item -> Objects.equals(item.getDepartment().getId(), department.getId()))
            .count();
        long vacanciesCount = staffingUnits.stream()
            .filter(item -> Objects.equals(item.getDepartment().getId(), department.getId()))
            .filter(item -> item.getStatus() == StaffingUnitStatus.ACTIVE)
            .filter(item -> item.getOccupiedFte().compareTo(item.getPlannedFte()) < 0)
            .count();
        return new DepartmentResponse(
            department.getId(),
            department.getCode(),
            department.getName(),
            department.getUnitType(),
            department.getParentDepartment() == null ? null : department.getParentDepartment().getId(),
            department.getParentDepartment() == null ? null : department.getParentDepartment().getName(),
            department.getManagerEmployee() == null ? null : department.getManagerEmployee().getId(),
            department.getManagerEmployee() == null ? null : department.getManagerEmployee().getPersonnelNumber(),
            department.getPhone(),
            department.getEmail(),
            department.getLocation(),
            department.isActive(),
            department.getClosedAt(),
            directChildrenCount,
            staffingUnitsCount,
            vacanciesCount
        );
    }

    private PositionResponse toPositionResponse(Position position, List<StaffingUnit> staffingUnits) {
        long staffingUnitsCount = staffingUnits.stream()
            .filter(item -> Objects.equals(item.getPosition().getId(), position.getId()))
            .count();
        long vacanciesCount = staffingUnits.stream()
            .filter(item -> Objects.equals(item.getPosition().getId(), position.getId()))
            .filter(item -> item.getStatus() == StaffingUnitStatus.ACTIVE)
            .filter(item -> item.getOccupiedFte().compareTo(item.getPlannedFte()) < 0)
            .count();
        return new PositionResponse(
            position.getId(),
            position.getCode(),
            position.getTitle(),
            position.getCategory(),
            position.isActive(),
            staffingUnitsCount,
            vacanciesCount
        );
    }

    private StaffingUnitResponse toStaffingResponse(StaffingUnit staffingUnit) {
        BigDecimal vacantFte = staffingUnit.getPlannedFte().subtract(staffingUnit.getOccupiedFte());
        if (vacantFte.compareTo(BigDecimal.ZERO) < 0) {
            vacantFte = BigDecimal.ZERO;
        }
        return new StaffingUnitResponse(
            staffingUnit.getId(),
            staffingUnit.getCode(),
            staffingUnit.getDepartment().getId(),
            staffingUnit.getDepartment().getName(),
            staffingUnit.getPosition().getId(),
            staffingUnit.getPosition().getTitle(),
            staffingUnit.getPlannedFte(),
            staffingUnit.getOccupiedFte(),
            vacantFte,
            staffingUnit.getStatus(),
            staffingUnit.getOpenedAt(),
            staffingUnit.getClosedAt(),
            staffingUnit.getNotes()
        );
    }

    private AssignmentResponse toAssignmentResponse(EmployeeAssignment assignment) {
        return new AssignmentResponse(
            assignment.getId(),
            assignment.getEmployee().getId(),
            assignment.getDepartment().getId(),
            assignment.getPosition() == null ? null : assignment.getPosition().getId(),
            assignment.getStaffingUnit() == null ? null : assignment.getStaffingUnit().getId(),
            assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
            assignment.getStartedAt(),
            assignment.getEndedAt()
        );
    }

    private List<UUID> resolveScope(UUID branchId, UUID departmentId) {
        if (departmentId != null) {
            return collectDepartmentIds(departmentId);
        }
        if (branchId != null) {
            return collectDepartmentIds(branchId);
        }
        return null;
    }

    private List<UUID> collectDepartmentIds(UUID rootDepartmentId) {
        List<Department> departments = departmentRepository.findAllByDeletedFalseOrderByNameAsc();
        Set<UUID> ids = new HashSet<>();
        ids.add(rootDepartmentId);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Department department : departments) {
                if (department.getParentDepartment() != null && ids.contains(department.getParentDepartment().getId())) {
                    if (ids.add(department.getId())) {
                        changed = true;
                    }
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private boolean isChildOf(Department parentCandidate, UUID searchedChildId) {
        Department current = parentCandidate;
        while (current != null) {
            if (Objects.equals(current.getId(), searchedChildId)) {
                return true;
            }
            current = current.getParentDepartment();
        }
        return false;
    }

    private Department getDepartmentEntity(UUID departmentId) {
        return departmentRepository.findByIdAndDeletedFalse(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private Position getPositionEntity(UUID positionId) {
        return positionRepository.findByIdAndDeletedFalse(positionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Position not found"));
    }

    private StaffingUnit getStaffingUnitEntity(UUID staffingUnitId) {
        StaffingUnit staffingUnit = staffingUnitRepository.findByIdAndDeletedFalse(staffingUnitId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staffing unit not found"));
        refreshOccupiedFte(staffingUnit);
        return staffingUnit;
    }

    private Employee getEmployeeEntity(UUID employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void writeDepartmentHistory(Department department, String actionType) {
        DepartmentHistory history = new DepartmentHistory();
        history.setDepartment(department);
        history.setVersionNo((int) departmentHistoryRepository.countByDepartmentId(department.getId()) + 1);
        history.setActionType(actionType);
        history.setCode(department.getCode());
        history.setName(department.getName());
        history.setUnitType(department.getUnitType().name());
        history.setParentDepartmentId(department.getParentDepartment() == null ? null : department.getParentDepartment().getId());
        history.setManagerEmployeeId(department.getManagerEmployee() == null ? null : department.getManagerEmployee().getId());
        history.setActive(department.isActive());
        history.setChangedAt(OffsetDateTime.now());
        history.setPayloadJson(departmentSnapshot(department));
        departmentHistoryRepository.save(history);
    }

    private void writePositionHistory(Position position, String actionType) {
        PositionHistory history = new PositionHistory();
        history.setPosition(position);
        history.setVersionNo((int) positionHistoryRepository.countByPositionId(position.getId()) + 1);
        history.setActionType(actionType);
        history.setCode(position.getCode());
        history.setTitle(position.getTitle());
        history.setCategory(position.getCategory());
        history.setActive(position.isActive());
        history.setChangedAt(OffsetDateTime.now());
        history.setPayloadJson(positionSnapshot(position));
        positionHistoryRepository.save(history);
    }

    private void writeStaffingHistory(StaffingUnit staffingUnit, String actionType) {
        StaffingUnitHistory history = new StaffingUnitHistory();
        history.setStaffingUnit(staffingUnit);
        history.setVersionNo((int) staffingUnitHistoryRepository.countByStaffingUnitId(staffingUnit.getId()) + 1);
        history.setActionType(actionType);
        history.setCode(staffingUnit.getCode());
        history.setDepartment(staffingUnit.getDepartment());
        history.setPosition(staffingUnit.getPosition());
        history.setPlannedFte(staffingUnit.getPlannedFte());
        history.setOccupiedFte(staffingUnit.getOccupiedFte());
        history.setStatus(staffingUnit.getStatus().name());
        history.setOpenedAt(staffingUnit.getOpenedAt());
        history.setClosedAt(staffingUnit.getClosedAt());
        history.setChangedAt(OffsetDateTime.now());
        history.setPayloadJson(staffingSnapshot(staffingUnit));
        staffingUnitHistoryRepository.save(history);
    }

    private String departmentSnapshot(Department department) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", department.getId());
        payload.put("code", department.getCode());
        payload.put("name", department.getName());
        payload.put("unitType", department.getUnitType());
        payload.put("parentDepartmentId", department.getParentDepartment() == null ? null : department.getParentDepartment().getId());
        payload.put("managerEmployeeId", department.getManagerEmployee() == null ? null : department.getManagerEmployee().getId());
        payload.put("active", department.isActive());
        payload.put("closedAt", department.getClosedAt());
        return toJson(payload);
    }

    private String positionSnapshot(Position position) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", position.getId());
        payload.put("code", position.getCode());
        payload.put("title", position.getTitle());
        payload.put("category", position.getCategory());
        payload.put("active", position.isActive());
        return toJson(payload);
    }

    private String staffingSnapshot(StaffingUnit staffingUnit) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", staffingUnit.getId());
        payload.put("code", staffingUnit.getCode());
        payload.put("departmentId", staffingUnit.getDepartment().getId());
        payload.put("positionId", staffingUnit.getPosition().getId());
        payload.put("plannedFte", staffingUnit.getPlannedFte());
        payload.put("occupiedFte", staffingUnit.getOccupiedFte());
        payload.put("status", staffingUnit.getStatus());
        payload.put("openedAt", staffingUnit.getOpenedAt());
        payload.put("closedAt", staffingUnit.getClosedAt());
        payload.put("notes", staffingUnit.getNotes());
        return toJson(payload);
    }

    private String assignmentSnapshot(EmployeeAssignment assignment) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", assignment.getId());
        payload.put("employeeId", assignment.getEmployee().getId());
        payload.put("departmentId", assignment.getDepartment().getId());
        payload.put("positionId", assignment.getPosition() == null ? null : assignment.getPosition().getId());
        payload.put("staffingUnitId", assignment.getStaffingUnit() == null ? null : assignment.getStaffingUnit().getId());
        payload.put("managerEmployeeId", assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId());
        payload.put("startedAt", assignment.getStartedAt());
        payload.put("endedAt", assignment.getEndedAt());
        return toJson(payload);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize audit payload");
        }
    }

    private void writeAudit(String action, String entityTable, UUID entityId, String beforeData, String afterData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable(entityTable);
        auditLog.setEntityId(entityId);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLog.setBeforeData(beforeData);
        auditLog.setAfterData(afterData);
        auditLogRepository.save(auditLog);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private record DepartmentTreeNodeMutable(Department department, List<DepartmentTreeNodeMutable> children) {
        private DepartmentTreeNodeMutable(Department department) {
            this(department, new ArrayList<>());
        }

        private DepartmentTreeNode toResponse() {
            List<DepartmentTreeNode> childNodes = children.stream()
                .sorted(Comparator.comparing(node -> node.department().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(DepartmentTreeNodeMutable::toResponse)
                .toList();
            return new DepartmentTreeNode(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.getUnitType(),
                department.isActive(),
                department.getManagerEmployee() == null ? null : department.getManagerEmployee().getPersonnelNumber(),
                childNodes
            );
        }
    }
}
