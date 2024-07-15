package com.vlat.service;

import com.vlat.entity.AppDocument;
import com.vlat.entity.AppPhoto;
import com.vlat.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
    FileSystemResource getFileSystemresource(BinaryContent binaryContent); // получение файла для передачи в тг
}
