package com.vlat.service;

import com.vlat.entity.AppDocument;
import com.vlat.entity.AppPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
    AppPhoto processPhoto(Message externalMessage);
}
