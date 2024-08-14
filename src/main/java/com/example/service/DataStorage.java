package com.example.service;

import com.example.model.Note;
import com.example.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {

    private static final String NOTES_FILE_PATH = "notes.json";
    private static final String EVENTS_FILE_PATH = "events.json";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // Register the module for Java 8 Date/Time
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)  // Optional: if you want human-readable date formats
            .enable(SerializationFeature.INDENT_OUTPUT);  // To format the output JSON nicely

    public static void saveNotes(List<Note> notes) throws IOException {
        objectMapper.writeValue(new File(NOTES_FILE_PATH), notes);
    }

    public static List<Note> loadNotes() {
        File file = new File(NOTES_FILE_PATH);
        if (!file.exists()) {
            System.out.println("Notes file does not exist.");
            return new ArrayList<>(); // Return an empty list if the file doesn't exist
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Note.class);
            return objectMapper.readValue(file, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return an empty list in case of an error
        }
    }

    public static void saveEvents(List<Event> events) throws IOException {
        objectMapper.writeValue(new File(EVENTS_FILE_PATH), events);
    }

    public static List<Event> loadEvents() {
        File file = new File(EVENTS_FILE_PATH);
        if (!file.exists()) {
            System.out.println("Events file does not exist.");
            return new ArrayList<>(); // Return an empty list if the file doesn't exist
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class);
            return objectMapper.readValue(file, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return an empty list in case of an error
        }
    }
}