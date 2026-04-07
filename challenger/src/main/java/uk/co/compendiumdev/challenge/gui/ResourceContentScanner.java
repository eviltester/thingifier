package uk.co.compendiumdev.challenge.gui;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

public class ResourceContentScanner {

    List<String> availableContent = new ArrayList<>();

    public List<String> scanForFullPathsOfExtensionsIn(String folder, String extension) {
        List<String> pathsToFileContent = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().acceptPaths(folder).scan()) {
            scanResult.getResourcesWithExtension(extension).forEach( (Resource res) -> {
                pathsToFileContent.add(res.getPath());
            });
        }
        return pathsToFileContent;
    }

    public Map<String, LocalDate> scanForUrlsWithDates(String folder, String extension) {
        Map<String, LocalDate> urlsWithLastModDates = new HashMap<>();
        try (ScanResult scanResult = new ClassGraph().acceptPaths(folder).scan()) {
            scanResult.getResourcesWithExtension(extension).forEach( (Resource res) -> {
                final LocalDate dateFromFrontMatter = extractDateFromFrontMatter(res);
                urlsWithLastModDates.put(
                        res.getPath().replaceFirst(folder,"").replace("." + extension,""),
                        dateFromFrontMatter == null ?
                                Instant.ofEpochMilli(res.getLastModified()).atZone(ZoneId.systemDefault()).toLocalDate()
                                : dateFromFrontMatter
                );
            });
        }
        return urlsWithLastModDates;
    }

    private LocalDate extractDateFromFrontMatter(final Resource res){

        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(res.open(), StandardCharsets.UTF_8))){

            String line;
            boolean inHeader = false;
            String lastmod = "";
            String date = "";

            while((line = reader.readLine()) != null){
                if(line.equals("---") && !inHeader){
                    inHeader = true;
                    continue;
                }
                if(line.equals("---") && inHeader){
                    break;
                }
                if(!inHeader){
                    // no front matter
                    break;
                }

                if(line.startsWith("lastmod: ")){
                    lastmod = line.replaceFirst("^lastmod:\\s*", "").trim();
                }
                if(line.startsWith("date: ")){
                    date = line.replaceFirst("^date:\\s*", "").trim();
                }
            }

            final LocalDate parsedLastmod = parseDateValue(lastmod);
            if(parsedLastmod != null){
                return parsedLastmod;
            }
            return parseDateValue(date);

        }catch (IOException ignored){
            return null;
        }
    }

    private LocalDate parseDateValue(final String rawDateValue){
        if(rawDateValue==null || rawDateValue.trim().isEmpty()){
            return null;
        }
        final String value = rawDateValue.trim();

        try{
            return LocalDate.parse(value);
        }catch(DateTimeException ignored){
            // try date-time format
        }

        try{
            return OffsetDateTime.parse(value).toLocalDate();
        }catch(DateTimeException ignored){
            return null;
        }
    }
    public void addPathsToAvailableContent(List<String> pathsToFileContent) {
        availableContent.addAll(pathsToFileContent);
    }

}
