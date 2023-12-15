package com.eng.foodbackend.services;

import com.eng.foodbackend.dtos.ClassificationResponse;
import com.eng.foodbackend.util.PhotoHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ImageRecognitionService
{
	public ClassificationResponse classifyImage(MultipartFile[] photos)
	{
		saveImageFile(photos);
		ClassificationResponse classificationResponse = new ClassificationResponse();

		classificationResponse.setClassifiedCategory("apple");

		return classificationResponse;
	}
	public List<String> saveImageFile(MultipartFile[] photos) {
		return PhotoHandler.saveFiles(photos);
	}
}
