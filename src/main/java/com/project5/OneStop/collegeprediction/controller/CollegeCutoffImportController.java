package com.project5.OneStop.collegeprediction.controller;

import com.project5.OneStop.collegeprediction.importer.JosaaCutoffImporter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/college-cutoffs")
@CrossOrigin("*")
public class CollegeCutoffImportController {

    private final JosaaCutoffImporter josaaCutoffImporter;

    public CollegeCutoffImportController(
            JosaaCutoffImporter josaaCutoffImporter) {

        this.josaaCutoffImporter =
                josaaCutoffImporter;
    }

    @PostMapping("/import/josaa")
    public ResponseEntity<?> importJosaa() {

        int imported =
                josaaCutoffImporter.importPreviousYear();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "JoSAA previous-year data imported successfully",
                        "recordsImported",
                        imported
                )
        );
    }
}