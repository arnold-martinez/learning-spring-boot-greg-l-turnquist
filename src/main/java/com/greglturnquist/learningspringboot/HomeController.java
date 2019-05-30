package com.greglturnquist.learningspringboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class HomeController {

    private static final String BASE_PATH = "/images";
    private static final String FILENAME = "{filename: +}";

    private final ImageService imageService;

    @Autowired
    public HomeController(ImageService imageService) {
        this.imageService = imageService;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/" + FILENAME + "/raw")
    public ResponseEntity<?> oneRawImage(@PathVariable String filename) throws IOException {
        Resource file = imageService.findOneImage(filename);

        try {
            return ResponseEntity.ok()
                    .contentLength(file.contentLength())
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(file.getInputStream()));
        } catch (IOException exception) {
            return ResponseEntity.badRequest()
                    .body("Couldn't find " + filename + " => " + exception.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = BASE_PATH)
    public ResponseEntity<?> createFile(@RequestParam("file")MultipartFile file, HttpRequest request) {
        try {
            imageService.createImage(file);

            return ResponseEntity.created(request.getURI().resolve(file.getOriginalFilename() + "/raw"))
                    .body("Successfully upload " + file.getOriginalFilename());
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload " + file.getOriginalFilename() + " => " + exception.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/" + FILENAME)
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        try {
            imageService.deleteImage(filename);

            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("Successfully delete " + filename);
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete " + filename + " => " + exception.getMessage());
        }
    }
}
