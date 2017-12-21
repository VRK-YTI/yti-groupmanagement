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

import static javax.mail.Message.RecipientType.TO;

@Service
public class EmailSenderService {

    private final EmailSenderDao schedulerDao;
    private final JavaMailSender javaMailSender;
    private final String environmentUrl;

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
            schedulerDao.markRequestAsSentForOrganization(request.id);
        }
    }

    private void sendAccessRequestEmail(List<String> adminEmails, int requestCount, String organizationNameFi) {
        try {
            MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipients(TO, adminEmails.stream().map(EmailSenderService::createAddress).toArray(Address[]::new));
            String from = "no.reply@vrk.fi";
            String message = "Sinulle on " + requestCount + " uutta käyttöoikeuspyyntöä organisaatioon '" + organizationNameFi + "':   " + environmentUrl;
            mail.setFrom(createAddress(from));
            mail.setSender(createAddress(from));
            mail.setSubject("Sinulle on uusia käyttöoikeuspyyntöjä", "text/html; charset=utf-8");
            mail.setContent(message, "text/html; charset=utf-8");

            javaMailSender.send(mail);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Address createAddress(String address) {
        try {
            return new InternetAddress(address);
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }
}