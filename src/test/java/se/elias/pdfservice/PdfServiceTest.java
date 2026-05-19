package se.elias.pdfservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import se.elias.pdfservice.service.PdfService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PdfServiceTest {

    @Autowired
    private PdfService pdfService;

    @Test
    public void merge_two_pdfs_should_return_valid_pdf_bytes() throws IOException {
        byte[] pdf1 = TestPdfGenerator.createSimplePdf("Test dokument 1");
        byte[] pdf2 = TestPdfGenerator.createSimplePdf("Test dokument 2");

        MockMultipartFile file1 = new MockMultipartFile(
                "file1", "test1.pdf", "application/pdf", pdf1
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", "test2.pdf", "application/pdf", pdf2
        );

        byte[] result = pdfService.mergePdfs(List.of(file1, file2));

        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals('%', (char) result[0]);
        assertEquals('P', (char) result[1]);
        assertEquals('D', (char) result[2]);
        assertEquals('F', (char) result[3]);
    }

    @Test
    public void split_pdf_should_return_correct_number_of_pages() throws IOException {
        byte[] pdfBytes = TestPdfGenerator.createSimplePdf("Sida 1");

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfBytes
        );

        Map<Integer, byte[]> pages = pdfService.splitPdf(file);

        assertNotNull(pages);
        assertEquals(1, pages.size());
        assertTrue(pages.containsKey(1));
    }

    @Test
    public void merge_should_combine_multiple_files() throws IOException {
        byte[] pdf1 = TestPdfGenerator.createSimplePdf("Sida A");
        byte[] pdf2 = TestPdfGenerator.createSimplePdf("Sida B");
        byte[] pdf3 = TestPdfGenerator.createSimplePdf("Sida C");

        MockMultipartFile file1 = new MockMultipartFile("f1", "a.pdf", "application/pdf", pdf1);
        MockMultipartFile file2 = new MockMultipartFile("f2", "b.pdf", "application/pdf", pdf2);
        MockMultipartFile file3 = new MockMultipartFile("f3", "c.pdf", "application/pdf", pdf3);

        byte[] result = pdfService.mergePdfs(List.of(file1, file2, file3));

        assertTrue(result.length > pdf1.length);
    }
}