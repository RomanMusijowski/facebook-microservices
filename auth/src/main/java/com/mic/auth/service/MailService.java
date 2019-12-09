package com.mic.auth.service;

import com.mic.auth.domain.User;
import com.mic.auth.payload.MailRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MailService {

    @Value("${app.queueName}")
    private String queueName;

    private RabbitTemplate queueSender;

    public MailService(RabbitTemplate queueSender) {
        this.queueSender = queueSender;
    }

    public void sendActivationMail(User user) {
        String subject = "Facebook activation code";
        String content = "Dear " + user.getFirstName() + " " + user.getLastName() + ","
                + "\n\n" + "Here is your activation link: "
                + "\n\n" + "http://localhost:8080/auth/api/auth/activation/" + user.getId() + "?activationCode=" + user.getActivationCode();

        MailRequest mailRequest = new MailRequest(user.getId(), user.getEmail(), subject, content);
        queueSender.convertAndSend(queueName, mailRequest);
        log.info("E-mail has been sent on address " + user.getEmail());

    }

}
