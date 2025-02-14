package com.vlat.controller;

import com.vlat.service.UpdateProducer;
import com.vlat.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.vlat.model.RabbitQueue.*;

@Component
@Log4j
public class UpdateController {

    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update){
        if (update == null){
            log.error("Received update is null");
            return;
        }

        if (update.getMessage() != null){
            distributeMessageByType(update);
        }else{
            log.error("Received unsupported message type " + update);
        }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        boolean correctMessageType = false;
        if(message.hasText()){
            processTextMessage(update);
            correctMessageType = true;
        }
        if (message.hasDocument()){
            processDocMessage(update);
            correctMessageType = true;
        }
        if (message.hasPhoto()){
            processPhotoMessage(update);
            correctMessageType = true;
        }

        if(!correctMessageType){
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Неподдерживаемый тип сообщения!");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void setFileIsReceivedView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл получен. Идет обработка...");
        setView(sendMessage);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }
}
