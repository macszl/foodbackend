package com.eng.foodbackend.services;

import com.eng.foodbackend.dtos.ClassificationResponse;
import com.eng.foodbackend.util.PhotoHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ImageRecognitionService {
	private static final Logger logger = Logger.getLogger(ImageRecognitionService.class.getName());
	private static final long MAX_FILE_SIZE = 5242880; // 5 MB, for example
	private static final List<String> ALLOWED_FILE_TYPES = List.of("image/jpeg", "image/png");

	public ClassificationResponse classifyImage(MultipartFile[] photos) {
		logger.info("Starting image classification process");
		if (photos == null || photos.length == 0) {
			logger.severe("No files provided for classification");
			return new ClassificationResponse();
		}

		try {
			logger.info("Saving files");
			List<String> savedFiles = saveImageFile(photos);
			logger.info("Saved files");
			logger.info("Starting classification");
			ClassificationResponse classificationResponse = new ClassificationResponse();

			classificationResponse.setClassifiedCategory("apple"); // example classification
			logger.info("Image classification completed successfully");
			return classificationResponse;
		} catch (Exception e) {
			logger.severe("Error during image classification: " + e.getMessage());
			return new ClassificationResponse();
		}
	}

	public List<String> saveImageFile(MultipartFile[] photos) throws IOException {
		if (photos == null || photos.length == 0) {
			logger.warning("No files provided for saving");
			throw new IllegalArgumentException("No files provided");
		}

		logger.info("Starting saving files");
		List<String> fileNames = new ArrayList<>();
		for (MultipartFile photo : photos) {
			try {
				validateFile(photo);
				MultipartFile[] singlePhotoArray = new MultipartFile[]{photo};
				fileNames.addAll(PhotoHandler.saveFiles(singlePhotoArray));
			} catch (Exception e) {
				logger.severe("File validation failed: " + e.getMessage());
			}
		}

		logger.info("Files saved successfully");
		return fileNames;
	}

	private void validateFile(MultipartFile photo) {
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
}
