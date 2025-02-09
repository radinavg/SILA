package com.inso.sila.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {



    /**
     * Saves an image file for a studio activity into the assets' directory.
     *
     * @param imageFile a multipart file for the image.
     * @param directoryPath the path to the directory.
     * @return the path of the newly saved image.
     * @throws IOException if an error occurred while writing the image file.
     */
    String saveImage(MultipartFile imageFile, String directoryPath) throws IOException;

    /**
     * Deletes old file if image is updated.
     *
     * @param directoryPath path of the image to delete.
     *
     * */
    void deleteOldFile(String directoryPath);


}
