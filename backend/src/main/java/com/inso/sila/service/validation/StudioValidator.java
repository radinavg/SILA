package com.inso.sila.service.validation;


import com.inso.sila.exception.ConflictException;
import com.inso.sila.repository.StudioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;


@Component
public class StudioValidator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final StudioRepository studioRepository;

    public StudioValidator(StudioRepository studioRepository) {
        this.studioRepository = studioRepository;
    }

    public void validateUniqueStudioLocation(String location) throws ConflictException {
        LOG.trace("validateUniqueStudioLocation({})", location);
        List<String> conflictErrors = new ArrayList<>();
        if (studioRepository.findByLocation(location) != null) {
            conflictErrors.add("There already exists a studio on given location");
        }
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Couldn't create studio", conflictErrors);
        }
    }
}
