package se.elias.pdfservice.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class PdfService {

    public byte[] mergePdfs(List<MultipartFile> files) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (MultipartFile file : files) {
            merger.addSource(new RandomAccessReadBuffer(file.getInputStream()));
        }

        merger.setDestinationStream(output);
        merger.mergeDocuments(null);
        return output.toByteArray();
    }

    public Map<Integer, byte[]> splitPdf(MultipartFile file) throws IOException {
        Map<Integer, byte[]> pages = new LinkedHashMap<>();
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(file.getInputStream()))) {
            Splitter splitter = new Splitter();
            List<PDDocument> splitDocs = splitter.split(document);

            for (int i = 0; i < splitDocs.size(); i++) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                splitDocs.get(i).save(out);
                splitDocs.get(i).close();
                pages.put(i + 1, out.toByteArray());
            }
        }
        return pages;
    }
}