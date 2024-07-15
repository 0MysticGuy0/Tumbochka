package com.vlat.service.Impl;

import com.vlat.dao.AppDocumentDAO;
import com.vlat.dao.AppPhotoDAO;
import com.vlat.dao.BinaryContentDAO;
import com.vlat.entity.AppDocument;
import com.vlat.entity.AppPhoto;
import com.vlat.entity.BinaryContent;
import com.vlat.service.FileService;
import com.vlat.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Log4j
public class FileServiceImpl implements FileService {
    private AppDocumentDAO appDocumentDAO;
    private AppPhotoDAO appPhotoDAO;
    private CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, AppPhotoDAO appPhotoDAO, CryptoTool cryptoTool) {
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDAO = appPhotoDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument getDocument(String id) {
        Long file_id = cryptoTool.idOf(id);
        return appDocumentDAO.findById(file_id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String id) {
        Long file_id = cryptoTool.idOf(id);
        return appPhotoDAO.findById(file_id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemresource(BinaryContent binaryContent) {
        try{
            File temp = File.createTempFile("tempFile",".bin");
            temp.deleteOnExit();
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileAsArrayOfBytes());
            return new FileSystemResource(temp);
        }catch (IOException e){
            log.error(e);
            return null;
        }
    }
}
