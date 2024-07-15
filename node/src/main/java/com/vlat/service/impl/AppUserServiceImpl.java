package com.vlat.service.impl;

import com.vlat.dao.AppUserDAO;
import com.vlat.dto.MailParams;
import com.vlat.entity.AppUser;
import com.vlat.entity.enums.UserState;
import com.vlat.service.AppUserService;
import com.vlat.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Service
@Log4j
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if(appUser.getIsActive()){
            return "Вы уже зарегистрированы!";
        }else if(appUser.getEmail() != null){
            return "На указанную вами почту ("+appUser.getEmail()+") уже было отправлено письмо для подтверждения.";
        }
        appUser.setState(UserState.WAIT_FOR_EMAIL_STATE);
        appUserDAO.save(appUser);
        return "Введите ваш emasil: ";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try{
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        }catch (AddressException e){
            return "Введите корректный email! Отмена команды: /cancel";
        }
        var optionalUser = appUserDAO.findByEmail(email);
        if(optionalUser.isEmpty()){
            appUser.setEmail(email);
            appUser.setState(UserState.BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            var cryptoUserId = cryptoTool.hashOf(appUser.getId());
            var response = sendRequestToEmailService(cryptoUserId, email);

            if(response.getStatusCode() != HttpStatus.OK){
                var msg = "Не удалось отпрваить письмо на почту "+email+"!";
                log.error(msg);
                appUser.setEmail(null);
                appUserDAO.save(appUser);
                return msg;
            }

            return "Email успешно установлен. Для завершения регистрации пройдите по ссылке из письма, отправленного на указанный адрес.";
        }
        return "Пользователь с таким email`ом уже существует! Для отмены комманды введите /cancel";
    }

    private ResponseEntity<String> sendRequestToEmailService(String cryptoUserId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        var request = new HttpEntity<MailParams>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
