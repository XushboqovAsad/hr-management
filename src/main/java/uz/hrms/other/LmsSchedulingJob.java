package uz.hrms.other;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.hrms.other.service.LmsService;

@Component
class LmsSchedulingJob {

    private final LmsService lmsService;

    LmsSchedulingJob(LmsService lmsService) {
        this.lmsService = lmsService;
    }

    @Scheduled(cron = "${app.lms.mandatory-sync-cron:0 30 6 * * *}")
    void syncMandatoryAssignments() {
        lmsService.syncMandatoryAssignments();
    }

    @Scheduled(cron = "${app.lms.reminder-cron:0 0 9 * * *}")
    void sendOverdueReminders() {
        lmsService.sendOverdueReminders();
    }
}
