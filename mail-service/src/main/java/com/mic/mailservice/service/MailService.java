package com.mic.mailservice.service;

import com.mic.mailservice.payload.MailRequest;

import javax.mail.MessagingException;

public interface MailService {
    void sendMail(MailRequest mailRequest) throws MessagingException;
}
