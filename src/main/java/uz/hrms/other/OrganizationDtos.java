package uz.hrms;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record DepartmentRequest(
    @NotBlank String code,
    @NotBlank String name,
    @NotNull DepartmentUnitType unitType,
    UUID parentDepartmentId,
    UUID managerEmployeeId,
    String phone,
    String email,
    String location,
    Boolean active
) {
}

record DepartmentResponse(
    UUID id,
    String code,
    String name,
    DepartmentUnitType unitType,
    UUID parentDepartmentId,
    String parentDepartmentName,
    UUID managerEmployeeId,
    String managerPersonnelNumber,
    String phone,
    String email,
    String location,
    boolean active,
    OffsetDateTime closedAt,
    long directChildrenCount,
    long staffingUnitsCount,
    long vacanciesCount
) {
}

record DepartmentTreeNode(
    UUID id,
    String code,
    String name,
    DepartmentUnitType unitType,
    boolean active,
    String managerPersonnelNumber,
    List<DepartmentTreeNode> children
) {
}

record PositionRequest(
    @NotBlank String code,
    @NotBlank String title,
    String category,
    Boolean active
) {
}

record PositionResponse(
    UUID id,
    String code,
    String title,
    String category,
    boolean active,
    long staffingUnitsCount,
    long vacanciesCount
) {
}

record StaffingUnitRequest(
    @NotBlank String code,
    @NotNull UUID departmentId,
    @NotNull UUID positionId,
    @NotNull @DecimalMin("0.01") BigDecimal plannedFte,
    @NotNull StaffingUnitStatus status,
    LocalDate openedAt,
    LocalDate closedAt,
    String notes
) {
}

record StaffingUnitResponse(
    UUID id,
    String code,
    UUID departmentId,
    String departmentName,
    UUID positionId,
    String positionTitle,
    BigDecimal plannedFte,
    BigDecimal occupiedFte,
    BigDecimal vacantFte,
    StaffingUnitStatus status,
    LocalDate openedAt,
    LocalDate closedAt,
    String notes
) {
}

record StaffingFilterResponse(
    List<StaffingUnitResponse> items,
    long total
) {
}

record VacancyResponse(
    UUID staffingUnitId,
    String staffingCode,
    UUID departmentId,
    String departmentName,
    UUID positionId,
    String positionTitle,
    BigDecimal plannedFte,
    BigDecimal occupiedFte,
    BigDecimal vacantFte
) {
}

record AssignmentRequest(
    @NotNull UUID employeeId,
    @NotNull UUID departmentId,
    @NotNull UUID positionId,
    @NotNull UUID staffingUnitId,
    UUID managerEmployeeId,
    @NotNull LocalDate startedAt,
    LocalDate endedAt
) {
}

record AssignmentResponse(
    UUID id,
    UUID employeeId,
    UUID departmentId,
    UUID positionId,
    UUID staffingUnitId,
    UUID managerEmployeeId,
    LocalDate startedAt,
    LocalDate endedAt
) {
}

record HistoryResponse(
    Integer versionNo,
    String actionType,
    OffsetDateTime changedAt,
    String payloadJson
) {
}
