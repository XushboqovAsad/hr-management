package uz.hrms.other.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.repository.AuditLogRepository;

import java.time.OffsetDateTime;
import java.util.*;

@org.springframework.stereotype.Service
@Validated
public class ReferenceCatalogService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    ReferenceCatalogService(
            NamedParameterJdbcTemplate jdbcTemplate,
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public List<ReferenceCatalogDefinitionResponse> definitions() {
        return List.of(
                new ReferenceCatalogDefinitionResponse(ReferenceCatalogKey.ORDER_TYPES.path(), ReferenceCatalogKey.ORDER_TYPES.label(), ReferenceCatalogKey.ORDER_TYPES.description()),
                new ReferenceCatalogDefinitionResponse(ReferenceCatalogKey.LEAVE_TYPES.path(), ReferenceCatalogKey.LEAVE_TYPES.label(), ReferenceCatalogKey.LEAVE_TYPES.description()),
                new ReferenceCatalogDefinitionResponse(ReferenceCatalogKey.DISCIPLINARY_ACTION_TYPES.path(), ReferenceCatalogKey.DISCIPLINARY_ACTION_TYPES.label(), ReferenceCatalogKey.DISCIPLINARY_ACTION_TYPES.description()),
                new ReferenceCatalogDefinitionResponse(ReferenceCatalogKey.REWARD_TYPES.path(), ReferenceCatalogKey.REWARD_TYPES.label(), ReferenceCatalogKey.REWARD_TYPES.description()),
                new ReferenceCatalogDefinitionResponse(ReferenceCatalogKey.DOCUMENT_TYPES.path(), ReferenceCatalogKey.DOCUMENT_TYPES.label(), ReferenceCatalogKey.DOCUMENT_TYPES.description()),
                new ReferenceCatalogDefinitionResponse(ReferenceCatalogKey.NOTIFICATION_TYPES.path(), ReferenceCatalogKey.NOTIFICATION_TYPES.label(), ReferenceCatalogKey.NOTIFICATION_TYPES.description()),
                new ReferenceCatalogDefinitionResponse(ReferenceCatalogKey.LMS_COURSE_TYPES.path(), ReferenceCatalogKey.LMS_COURSE_TYPES.label(), ReferenceCatalogKey.LMS_COURSE_TYPES.description())
        );
    }

    public List<ReferenceCatalogItemResponse> list(ReferenceCatalogKey catalog) {
        return jdbcTemplate.query(selectSql(catalog), new MapSqlParameterSource(), rowMapper(catalog));
    }

    public ReferenceCatalogItemResponse create(ReferenceCatalogKey catalog, ReferenceCatalogUpsertRequest request, CurrentUser actor) {
        validate(catalog, request, null);
        UUID id = UUID.randomUUID();
        MapSqlParameterSource params = commonParams(id, request, actor.userId());
        jdbcTemplate.update(insertSql(catalog), enrichParams(catalog, params, request));
        ReferenceCatalogItemResponse created = get(catalog, id);
        writeAudit("REFERENCE_CREATED", catalog, created.id(), null, created, actor);
        return created;
    }

    public ReferenceCatalogItemResponse update(ReferenceCatalogKey catalog, UUID id, ReferenceCatalogUpsertRequest request, CurrentUser actor) {
        ReferenceCatalogItemResponse before = get(catalog, id);
        validate(catalog, request, id);
        MapSqlParameterSource params = commonParams(id, request, actor.userId());
        params.addValue("id", id);
        int updated = jdbcTemplate.update(updateSql(catalog), enrichParams(catalog, params, request));
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reference catalog item not found");
        }
        ReferenceCatalogItemResponse after = get(catalog, id);
        writeAudit("REFERENCE_UPDATED", catalog, id, before, after, actor);
        return after;
    }

    private ReferenceCatalogItemResponse get(ReferenceCatalogKey catalog, UUID id) {
        List<ReferenceCatalogItemResponse> items = jdbcTemplate.query(
                selectSql(catalog) + " and id = :id",
                new MapSqlParameterSource("id", id),
                rowMapper(catalog)
        );
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reference catalog item not found");
        }
        return items.get(0);
    }

    private void validate(ReferenceCatalogKey catalog, ReferenceCatalogUpsertRequest request, UUID existingId) {
        String code = trimToNull(request.code());
        String name = trimToNull(request.name());
        if (code == null || name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code and name are required");
        }
        MapSqlParameterSource duplicateParams = new MapSqlParameterSource()
                .addValue("code", code)
                .addValue("id", existingId);
        Long duplicates = jdbcTemplate.queryForObject(
                "select count(1) from " + tableName(catalog) + " where is_deleted = false and lower(code) = lower(:code) and (:id is null or id <> :id)",
                duplicateParams,
                Long.class
        );
        if (duplicates != null && duplicates > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code already exists in selected catalog");
        }
        if (catalog == ReferenceCatalogKey.NOTIFICATION_TYPES) {
            normalizeChannel(request.attributes());
        }
        if (catalog == ReferenceCatalogKey.DISCIPLINARY_ACTION_TYPES && intValue(request.attributes(), "severityRank", 1) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Severity rank must be positive");
        }
    }

    private String selectSql(ReferenceCatalogKey catalog) {
        return "select id, code, name, description, is_active" + extraSelect(catalog) + " from " + tableName(catalog) + " where is_deleted = false";
    }

    private String insertSql(ReferenceCatalogKey catalog) {
        return switch (catalog) {
            case ORDER_TYPES ->
                    "insert into ref.order_types (id, code, name, description, is_active, created_at, updated_at, created_by, updated_by, is_deleted) " +
                            "values (:id, :code, :name, :description, :active, now(), now(), :actorUserId, :actorUserId, false)";
            case LEAVE_TYPES ->
                    "insert into ref.leave_types (id, code, name, description, is_paid, requires_document, available_for_self_service, is_active, created_at, updated_at, created_by, updated_by, is_deleted) " +
                            "values (:id, :code, :name, :description, :isPaid, :requiresDocument, :availableForSelfService, :active, now(), now(), :actorUserId, :actorUserId, false)";
            case DISCIPLINARY_ACTION_TYPES ->
                    "insert into ref.disciplinary_action_types (id, code, name, description, severity_rank, is_active, created_at, updated_at, created_by, updated_by, is_deleted) " +
                            "values (:id, :code, :name, :description, :severityRank, :active, now(), now(), :actorUserId, :actorUserId, false)";
            case REWARD_TYPES ->
                    "insert into ref.reward_types (id, code, name, description, is_monetary, is_active, created_at, updated_at, created_by, updated_by, is_deleted) " +
                            "values (:id, :code, :name, :description, :isMonetary, :active, now(), now(), :actorUserId, :actorUserId, false)";
            case DOCUMENT_TYPES ->
                    "insert into ref.document_types (id, code, name, description, confidential_by_default, is_active, created_at, updated_at, created_by, updated_by, is_deleted) " +
                            "values (:id, :code, :name, :description, :confidentialByDefault, :active, now(), now(), :actorUserId, :actorUserId, false)";
            case NOTIFICATION_TYPES ->
                    "insert into ref.notification_types (id, code, name, description, default_channel, is_active, created_at, updated_at, created_by, updated_by, is_deleted) " +
                            "values (:id, :code, :name, :description, :defaultChannel, :active, now(), now(), :actorUserId, :actorUserId, false)";
            case LMS_COURSE_TYPES ->
                    "insert into ref.lms_course_types (id, code, name, description, mandatory_by_default, is_active, created_at, updated_at, created_by, updated_by, is_deleted) " +
                            "values (:id, :code, :name, :description, :mandatoryByDefault, :active, now(), now(), :actorUserId, :actorUserId, false)";
        };
    }

    private String updateSql(ReferenceCatalogKey catalog) {
        return switch (catalog) {
            case ORDER_TYPES ->
                    "update ref.order_types set code = :code, name = :name, description = :description, is_active = :active, updated_at = now(), updated_by = :actorUserId where id = :id and is_deleted = false";
            case LEAVE_TYPES ->
                    "update ref.leave_types set code = :code, name = :name, description = :description, is_paid = :isPaid, requires_document = :requiresDocument, available_for_self_service = :availableForSelfService, is_active = :active, updated_at = now(), updated_by = :actorUserId where id = :id and is_deleted = false";
            case DISCIPLINARY_ACTION_TYPES ->
                    "update ref.disciplinary_action_types set code = :code, name = :name, description = :description, severity_rank = :severityRank, is_active = :active, updated_at = now(), updated_by = :actorUserId where id = :id and is_deleted = false";
            case REWARD_TYPES ->
                    "update ref.reward_types set code = :code, name = :name, description = :description, is_monetary = :isMonetary, is_active = :active, updated_at = now(), updated_by = :actorUserId where id = :id and is_deleted = false";
            case DOCUMENT_TYPES ->
                    "update ref.document_types set code = :code, name = :name, description = :description, confidential_by_default = :confidentialByDefault, is_active = :active, updated_at = now(), updated_by = :actorUserId where id = :id and is_deleted = false";
            case NOTIFICATION_TYPES ->
                    "update ref.notification_types set code = :code, name = :name, description = :description, default_channel = :defaultChannel, is_active = :active, updated_at = now(), updated_by = :actorUserId where id = :id and is_deleted = false";
            case LMS_COURSE_TYPES ->
                    "update ref.lms_course_types set code = :code, name = :name, description = :description, mandatory_by_default = :mandatoryByDefault, is_active = :active, updated_at = now(), updated_by = :actorUserId where id = :id and is_deleted = false";
        };
    }

    private String tableName(ReferenceCatalogKey catalog) {
        return switch (catalog) {
            case ORDER_TYPES -> "ref.order_types";
            case LEAVE_TYPES -> "ref.leave_types";
            case DISCIPLINARY_ACTION_TYPES -> "ref.disciplinary_action_types";
            case REWARD_TYPES -> "ref.reward_types";
            case DOCUMENT_TYPES -> "ref.document_types";
            case NOTIFICATION_TYPES -> "ref.notification_types";
            case LMS_COURSE_TYPES -> "ref.lms_course_types";
        };
    }

    private String extraSelect(ReferenceCatalogKey catalog) {
        return switch (catalog) {
            case ORDER_TYPES -> "";
            case LEAVE_TYPES -> ", is_paid, requires_document, available_for_self_service";
            case DISCIPLINARY_ACTION_TYPES -> ", severity_rank";
            case REWARD_TYPES -> ", is_monetary";
            case DOCUMENT_TYPES -> ", confidential_by_default";
            case NOTIFICATION_TYPES -> ", default_channel";
            case LMS_COURSE_TYPES -> ", mandatory_by_default";
        };
    }

    private RowMapper<ReferenceCatalogItemResponse> rowMapper(ReferenceCatalogKey catalog) {
        return (rs, rowNum) -> {
            Map<String, Object> attributes = new LinkedHashMap<>();
            switch (catalog) {
                case LEAVE_TYPES -> {
                    attributes.put("isPaid", rs.getBoolean("is_paid"));
                    attributes.put("requiresDocument", rs.getBoolean("requires_document"));
                    attributes.put("availableForSelfService", rs.getBoolean("available_for_self_service"));
                }
                case DISCIPLINARY_ACTION_TYPES -> attributes.put("severityRank", rs.getInt("severity_rank"));
                case REWARD_TYPES -> attributes.put("isMonetary", rs.getBoolean("is_monetary"));
                case DOCUMENT_TYPES -> attributes.put("confidentialByDefault", rs.getBoolean("confidential_by_default"));
                case NOTIFICATION_TYPES -> attributes.put("defaultChannel", rs.getString("default_channel"));
                case LMS_COURSE_TYPES -> attributes.put("mandatoryByDefault", rs.getBoolean("mandatory_by_default"));
                default -> {
                }
            }
            return new ReferenceCatalogItemResponse(
                    rs.getObject("id", UUID.class),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getBoolean("is_active"),
                    attributes
            );
        };
    }

    private MapSqlParameterSource commonParams(UUID id, ReferenceCatalogUpsertRequest request, UUID actorUserId) {
        return new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", trimToNull(request.code()))
                .addValue("name", trimToNull(request.name()))
                .addValue("description", trimToNull(request.description()))
                .addValue("active", request.active() == null || request.active())
                .addValue("actorUserId", actorUserId);
    }

    private MapSqlParameterSource enrichParams(ReferenceCatalogKey catalog, MapSqlParameterSource params, ReferenceCatalogUpsertRequest request) {
        switch (catalog) {
            case LEAVE_TYPES -> {
                params.addValue("isPaid", booleanValue(request.attributes(), "isPaid", true));
                params.addValue("requiresDocument", booleanValue(request.attributes(), "requiresDocument", false));
                params.addValue("availableForSelfService", booleanValue(request.attributes(), "availableForSelfService", true));
            }
            case DISCIPLINARY_ACTION_TYPES -> params.addValue("severityRank", intValue(request.attributes(), "severityRank", 1));
            case REWARD_TYPES -> params.addValue("isMonetary", booleanValue(request.attributes(), "isMonetary", false));
            case DOCUMENT_TYPES -> params.addValue("confidentialByDefault", booleanValue(request.attributes(), "confidentialByDefault", false));
            case NOTIFICATION_TYPES -> params.addValue("defaultChannel", normalizeChannel(request.attributes()));
            case LMS_COURSE_TYPES -> params.addValue("mandatoryByDefault", booleanValue(request.attributes(), "mandatoryByDefault", false));
            default -> {
            }
        }
        return params;
    }

    private boolean booleanValue(Map<String, Object> attributes, String key, boolean defaultValue) {
        Object value = attributes == null ? null : attributes.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private int intValue(Map<String, Object> attributes, String key, int defaultValue) {
        Object value = attributes == null ? null : attributes.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid numeric value for " + key);
        }
    }

    private String normalizeChannel(Map<String, Object> attributes) {
        Object rawValue = attributes == null ? null : attributes.get("defaultChannel");
        String value = rawValue == null ? "IN_APP" : String.valueOf(rawValue).trim().toUpperCase(Locale.ROOT);
        if (List.of("IN_APP", "EMAIL", "SMS", "TELEGRAM", "SYSTEM").contains(value) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported notification channel");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void writeAudit(String action, ReferenceCatalogKey catalog, UUID entityId, Object before, Object after, CurrentUser actor) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actor.userId());
        log.setActorEmployeeId(actor.employeeId());
        log.setAction(action);
        log.setEntitySchema("ref");
        log.setEntityTable(catalog.path());
        log.setEntityId(entityId);
        log.setOccurredAt(OffsetDateTime.now());
        log.setBeforeData(json(before));
        log.setAfterData(json(after));
        auditLogRepository.save(log);
    }

    private String json(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize reference catalog audit payload", exception);
        }
    }
}
