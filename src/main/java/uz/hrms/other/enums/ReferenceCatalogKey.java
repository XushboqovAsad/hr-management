package uz.hrms.other.enums;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum ReferenceCatalogKey {
    ORDER_TYPES("order-types", "Типы приказов", "Справочник типов кадровых приказов"),
    LEAVE_TYPES("leave-types", "Виды отпусков", "Параметры отпускных типов"),
    DISCIPLINARY_ACTION_TYPES("disciplinary-action-types", "Виды взысканий", "Типы дисциплинарных взысканий"),
    REWARD_TYPES("reward-types", "Виды поощрений", "Типы поощрений и наград"),
    DOCUMENT_TYPES("document-types", "Типы документов", "Кадровые и личные документы"),
    NOTIFICATION_TYPES("notification-types", "Типы уведомлений", "Типы системных уведомлений"),
    LMS_COURSE_TYPES("lms-course-types", "Типы LMS-курсов", "Типы и флаги назначений LMS");

    private final String path;
    private final String label;
    private final String description;

    ReferenceCatalogKey(String path, String label, String description) {
        this.path = path;
        this.label = label;
        this.description = description;
    }

    String path() {
        return path;
    }

    String label() {
        return label;
    }

    String description() {
        return description;
    }

    public static uz.hrms.other.ReferenceCatalogKey fromPath(String path) {
        for (uz.hrms.other.ReferenceCatalogKey value : values()) {
            if (value.path.equalsIgnoreCase(path)) {
                return value;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown reference catalog: " + path);
    }
}
