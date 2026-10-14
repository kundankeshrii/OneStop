package com.project5.OneStop.collegeprediction.importer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollegeCutoffImportService {

    @Autowired
    private JosaaCutoffImporter josaaCutoffImporter;

    @Autowired
    private UptacCutoffImporter uptacCutoffImporter;

    public void importPreviousYearData() {

        int currentYear =
                java.time.Year.now().getValue();

        int previousYear = currentYear - 1;

        josaaCutoffImporter.importPreviousYear();

        uptacCutoffImporter.importData(previousYear);
    }
}