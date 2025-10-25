package com.example.movie.review.analyser;

import com.example.movie.review.analyser.model.*;
import com.example.movie.review.analyser.repository.*;
import com.example.movie.review.analyser.service.SentimentAnalysisService;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.apache.poi.util.IOUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@Profile("!test") 
public class DataInitializer implements CommandLineRunner {

    // --- All Autowired Repositories ---
    @Autowired private MovieRepository movieRepository;
    @Autowired private PersonRepository personRepository;
    @Autowired private CrewCreditRepository crewCreditRepository;
    @Autowired private ExternalReviewRepository externalReviewRepository;
    @Autowired private SimilarMovieRepository similarMovieRepository;
    @Autowired private SentimentAnalysisService sentimentAnalysisService;
  

    @Override
    public void run(String... args) throws Exception {
    	IOUtils.setByteArrayMaxOverride(900_000_000);
        if (movieRepository.count() > 0) {
            System.out.println("Local H2 database already populated. Skipping data load.");
            return;
        }
        System.out.println("Local H2 database is empty. Starting high-performance data warehouse load...");

        // Process files using the efficient streaming method
        processSheet("data/MOVIE.xlsx", this::saveMoviesInBatch);
        processSheet("data/PERSON.xlsx", this::savePersonsInBatch);
        processSheet("data/CREW_CREDIT.xlsx", this::saveCrewCreditsInBatch);
        processSheet("data/MOVIE_REVIEW.xlsx", this::saveExternalReviewsInBatch);
        processSheet("data/MOVIE_SIMILAR.xlsx", this::saveSimilarMoviesInBatch);
        
        System.out.println("\n******************************************");
        System.out.println("DATA WAREHOUSE LOAD COMPLETE!");
        System.out.println("******************************************");
    }

    // --- Batch Saving Logic ---
    private void saveMoviesInBatch(List<String[]> batch) {
        List<Movie> movies = new ArrayList<>();
        for (String[] row : batch) {
            try {
                Movie movie = new Movie();
                movie.setFilmid(Integer.parseInt(row[1]));
                movie.setTitle(row[2]);
                movie.setOverview(row[10]);
                movie.setPosterPath(row[12]);
                movie.setReleaseDate(row[13]);
                movie.setVoteAverage(Double.parseDouble(row[16]));
                movie.setVoteCount(Integer.parseInt(row[17]));
                movies.add(movie);
            } catch (Exception e) {/* Skip row on parse error */}
        }
        if (!movies.isEmpty()) movieRepository.saveAll(movies);
    }
    // ... (Similar batch saving methods for Person, CrewCredit, etc.)

    // --- High-Performance Streaming XLSX Processor ---
    public void processSheet(String filePath, Consumer<List<String[]>> batchProcessor) throws Exception {
        System.out.println("Streaming " + filePath + "...");
        try (OPCPackage pkg = OPCPackage.open(new ClassPathResource(filePath).getInputStream())) {
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = (SharedStringsTable) r.getSharedStringsTable();
            XMLReader parser = XMLReaderFactory.createXMLReader();
            ContentHandler handler = new SheetHandler(sst, batchProcessor);
            parser.setContentHandler(handler);

            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) r.getSheetsData();
            if (sheets.hasNext()) { // Process only the first sheet
                try (InputStream sheetStream = sheets.next()) {
                    InputSource sheetSource = new InputSource(sheetStream);
                    parser.parse(sheetSource);
                }
            }
        }
        System.out.println("-> Finished streaming " + filePath);
    }

    // Inner class to handle the XML parsing events from the sheet
    private static class SheetHandler extends DefaultHandler {
        private final SharedStringsTable sst;
        private final Consumer<List<String[]>> batchProcessor;
        private String lastContents;
        private boolean nextIsString;
        private List<String> currentRow = new ArrayList<>();
        private List<String[]> batch = new ArrayList<>();
        private final int BATCH_SIZE = 2000;
        private long rowCount = 0;

        private SheetHandler(SharedStringsTable sst, Consumer<List<String[]>> batchProcessor) {
            this.sst = sst;
            this.batchProcessor = batchProcessor;
        }

        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if (name.equals("c")) { // "c" is a cell
                String cellType = attributes.getValue("t");
                nextIsString = (cellType != null && cellType.equals("s"));
            }
            lastContents = "";
        }

        public void endElement(String uri, String localName, String name) {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getItemAt(idx).getString()).toString();
                nextIsString = false;
            }
            if (name.equals("v")) { // "v" is the cell value
                currentRow.add(lastContents);
            } else if (name.equals("row")) { // "row" is the end of a row
                if (rowCount > 0) { // Skip header row
                    batch.add(currentRow.toArray(new String[0]));
                    if (batch.size() >= BATCH_SIZE) {
                        batchProcessor.accept(batch);
                        batch.clear();
                    }
                }
                currentRow.clear();
                rowCount++;
            }
        }

        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }

        @Override
        public void endDocument() {
            if (!batch.isEmpty()) { // Process any remaining rows
                batchProcessor.accept(batch);
            }
        }
    }
    
    // --- The other batch saving methods ---
    private void savePersonsInBatch(List<String[]> batch) { /* ... similar logic to saveMoviesInBatch ... */ }
    private void saveCrewCreditsInBatch(List<String[]> batch) { /* ... similar logic to saveMoviesInBatch ... */ }
    private void saveExternalReviewsInBatch(List<String[]> batch) { /* ... similar logic to saveMoviesInBatch ... */ }
    private void saveSimilarMoviesInBatch(List<String[]> batch) { /* ... similar logic to saveMoviesInBatch ... */ }
}