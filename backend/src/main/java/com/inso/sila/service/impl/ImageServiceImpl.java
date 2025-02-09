package com.inso.sila.service.impl;

import com.inso.sila.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    @Override
    public String saveImage(MultipartFile imageFile, String directoryPath) throws IOException {
        LOG.trace("saveFileToStorage({}, {})", imageFile.getOriginalFilename(), directoryPath);

        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            return "";
        }

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename()));
        String newFileName = UUID.randomUUID() + "-" + fileName;

        Path targetLocation = directory.toPath().resolve(newFileName);
        try (InputStream inputStream = imageFile.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return directoryPath + "/" + newFileName;
    }

    public void deleteOldFile(String filePath) {
        if (filePath != null) {
            File oldFile = new File(filePath);
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                if (deleted) {
                    LOG.info("Deleted old image: {}", filePath);
                } else {
                    LOG.warn("Failed to delete old profile image: {}", filePath);
                }
            }
        }
    }
}
