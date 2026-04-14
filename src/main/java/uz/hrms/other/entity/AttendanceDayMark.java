package uz.hrms.other.entity;

import jakarta.persistence.*;
import uz.hrms.other.AttendanceMarkSource;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "hr", name = "attendance_day_marks")
public class AttendanceDayMark extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "mark_source", nullable = false, length = 30)
    private AttendanceMarkSource markSource;

    @Column(name = "source_record_id", nullable = false)
    private UUID sourceRecordId;

    @Column(name = "mark_status", nullable = false, length = 40)
    private String markStatus;

    @Column(name = "note_text", length = 1000)
    private String noteText;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceMarkSource getMarkSource() {
        return markSource;
    }

    public void setMarkSource(AttendanceMarkSource markSource) {
        this.markSource = markSource;
    }

    public UUID getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(UUID sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
    }

    public String getMarkStatus() {
        return markStatus;
    }

    public void setMarkStatus(String markStatus) {
        this.markStatus = markStatus;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}
