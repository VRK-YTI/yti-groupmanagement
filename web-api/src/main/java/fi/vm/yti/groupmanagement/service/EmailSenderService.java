package fi.vm.yti.groupmanagement.service;

import fi.vm.yti.groupmanagement.dao.EmailSenderDao;
import fi.vm.yti.groupmanagement.model.UnsentRequestsForOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.MailSendException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.UUID;

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailSenderService {

    private final EmailSenderDao schedulerDao;
    private final JavaMailSender javaMailSender;
    private final String environmentUrl;
    private final String adminEmail;

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);
        
    @Autowired
    public EmailSenderService(EmailSenderDao schedulerDao,
                              JavaMailSender javaMailSender,
                              @Value("${environment.url}") String environmentUrl,
                              @Value("${admin.email}") String adminEmail) {
        this.schedulerDao = schedulerDao;
        this.javaMailSender = javaMailSender;
        this.environmentUrl = environmentUrl;
        this.adminEmail=adminEmail;
        logger.info("SendEmail: AdminEmail:"+adminEmail);
    }    
    
    @Transactional
    public void sendEmailsToAdmins() {
        for (UnsentRequestsForOrganization request : schedulerDao.getUnsentRequests()) {
            logger.info("SendEmail; AccessRequest  for userId:"+request.id);
            sendAccessRequestEmail(request.adminEmails, request.adminUsers, request.userId,  request.requestCount, request.nameFi);
            // request.id = organizationId
            schedulerDao.markRequestAsSentForOrganization(request.id);
        }
    }

    @Transactional
    public void sendEmailToUserOnAcceptance(String userEmail, UUID userId,  String organizationNameFi) {
        sendAccessRequestAcceptedEmail(userEmail, userId, organizationNameFi);
    }

    private void sendAccessRequestEmail(List<String> adminEmails, List<UUID> adminUsers, UUID userId, int requestCount, String organizationNameFi) {
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipients(BCC, adminEmails.stream().map(EmailSenderService::createAddress).toArray(Address[]::new));
            String from = adminEmail;
            String message = "Sinulle on " + requestCount + " uutta käyttöoikeuspyyntöä organisaatioon '" + organizationNameFi + "':   " + environmentUrl;
            mail.setFrom(createAddress(from));
            mail.setSender(createAddress(from));
            mail.setSubject("Sinulle on uusia käyttöoikeuspyyntöjä", "UTF-8");
            mail.setText(message, "UTF-8");

            logger.info("SendEmail:  Accept Access Request for user:"+ userId.toString()+" sent to the following admins:"+ adminUsers.toString() );
            javaMailSender.send(mail);

        } catch (MessagingException e) { 
            // log exception
            logger.warn("SendEmail: sendAccessRequest accept  failed for "+organizationNameFi);
            throw new RuntimeException(e);
        } catch (MailSendException me){
            logger.warn("SendEmail: sendAccessRequest accept  failed for "+organizationNameFi + " Exception:"+me.getMessage());
        }
    }

    private void sendAccessRequestAcceptedEmail(String userEmail, UUID userId, String organizationNameFi) {
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipient(TO, createAddress(userEmail));
            String from = "yhteentoimivuus@vrk.fi";
            String message = "Teille on myönnetty käyttöoikeus yhteentoimivuusalustan organisaatioon '" + organizationNameFi + "':   " + environmentUrl;
            mail.setFrom(createAddress(from));
            mail.setSender(createAddress(from));
            mail.setSubject("Ilmoitus käyttöoikeuden hyväksymisestä", "UTF-8");
            mail.setText(message, "UTF-8");

            logger.info("sendEmail: Access request accepted to the user:"+userId.toString());
            javaMailSender.send(mail);

        } catch (MessagingException e) {
            logger.warn("SendEmail: sendAccessRequestAcceptedEmail failed for "+organizationNameFi);
            throw new RuntimeException(e);
        }
    }


    private static Address createAddress(String address) {
        try {
            return new InternetAddress(address);
        } catch (AddressException e) {
            logger.error("SendEmail: CreateAddess failed for "+address);
            throw new RuntimeException(e);
        }
    }
}
