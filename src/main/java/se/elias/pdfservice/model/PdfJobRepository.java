package se.elias.pdfservice.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PdfJobRepository extends JpaRepository<PdfJob, Long> {
    List<PdfJob> findByStatus(String status);
}