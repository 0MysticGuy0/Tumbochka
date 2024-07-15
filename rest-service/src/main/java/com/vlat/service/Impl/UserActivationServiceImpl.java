package com.vlat.service.Impl;

import com.vlat.dao.AppUserDAO;
import com.vlat.service.UserActivationService;
import com.vlat.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import lombok.var;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDAO appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        var userId = cryptoTool.idOf(cryptoUserId);
        var optionalUser = appUserDAO.findById(userId);
        if(optionalUser.isPresent()){
            var user = optionalUser.get();
            user.setIsActive(true);
            appUserDAO.save(user);
            return true;
        }
        return false;
    }
}
