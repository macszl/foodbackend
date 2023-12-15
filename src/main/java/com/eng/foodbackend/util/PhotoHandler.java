package com.eng.foodbackend.util;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class PhotoHandler {

	private final static String PHOTOS_PATH = "assets/";

	/**
	 * Save photo filename is constructed as follows:
	 * listingId_originalFilename
	 * If filename repeats, it is overwritten
	 *
	 * @param photos - array of photos to be saved
	 * @return - list of paths to the saved photos
	 */
	static public List<String> saveFiles(MultipartFile[] photos) {
		List<String> imagePaths = new ArrayList<>();

		for (MultipartFile photo : photos) {
			try {
				String photoName = photo.getOriginalFilename();

				if (photo.isEmpty())
					continue;

				String fileUrl = saveFile(photo, photoName);
				imagePaths.add(fileUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return imagePaths;
	}

	private static String saveFile(MultipartFile photo, String filename) {
		if (photo.isEmpty())
			throw new RuntimeException("Empty file");
		try {
			Path directoryPath = Path.of(PHOTOS_PATH);
			Path filePath = directoryPath.resolve(filename);
			if (!Files.exists(directoryPath)) {
				Files.createDirectories(directoryPath);
			}
			Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			return ServletUriComponentsBuilder.fromCurrentContextPath()
											  .path(PHOTOS_PATH)
											  .path(filename)
											  .port(8080)
											  .toUriString();

		} catch (Exception e) {
			throw new RuntimeException("Could not store file " + filename
									   + ". Please try again!", e);
		}

	}
}

