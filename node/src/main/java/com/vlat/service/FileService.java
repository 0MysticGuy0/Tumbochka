package com.vlat.service;

import com.vlat.entity.AppDocument;
import com.vlat.entity.AppPhoto;
import com.vlat.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
    AppPhoto processPhoto(Message externalMessage);
    String generateLink(Long fileId, LinkType linkType);
}
