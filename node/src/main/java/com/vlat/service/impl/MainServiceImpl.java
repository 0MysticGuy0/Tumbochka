package com.vlat.service.impl;

import com.vlat.dao.AppUserDAO;
import com.vlat.dao.RawDataDAO;
import com.vlat.entity.AppDocument;
import com.vlat.entity.AppPhoto;
import com.vlat.entity.AppUser;
import com.vlat.entity.RawData;
import com.vlat.entity.enums.UserState;
import com.vlat.exceptions.UploadFileException;
import com.vlat.service.FileService;
import com.vlat.service.MainService;
import com.vlat.service.ProducerService;
import com.vlat.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import lombok.var;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.vlat.entity.enums.UserState.*;
import static com.vlat.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {

    private RawDataDAO rawDataDAO;
    private ProducerService producerService;
    private AppUserDAO appUserDAO;
    private FileService fileService;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, FileService fileService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var textMessage = update.getMessage();
        String text = textMessage.getText();
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var output = "";

        var serviceCommand = ServiceCommands.fromValue(text);
        if(CANCEL.equals(serviceCommand)){
            output = cancelProcess(appUser);
        }else if(BASIC_STATE.equals(userState)){
            output = processServiceCommand(appUser, text);
        }else if(WAIT_FOR_EMAIL_STATE.equals(userState)){
            //TODO обработка email
        }else{
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }

        sendAnswer(textMessage.getChatId(), output);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var message = update.getMessage();
        var chatId = message.getChatId();
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        if (isNotAllowedToSendContent(chatId, appUser)){
            return;
        }

        try{
            AppDocument doc = fileService.processDoc(message);
            //TODO генерация ссылки
            var answer = "Документ успешно загружен! Ссылка для скачивания: ***";
            sendAnswer(chatId, answer);
        }catch (UploadFileException e){
            log.error(e);
            String error = "Ошибка при загрузке. Повторите попытку позже.";
            sendAnswer(chatId, error);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var message = update.getMessage();
        var chatId = message.getChatId();
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        if (isNotAllowedToSendContent(chatId, appUser)){
            return;
        }

        try{
            AppPhoto doc = fileService.processPhoto(message);
            //TODO генерация ссылки
            var answer = "Фото успешно загружено! Ссылка для скачивания: ***";
            sendAnswer(chatId, answer);
        }catch (UploadFileException e){
            log.error(e);
            String error = "Ошибка при загрузке. Повторите попытку позже.";
            sendAnswer(chatId, error);
        }
    }

    private boolean isNotAllowedToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if(!appUser.getIsActive()){
            sendAnswer(chatId, "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента!");
            return true;
        }else if(!BASIC_STATE.equals(userState)){
            sendAnswer(chatId, "Отмените текущую команду с помощью /cancel для отправки файлов.");
            return true;
        }
        return false;
    }

    private void sendAnswer(Long chatId, String output) {
        SendMessage sendMessage = new SendMessage(chatId.toString(),
                output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        log.debug("Received command: "+cmd);
        if(REGISTRATION.equals(cmd)){
            //TODO добавить регистрацию
            return "Временно недоступно!";
        }else if(HELP.equals(cmd)){
            return help();
        }else if(START.equals(cmd)){
            return "Приветствую! Посмотреть список доступных команд: /help";
        }else{
            return "Неизвестная команда! Посмотреть список доступных команд: /help";
        }
    }

    private String help() {
        return "Список доступных комманд:\n"+
                "/cancel - отмена выполнения текущей команды;\n"+
                "/registration - регистрация в системе.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена!";
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .update(update)
                .build();
        rawDataDAO.save(rawData);
    }

    private AppUser findOrSaveAppUser(Update update){
        var telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramId(telegramUser.getId());
        if(persistentAppUser == null){
            AppUser trasientAppUser = AppUser.builder()
                    .telegramId(telegramUser.getId())
                    .firstname(telegramUser.getFirstName())
                    .lastname(telegramUser.getLastName())
                    .username(telegramUser.getUserName())
                    //TODO изсенить после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(trasientAppUser);
        }
        return persistentAppUser;
    }
}
