package com.project5.OneStop.collegeprediction.importer;

import com.project5.OneStop.collegeprediction.repository.CollegeCutoffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UptacCutoffImporter implements CollegeCutoffImporter {

    @Autowired
    private CollegeCutoffRepository collegeCutoffRepository;

    @Override
    public void importData(int year) {

        System.out.println(
                "Preparing UPTAC B.Tech cutoff import for year: "
                        + year
        );

        /*
         * IMPORTANT:
         *
         * Do not insert manually-created college data here.
         *
         * The official UPTAC OR/CR document is the source.
         *
         * The document retrieval/parser will populate
         * CollegeCutoff records.
         */
    }
}