package com.scutelnic.rutex.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class PageController {
	
	@GetMapping("/uploads/profile-images/{filename}")
	@ResponseBody
	public ResponseEntity<Resource> serveProfileImage(@PathVariable String filename) {
		try {
			Path filePath = Paths.get("uploads/profile-images/" + filename);
			Resource resource = new UrlResource(filePath.toUri());
			
			if (resource.exists() && resource.isReadable()) {
				return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_JPEG)
					.body(resource);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
