package fi.vm.yti.groupmanagement.service;

import java.util.List;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.groupmanagement.dao.EmailSenderDao;
import fi.vm.yti.groupmanagement.model.UnsentRequestsForOrganization;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;

@Service
public class EmailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);
    private final EmailSenderDao schedulerDao;
    private final JavaMailSender javaMailSender;
    private final String environmentUrl;
    private final String adminEmail;

    @Autowired
    public EmailSenderService(final EmailSenderDao schedulerDao,
                              final JavaMailSender javaMailSender,
                              @Value("${environment.url}") final String environmentUrl,
                              @Value("${admin.email}") final String adminEmail) {
        this.schedulerDao = schedulerDao;
        this.javaMailSender = javaMailSender;
        this.environmentUrl = environmentUrl;
        this.adminEmail = adminEmail;
        logger.info("Use configured ADMIN email: " + adminEmail);
    }

    private static Address createAddress(final String address) {
        try {
            return new InternetAddress(address);
        } catch (AddressException e) {
            logger.error("createAddress failed for address: " + address);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void sendEmailsToAdmins() {
        for (final UnsentRequestsForOrganization request : schedulerDao.getUnsentRequests()) {
            sendAccessRequestEmail(request.adminEmails, request.requestCount, request.nameFi);
            schedulerDao.markRequestAsSentForOrganization(request.id);
        }
    }

    @Transactional
    public void sendEmailToUserOnAcceptance(final String userEmail,
                                            final UUID userId,
                                            final String organizationNameFi) {
        sendAccessRequestAcceptedEmail(userEmail, userId, organizationNameFi);
    }

    private void sendAccessRequestEmail(final List<String> adminEmails,
                                        final int requestCount,
                                        final String organizationNameFi) {
        try {
            final MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipients(BCC, adminEmails.stream().map(EmailSenderService::createAddress).toArray(Address[]::new));
            final String message = "Sinulle on " + requestCount + " uutta käyttöoikeuspyyntöä organisaatioon '" + organizationNameFi + "':   " + environmentUrl;
            mail.setFrom(createAddress(adminEmail));
            mail.setSender(createAddress(adminEmail));
            mail.setSubject("Sinulle on uusia käyttöoikeuspyyntöjä", "UTF-8");
            mail.setText(message, "UTF-8");
            javaMailSender.send(mail);
        } catch (final MessagingException e) {
            logger.warn("sendAccessRequestEmail failed for organization: " + organizationNameFi);
            throw new RuntimeException(e);
        }
    }

    private void sendAccessRequestAcceptedEmail(final String userEmail,
                                                final UUID userId,
                                                final String organizationNameFi) {
        try {
            final MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipient(TO, createAddress(userEmail));
            final String message = "Teille on myönnetty käyttöoikeus yhteentoimivuusalustan organisaatioon '" + organizationNameFi + "':   " + environmentUrl;
            mail.setFrom(createAddress(adminEmail));
            mail.setSender(createAddress(adminEmail));
            mail.setSubject("Ilmoitus käyttöoikeuden hyväksymisestä", "UTF-8");
            mail.setText(message, "UTF-8");
            javaMailSender.send(mail);
            logger.info("Organization request accepted email sent to: " + userId);
        } catch (final MessagingException e) {
            logger.warn("sendAccessRequestAcceptedEmail failed for organization: " + organizationNameFi);
            // Just consume exception if mail sending fails. But add log message
            //            throw new RuntimeException(e);
        }
    }
}
