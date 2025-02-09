package com.inso.sila.service.validation;

import com.inso.sila.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImageValidator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void validateUserProfileImageForUpload(MultipartFile file, String message) throws ValidationException {
        LOG.trace("checkMaxImageSize({})", file);
        List<String> errors = new ArrayList<>();
        long maxSizeBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            errors.add("File " + file.getOriginalFilename() + " is too large to be added. Maximal file size is 5MB.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(message, errors);
        }
    }
}
