package com.eng.foodbackend.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Component
public class PhotoHandler {

	private static String photosPath = "assets/";

	private static final Logger logger = Logger.getLogger(PhotoHandler.class.getName());

	public static List<String> saveFiles(MultipartFile[] photos) throws IOException {
		if (photos == null || photos.length == 0) {
			logWarning("saveImageFile: No files provided for saving");
			throw new IllegalArgumentException("No files provided");
		}

		List<String> imagePaths = new ArrayList<>();
		Path directoryPath = Path.of(photosPath);
		createDirectoryIfNotExist(directoryPath);

		for (MultipartFile photo : photos) {
			if (photo.isEmpty()) {
				logWarning("saveFiles: Received an empty file, skipping it.");
				continue;
			}

			String photoName = constructUniqueFilename(photo.getOriginalFilename());
			try {
				String fileUrl = saveFile(photo, photoName, directoryPath);
				imagePaths.add(fileUrl);
				logInfo("saveFiles: Successfully saved file: " + fileUrl);
			} catch (IOException e) {
				logSevere("saveFiles: Failed to save file " + photoName + ": " + e.getMessage());
				throw e;
			}
		}
		return imagePaths;
	}

	private static String constructUniqueFilename(String originalFilename) {
		String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
		logInfo("constructUniqueFilename: Generated unique filename: " + uniqueFilename);
		return uniqueFilename;
	}

	private static void createDirectoryIfNotExist(Path directoryPath) {
		try {
			if (!Files.exists(directoryPath)) {
				Files.createDirectories(directoryPath);
				logInfo("createDirectoryIfNotExist: Created directory for photos at: " + directoryPath);
			}
		} catch (IOException e) {
			logSevere("createDirectoryIfNotExist: Could not create the directory at " + directoryPath + ": " + e.getMessage());
			throw new RuntimeException("Could not create the directory for photos.", e);
		}
	}

	private static String saveFile(MultipartFile photo, String filename, Path directoryPath) throws IOException {
		Path filePath = directoryPath.resolve(filename);
		Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
													.path(photosPath)
													.path(filename)
													.toUriString();
		logInfo("saveFile: File stored at: " + fileUrl);
		return fileUrl;
	}

	private static void logInfo(String message) {
		logger.info("PhotoHandler." + message);
	}

	private static void logWarning(String message) {
		logger.warning("PhotoHandler." + message);
	}

	private static void logSevere(String message) {
		logger.severe("PhotoHandler." + message);
	}
}
