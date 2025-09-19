package com.mediscreen.notesservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Document(collection = "notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    private String id;

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be positive")
    @Field("patId")
    private Integer patId;

    @NotBlank(message = "Patient name is required")
    @Field("patient")
    private String patient;

    @NotBlank(message = "Note content is required")
    @Field("note")
    private String note;

    @Field("createdDate")
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}