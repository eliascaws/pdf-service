package se.elias.pdfservice.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.elias.pdfservice.service.PdfService;
import se.elias.pdfservice.service.S3Service;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;
    private final S3Service s3Service;

    public PdfController(PdfService pdfService, S3Service s3Service) {
        this.pdfService = pdfService;
        this.s3Service = s3Service;
    }

    @PostMapping("/merge")
    public ResponseEntity<?> mergePdfs(
            @RequestParam("files") List<MultipartFile> files) {
        try {
            byte[] result = pdfService.mergePdfs(files);
            String downloadUrl = s3Service.uploadAndGetUrl(result, "merged.pdf");
            return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/split")
    public ResponseEntity<?> splitPdf(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<Integer, byte[]> pages = pdfService.splitPdf(file);
            Map<Integer, String> result = new LinkedHashMap<>();

            pages.forEach((pageNum, pageBytes) -> {
                String url = s3Service.uploadAndGetUrl(pageBytes, "sida_" + pageNum + ".pdf");
                result.put(pageNum, url);
            });

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}