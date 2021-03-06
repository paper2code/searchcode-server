/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.OWASPMatchingResult;
import com.searchcode.app.model.OWASPResult;
import com.searchcode.app.service.Singleton;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OWASPClassifier {

    private final LoggerWrapper logger;
    private String DATABASEPATH = Properties.getProperties().getProperty(Values.OWASPDATABASELOCATION, Values.DEFAULTOWASPDATABASELOCATION);
    private ArrayList<OWASPResult> database;

    public OWASPClassifier() {
        this.database = this.loadDatabase();
        this.logger = Singleton.getLogger();
    }

    /**
     * Matches lines of code against all the matching OWASP rules that exist in the database and returns the matching
     * rules and the lines of code that are matched against.
     */
    public ArrayList<OWASPMatchingResult> classifyCode(List<String> codeLines, String languageName) {
        var matching = new ArrayList<OWASPMatchingResult>();

        if (codeLines == null || codeLines.isEmpty()) {
            return matching;
        }

        for (var result : this.database.stream().filter(x -> x.lang.equalsIgnoreCase(languageName) || x.lang.isEmpty()).collect(Collectors.toList())) {
            for (int i = 0; i < codeLines.size(); i++) {
                if (codeLines.get(i).contains(result.name)) {

                    // If the matching result already exists, just add the line number, otherwise
                    var matchingResult = this.getMatchingResult(matching, result.name);

                    if (matchingResult != null) {
                        matchingResult.addMatchingLine(i + 1);
                    } else {
                        matching.add(new OWASPMatchingResult(result.name, result.desc, result.type, i + 1));
                    }
                }
            }
        }
        return matching;
    }

    public List<OWASPResult> getDatabase() {
        return this.database;
    }

    public void clearDatabase() {
        this.database.clear();
    }

    public void addToDatabase(OWASPResult result) {
        this.database.add(result);
    }

    private OWASPMatchingResult getMatchingResult(ArrayList<OWASPMatchingResult> results, String name) {
        for (OWASPMatchingResult result : results) {
            if (result.getName().equals(name)) {
                return result;
            }
        }

        return null;
    }

    /**
     * Loads the OWASP database from a JSON file on disk
     */
    private ArrayList<OWASPResult> loadDatabase() {
        var db = new ArrayList<OWASPResult>();

        try {
            var gson = new GsonBuilder().create();
            var myArray = gson.fromJson(new FileReader(this.DATABASEPATH), OWASPResult[].class);
            db = new ArrayList<>(Arrays.asList(myArray));
        } catch (FileNotFoundException | JsonSyntaxException ex) {
            this.logger.severe(String.format("5be6cc58::error in class %s exception %s unable to load owasp database", ex.getClass(), ex.getMessage()));
        }

        return db;
    }
}
