package fi.vm.yti.groupmanagement.service;

import fi.vm.yti.groupmanagement.dao.EmailSenderDao;
import fi.vm.yti.groupmanagement.model.UnsentRequestsForOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailSenderService {

    private final EmailSenderDao schedulerDao;
    private final JavaMailSender javaMailSender;
    private final String environmentUrl;
    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);
        
    @Autowired
    public EmailSenderService(EmailSenderDao schedulerDao,
                              JavaMailSender javaMailSender,
                              @Value("${environment.url}") String environmentUrl) {
        this.schedulerDao = schedulerDao;
        this.javaMailSender = javaMailSender;
        this.environmentUrl = environmentUrl;
    }

    @Transactional
    public void sendEmailsToAdmins() {
        for (UnsentRequestsForOrganization request : schedulerDao.getUnsentRequests()) {            
            sendAccessRequestEmail(request.adminEmails, request.requestCount, request.nameFi);
            // request.id = organizationId
            schedulerDao.markRequestAsSentForOrganization(request.id);
        }
    }

    @Transactional
    public void sendEmailToUserOnAcceptance(String userEmail, String organizationNameFi) {
        sendAccessRequestAcceptedEmail(userEmail, organizationNameFi);
    }

    private void sendAccessRequestEmail(List<String> adminEmails, int requestCount, String organizationNameFi) {
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipients(BCC, adminEmails.stream().map(EmailSenderService::createAddress).toArray(Address[]::new));
            String from = "yhteentoimivuus@vrk.fi";
            String message = "Sinulle on " + requestCount + " uutta käyttöoikeuspyyntöä organisaatioon '" + organizationNameFi + "':   " + environmentUrl;
            mail.setFrom(createAddress(from));
            mail.setSender(createAddress(from));
            mail.setSubject("Sinulle on uusia käyttöoikeuspyyntöjä", "UTF-8");
            mail.setText(message, "UTF-8");

            javaMailSender.send(mail);

        } catch (MessagingException e) { 
            logger.warn("sendAccessRequestEmail failed for "+organizationNameFi);
            throw new RuntimeException(e);
        }
    }

    private void sendAccessRequestAcceptedEmail(String userEmail, String organizationNameFi) {
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipient(TO, createAddress(userEmail));
            String from = "yhteentoimivuus@vrk.fi";
            String message = "Teille on myönnetty käyttöoikeus yhteentoimivuusalustan organisaatioon '" + organizationNameFi + "':   " + environmentUrl;
            mail.setFrom(createAddress(from));
            mail.setSender(createAddress(from));
            mail.setSubject("Ilmoitus käyttöoikeuden hyväksymisestä", "UTF-8");
            mail.setText(message, "UTF-8");

            javaMailSender.send(mail);

        } catch (MessagingException e) {
            logger.warn("sendAccessRequestAcceptedEmail failed for "+organizationNameFi);
            // Just consume exception if mail sending fails. But add log message
            //            throw new RuntimeException(e);
        }
    }


    private static Address createAddress(String address) {
        try {
            return new InternetAddress(address);
        } catch (AddressException e) {
            logger.error("CreateAddess failed for "+address);
            throw new RuntimeException(e);
        }
    }
}
