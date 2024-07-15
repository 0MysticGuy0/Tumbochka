package com.vlat.service;

import com.vlat.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
