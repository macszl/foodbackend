package com.eng.foodbackend.services;

import com.eng.foodbackend.dtos.ClassificationResponse;
import com.eng.foodbackend.util.PhotoHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class ImageRecognitionService {
	private static final Logger logger = Logger.getLogger(ImageRecognitionService.class.getName());
	private static final long MAX_FILE_SIZE = 5242880; // 5 MB, for example
	private static final List<String> ALLOWED_FILE_TYPES = List.of("image/jpeg", "image/png");


    public ClassificationResponse classifyImage(MultipartFile[] photos) {
        log("classifyImage: Starting image classification process");
        if (photos == null || photos.length == 0) {
            logSevere("classifyImage: No files provided for classification");
            return new ClassificationResponse();
        }

        try {
            log("classifyImage: Saving files");
            List<String> savedFiles = saveImageFile(photos);
            log("classifyImage: Saved files");

            String imagePath = savedFiles.get(0); // Assuming a single image is provided for classification
            log("classifyImage: Sending image for classification");
            String classifiedCategory = sendImageForClassification(imagePath);

            ClassificationResponse classificationResponse = new ClassificationResponse();
            classificationResponse.setClassifiedCategory(classifiedCategory);

            log("classifyImage: Image classification completed successfully");
            return classificationResponse;
        } catch (Exception e) {
            logSevere("classifyImage: Error during image classification: " + e.getMessage());
            return new ClassificationResponse();
        }
    }

    private String sendImageForClassification(String imagePath) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://127.0.0.1:5000/predict";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestJson = "{\"file_uri\":\"" + imagePath + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<ClassificationResponse> response = restTemplate.postForEntity(url, entity, ClassificationResponse.class);

            return response.getBody().getClassifiedCategory();
        } catch (Exception e) {
            logSevere("sendImageForClassification: Error during sending image for classification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

	public List<String> saveImageFile(MultipartFile[] photos) throws IOException, IllegalArgumentException {
		log("saveImageFile: Starting saving files");
		if (photos == null || photos.length == 0) {
			logWarning("saveImageFile: No files provided for saving");
			throw new IllegalArgumentException("No files provided");
		}

		List<String> fileNames = new ArrayList<>();
		for (MultipartFile photo : photos) {
			try {
				log("saveImageFile: Starting file validation");
				validateFile(photo);
				log("saveImageFile: File validated");

				MultipartFile[] singlePhotoArray = { photo };

				log("saveImageFile: Starting file saving");
				List<String> stringList = PhotoHandler.saveFiles(singlePhotoArray);
				log("saveImageFile: Files saved successfully");

				log("saveImageFile: Adding Files to filenames");
				fileNames.addAll(stringList);
				log("saveImageFile: Added Files to filenames successfully");
			} catch (Exception e) {

				log("saveImageFile: Failed to save files successfully");
				throw new IOException(e);
			}
		}

		return fileNames;
	}

	private void validateFile(MultipartFile photo) throws IllegalArgumentException {
		log("validateFile: Validating file");

		if (photo == null) {
			throw new IllegalArgumentException("Photo is null");
		}

		if (photo.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("File size too large: " + photo.getOriginalFilename());
		}

		if (!ALLOWED_FILE_TYPES.contains(photo.getContentType())) {
			throw new IllegalArgumentException("Invalid file type: " + photo.getContentType());
		}

		if (photo.isEmpty()) {
			throw new IllegalArgumentException("File is empty: " + photo.getOriginalFilename());
		}
	}

	private void log(String message) {
		logger.info("ImageRecognitionService." + message);
	}

	private void logWarning(String message) {
		logger.warning("ImageRecognitionService." + message);
	}

	private void logSevere(String message) {
		logger.severe("ImageRecognitionService." + message);
	}
}
