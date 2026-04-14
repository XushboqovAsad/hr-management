package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.*;

public final class AttendanceDomain {
    private AttendanceDomain() {
    }
}

@Entity
@Table(schema = "hr", name = "attendance_adjustments")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_summary_id", nullable = false)
    private AttendanceSummary attendanceSummary;

    @Column(name = "adjusted_start_at")
    private OffsetDateTime adjustedStartAt;

    @Column(name = "adjusted_end_at")
    private OffsetDateTime adjustedEndAt;

    @Column(name = "adjusted_status", nullable = false, length = 40)
    private String adjustedStatus;

    @Column(name = "adjusted_reason", nullable = false, length = 1000)
    private String adjustedReason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    public AttendanceSummary getAttendanceSummary() {
        return attendanceSummary;
    }

    public void setAttendanceSummary(AttendanceSummary attendanceSummary) {
        this.attendanceSummary = attendanceSummary;
    }

    public OffsetDateTime getAdjustedStartAt() {
        return adjustedStartAt;
    }

    public void setAdjustedStartAt(OffsetDateTime adjustedStartAt) {
        this.adjustedStartAt = adjustedStartAt;
    }

    public OffsetDateTime getAdjustedEndAt() {
        return adjustedEndAt;
    }

    public void setAdjustedEndAt(OffsetDateTime adjustedEndAt) {
        this.adjustedEndAt = adjustedEndAt;
    }

    public String getAdjustedStatus() {
        return adjustedStatus;
    }

    public void setAdjustedStatus(String adjustedStatus) {
        this.adjustedStatus = adjustedStatus;
    }

    public String getAdjustedReason() {
        return adjustedReason;
    }

    public void setAdjustedReason(String adjustedReason) {
        this.adjustedReason = adjustedReason;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
