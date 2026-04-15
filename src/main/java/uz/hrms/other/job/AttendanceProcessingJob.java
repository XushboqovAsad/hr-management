package uz.hrms.other;

import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.hrms.other.service.AttendanceService;

@Component
class AttendanceProcessingJob {

    private final AttendanceService attendanceService;

    AttendanceProcessingJob(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Scheduled(cron = "${app.attendance.daily-process-cron:0 15 1 * * *}")
    void processPreviousDay() {
        attendanceService.processWorkDate(LocalDate.now().minusDays(1), null);
    }
}
