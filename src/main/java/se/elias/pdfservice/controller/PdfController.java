package se.elias.pdfservice.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.elias.pdfservice.service.PdfService;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(
            @RequestParam("files") List<MultipartFile> files) {
        try {
            byte[] result = pdfService.mergePdfs(files);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename("merged.pdf").build());
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/split")
    public ResponseEntity<Map<Integer, String>> splitPdf(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<Integer, byte[]> pages = pdfService.splitPdf(file);
            Map<Integer, String> result = new LinkedHashMap<>();
            pages.forEach((k, v) ->
                    result.put(k, Base64.getEncoder().encodeToString(v)));
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}