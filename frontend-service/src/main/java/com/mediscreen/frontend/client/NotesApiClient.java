package com.mediscreen.frontend.client;

import com.mediscreen.frontend.dto.NoteCreateDto;
import com.mediscreen.frontend.dto.NoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "notes-api", url = "${feign.client.url}")
public interface NotesApiClient {

    @GetMapping("/api/v1/notes/patient/{patId}")
    List<NoteDto> getNotesByPatientId(@PathVariable("patId") Integer patId);

    @GetMapping("/api/v1/notes/{id}")
    NoteDto getNoteById(@PathVariable("id") String id);

    @PostMapping("/api/v1/notes")
    void createNote(@RequestBody NoteCreateDto note);

    @PutMapping("/api/v1/notes/{id}")
    void updateNote(@PathVariable("id") String id, @RequestBody NoteDto note);

    @DeleteMapping("/api/v1/notes/{id}")
    void deleteNote(@PathVariable("id") String id);
}