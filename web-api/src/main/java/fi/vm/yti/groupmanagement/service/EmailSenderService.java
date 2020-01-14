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

import fi.vm.yti.groupmanagement.config.ApplicationProperties;
import fi.vm.yti.groupmanagement.dao.EmailSenderDao;
import fi.vm.yti.groupmanagement.dao.FrontendDao;
import fi.vm.yti.groupmanagement.model.TempUser;
import fi.vm.yti.groupmanagement.model.UnsentRequestsForOrganization;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;

@Service
public class EmailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);
    private final ApplicationProperties applicationProperties;
    private final EmailSenderDao schedulerDao;
    private final FrontendDao frontendDao;
    private final JavaMailSender javaMailSender;
    private final String environmentUrl;
    private final String adminEmail;

    @Autowired
    public EmailSenderService(final ApplicationProperties applicationProperties,
                              final EmailSenderDao schedulerDao,
                              final FrontendDao frontendDao,
                              final JavaMailSender javaMailSender,
                              @Value("${environment.url}") final String environmentUrl,
                              @Value("${admin.email}") final String adminEmail) {
        this.applicationProperties = applicationProperties;
        this.schedulerDao = schedulerDao;
        this.frontendDao = frontendDao;
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
    public void sendEmailsToTempUsers() {
        logger.debug("Sending invitations to temp users");
        for (final TempUser tempUser : schedulerDao.getTempUsersWithoutTokens()) {
            final String token = frontendDao.createToken(tempUser.id, "tempuser");
            final String uri = constructContainerUriWithTokenAndEnv(tempUser.containerUri, token);
            sendTempUserInvitationEmail(tempUser.email, tempUser.id, tempUser.containerUri, uri);
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

    private void sendTempUserInvitationEmail(final String userEmail,
                                             final UUID userId,
                                             final String resourceUri,
                                             final String resourceUriWithToken) {
        try {
            final MimeMessage mail = javaMailSender.createMimeMessage();
            mail.addRecipient(TO, createAddress(userEmail));
            final String message = createTempUserInviteString(resourceUri, resourceUriWithToken);
            mail.setFrom(createAddress(adminEmail));
            mail.setSender(createAddress(adminEmail));
            mail.setSubject("Ilmoitus kommentointikierrokselle osallistumisesta", "UTF-8");
            mail.setContent(message, "text/html; charset=UTF-8");
            javaMailSender.send(mail);
            logger.info("Temp user invitation email sent to: " + userId + " for resource: " + resourceUri);
        } catch (final MessagingException e) {
            logger.warn("sendTempUserInvitationEmail failed for user: " + userId);
        }
    }

    private String createTempUserInviteString(final String resourceUri,
                                              final String resourceUriWithToken) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<body>");
        stringBuffer.append("Hyvä vastaanottaja,<br/><br/>");
        stringBuffer.append("Olet saanut kutsun Yhteentoimivuusalustan kommentointikierrokselle.<br/><br/>");
        stringBuffer.append("Pääset kommentoimaan kierroksella olevia sisältöjä alla olevasta linkistä. Huomioithan, että linkki on henkilökohtainen ja sen kautta tehty kommentointi tallentuu kierrokselle sinun nimissäsi! Älä siis jaa linkkiä eteenpäin. Linkki on voimassa 6 kk.<br/><br/>");
        stringBuffer.append("<a href=\"" + resourceUriWithToken + "\">" + resourceUri + "</a><br/><br/>");
        stringBuffer.append("Kommentointi-työkalun käyttöohjeet löydät <a href=\"https://vrk-ewiki.eden.csc.fi/display/YTIJD/6.+Kommentointi\">täältä</a>.<br/><br/>");
        stringBuffer.append("Tämä viesti on lähetetty automaattisesti Yhteentoimivuusalustan Kommentit-työkalusta. Ethän vastaa viestiin!<br/><br/>");
        stringBuffer.append("Jos käytön suhteen ilmenee teknisiä ongelmia, otathan yhteyttä Digi- ja väestötietovirastoon yhteentoimivuus@dvv.fi!");
        stringBuffer.append("</body>");
        return stringBuffer.toString();
    }

    private String constructContainerUriWithTokenAndEnv(final String uri,
                                                        final String token) {
        final String env = applicationProperties.getEnv();
        if ("prod".equalsIgnoreCase(env)) {
            return uri + "?token=" + token;
        } else {
            return uri + "?env=" + env + "&token=" + token;
        }
    }
}
