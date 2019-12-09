package com.mic.mailservice.service;

import com.mic.mailservice.payload.MailRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


@Service
@Log4j2
public class MailServiceImpl implements MailService {

    @Value("${app.queueName}")
    private String queueName;
    @Value("${app.mail.smtp.host}")
    private String host;
    @Value("${app.mail.smtp.port}")
    private String port;
    @Value("${app.mail.smtp.auth}")
    private String auth;
    @Value("${app.mail.smtp.starttls.enable}")
    private boolean startTlsEnable;
    @Value("${app.mail.email}")
    private String email;
    @Value("${app.mail.username}")
    private String username;
    @Value("${app.mail.password}")
    private String password;

    @RabbitListener(queues = "${app.queueName}")
    public void sendMail(final MailRequest mailRequest) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
        prop.put("mail.smtp.auth", auth);
        prop.put("mail.smtp.starttls.enable", startTlsEnable); //TLS
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(mailRequest.getEmail())
            );
            message.setSubject(mailRequest.getSubject());
            message.setText(mailRequest.getContent());

            Transport.send(message);
            log.info("E-mail has beed send on address " + mailRequest.getEmail());
    }

}
