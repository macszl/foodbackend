package com.eng.foodbackend.controllers;

import com.eng.foodbackend.dtos.ClassificationResponse;
import com.eng.foodbackend.services.ImageRecognitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageRecognitionController
{
	private final ImageRecognitionService imageRecognitionService;

	@PostMapping("/recognize")
	public ResponseEntity<?> classifyImage(@RequestParam("photos") MultipartFile[] photos)
	{
		try {
			ClassificationResponse response = imageRecognitionService.classifyImage(photos);
			return ResponseEntity.ok(response);
		} catch ( Exception e ) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Improper data");
		}
	}
}
