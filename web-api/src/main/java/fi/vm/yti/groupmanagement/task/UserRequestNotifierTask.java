package fi.vm.yti.groupmanagement.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.vm.yti.groupmanagement.service.EmailSenderService;

@Component
public class UserRequestNotifierTask {

    private static Logger log = LoggerFactory.getLogger(UserRequestNotifierTask.class);

    private final EmailSenderService emailSenderService;
    private final boolean sendAdminEmails;

    public UserRequestNotifierTask(final EmailSenderService emailSenderService,
                                   @Value("${send.admin.emails}") final boolean sendAdminEmails) {
        this.emailSenderService = emailSenderService;
        this.sendAdminEmails = sendAdminEmails;
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void notifyAdmins() {
        if (sendAdminEmails) {
            log.debug("Scheduled admin notification task started");
            this.emailSenderService.sendEmailsToAdmins();
        }
    }
}
