package com.vlat.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;


public interface UpdateProducer {
    void produce(String rabbitQueue, Update update);
}
