package uz.hrms.other;

public final class AbsenceDomain {
    private AbsenceDomain() {
    }
}

enum AbsenceType {
    SICK_LEAVE,
    EXCUSED_ABSENCE,
    ABSENCE_UNEXCUSED,
    UNPAID_LEAVE,
    REMOTE_WORK,
    DOWNTIME,
    OTHER
}

public enum AbsenceStatus {
    DRAFT,
    SUBMITTED,
    HR_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED,
    CLOSED
}

public enum AbsenceDocumentStatus {
    ACTIVE,
    ARCHIVED
}

enum AttendanceMarkSource {
    ABSENCE,
    LEAVE,
    BUSINESS_TRIP,
    MANUAL
}


