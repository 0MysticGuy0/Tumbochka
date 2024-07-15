package com.vlat.service.impl;

import com.vlat.dao.AppUserDAO;
import com.vlat.dao.RawDataDAO;
import com.vlat.entity.AppDocument;
import com.vlat.entity.AppPhoto;
import com.vlat.entity.AppUser;
import com.vlat.entity.RawData;
import com.vlat.entity.enums.UserState;
import com.vlat.exceptions.UploadFileException;
import com.vlat.service.AppUserService;
import com.vlat.service.FileService;
import com.vlat.service.MainService;
import com.vlat.service.ProducerService;
import com.vlat.service.enums.LinkType;
import com.vlat.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.vlat.entity.enums.UserState.*;
import static com.vlat.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {

    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;
    private final AppUserService appUserService;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO, FileService fileService, AppUserService appUserService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
        this.appUserService = appUserService;
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
            output = processServiceCommand(appUser, serviceCommand);
        }else if(WAIT_FOR_EMAIL_STATE.equals(userState)){
            output = appUserService.setEmail(appUser, text);
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
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            var answer = "Документ успешно загружен! Ссылка для скачивания: " + link;
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
            AppPhoto photo = fileService.processPhoto(message);
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            var answer = "Фото успешно загружено! Ссылка для скачивания: " + link;
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

    private String processServiceCommand(AppUser appUser, ServiceCommands command) {
        log.debug("Received command: "+command);
        if(REGISTRATION.equals(command)){
            return appUserService.registerUser(appUser);
        }else if(HELP.equals(command)){
            return help();
        }else if(START.equals(command)){
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
        var optionalUser = appUserDAO.findByTelegramId(telegramUser.getId());
        if(optionalUser.isEmpty()){
            AppUser trasientAppUser = AppUser.builder()
                    .telegramId(telegramUser.getId())
                    .firstname(telegramUser.getFirstName())
                    .lastname(telegramUser.getLastName())
                    .username(telegramUser.getUserName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(trasientAppUser);
        }
        return optionalUser.get();
    }
}
