package com.eng.foodbackend.util;

import org.springframework.beans.factory.annotation.Value;
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

public class PhotoHandler {

	@Value("${photos.path}")
	private static String photosPath;

	private static final Logger logger = Logger.getLogger(PhotoHandler.class.getName());

	static public List<String> saveFiles(MultipartFile[] photos) throws IOException {
		List<String> imagePaths = new ArrayList<>();
		Path directoryPath = Path.of(photosPath);
		createDirectoryIfNotExist(directoryPath);

		for (MultipartFile photo : photos) {
			if (photo.isEmpty()) {
				logger.warning("Received an empty file, skipping it.");
				continue;
			}

			String photoName = constructUniqueFilename(photo.getOriginalFilename());
			try {
				String fileUrl = saveFile(photo, photoName, directoryPath);
				imagePaths.add(fileUrl);
				logger.info("Successfully saved file: " + fileUrl);
			} catch (IOException e) {
				logger.severe("Failed to save file " + photoName + ": " + e.getMessage());
				throw e;
			}
		}
		return imagePaths;
	}

	private static String constructUniqueFilename(String originalFilename) {
		String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
		logger.info("Generated unique filename: " + uniqueFilename);
		return uniqueFilename;
	}

	private static void createDirectoryIfNotExist(Path directoryPath) {
		try {
			if (!Files.exists(directoryPath)) {
				Files.createDirectories(directoryPath);
				logger.info("Created directory for photos at: " + directoryPath);
			}
		} catch (IOException e) {
			logger.severe("Could not create the directory at " + directoryPath + ": " + e.getMessage());
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
		logger.info("File stored at: " + fileUrl);
		return fileUrl;
	}
}
