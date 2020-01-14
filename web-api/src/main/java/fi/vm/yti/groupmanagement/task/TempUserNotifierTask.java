package fi.vm.yti.groupmanagement.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.vm.yti.groupmanagement.service.EmailSenderService;

@Component
public class TempUserNotifierTask {

    private static Logger log = LoggerFactory.getLogger(UserRequestNotifierTask.class);

    private final EmailSenderService emailSenderService;

    public TempUserNotifierTask(final EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @Scheduled(cron = "0 * * * * ?")
    public void sendTempUserInvitations() {
        log.debug("Scheduled temp user invite task started");
        this.emailSenderService.sendEmailsToTempUsers();
    }
}
