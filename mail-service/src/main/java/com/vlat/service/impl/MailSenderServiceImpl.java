package com.vlat.service.impl;

import com.vlat.dto.MailParams;
import com.vlat.service.MailSenderService;
import lombok.extern.log4j.Log4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class MailSenderServiceImpl implements MailSenderService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String mailFrom;
    @Value("${service.activation.uri}")
    private String activationUri;

    public MailSenderServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(MailParams mailParams) {
        var subject = "Активация учетной записи для telegram-бота \"Tumbochka\"";
        var messageBody = getActivationMessageBody(mailParams.getId());
        var mailTo = mailParams.getEmailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setSubject(subject);
        mailMessage.setTo(mailTo);
        mailMessage.setText(messageBody);

        log.debug("Started sending email to " + mailTo);

        mailSender.send(mailMessage);
    }

    private String getActivationMessageBody(String id) {
        return String.format(
                "Для завершения регистрации пройдите по ссылке:\n%s",
                activationUri.replace("{id}", id));
    }
}
