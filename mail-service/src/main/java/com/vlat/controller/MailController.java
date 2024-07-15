package com.vlat.controller;

import com.vlat.dto.MailParams;
import com.vlat.service.MailSenderService;
import lombok.extern.log4j.Log4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
@Log4j
public class MailController {
    private MailSenderService mailSenderService;

    public MailController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParams mailParams){
        log.debug("Received request to send email to " + mailParams.getEmailTo());
        mailSenderService.send(mailParams);
        return ResponseEntity.ok().build();
    }

}
