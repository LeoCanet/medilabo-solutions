package com.mediscreen.notesservice.mapper;

import com.mediscreen.notesservice.dto.NoteCreateDto;
import com.mediscreen.notesservice.dto.NoteDto;
import com.mediscreen.notesservice.entity.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour l'interface {@link NoteMapper}.
 * Vérifie le bon fonctionnement des méthodes de mappage entre les entités Note et les DTOs correspondants.
 */
class NoteMapperTest {

    private NoteMapper noteMapper;

    /**
     * Initialisation du mapper avant chaque test.
     * Utilise MapStruct's Mappers.getMapper pour obtenir une instance du mapper.
     */
    @BeforeEach
    void setUp() {
        noteMapper = Mappers.getMapper(NoteMapper.class);
    }

    /**
     * Teste la conversion d'une entité {@link Note} en {@link NoteDto}.
     */
    @Test
    @DisplayName("toDto - Should map Note entity to NoteDto")
    void toDto_ShouldMapNoteEntityToNoteDto() {
        LocalDateTime createdDate = LocalDateTime.of(2023, 12, 1, 10, 0);

        Note note = Note.builder()
                .id("507f1f77bcf86cd799439011")
                .patId(1)
                .patient("Test TestNone")
                .note("Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé")
                .createdDate(createdDate)
                .build();

        NoteDto dto = noteMapper.toDto(note);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(note.getId());
        assertThat(dto.patId()).isEqualTo(note.getPatId());
        assertThat(dto.patient()).isEqualTo(note.getPatient());
        assertThat(dto.note()).isEqualTo(note.getNote());
        assertThat(dto.createdDate()).isEqualTo(note.getCreatedDate());
    }

    /**
     * Teste la conversion d'une liste d'entités {@link Note} en une liste de {@link NoteDto}.
     */
    @Test
    @DisplayName("toDtoList - Should map list of Note entities to list of NoteDto")
    void toDtoList_ShouldMapListOfNoteEntitiesToListOfNoteDto() {
        Note note1 = Note.builder()
                .id("507f1f77bcf86cd799439011")
                .patId(1)
                .patient("Test TestNone")
                .note("Première note")
                .build();

        Note note2 = Note.builder()
                .id("507f1f77bcf86cd799439012")
                .patId(2)
                .patient("Test TestBorderline")
                .note("Deuxième note")
                .build();

        List<Note> notes = Arrays.asList(note1, note2);
        List<NoteDto> dtos = noteMapper.toDtoList(notes);

        assertThat(dtos).isNotNull().hasSize(2);
        assertThat(dtos.get(0).id()).isEqualTo(note1.getId());
        assertThat(dtos.get(1).id()).isEqualTo(note2.getId());
    }

    /**
     * Teste la conversion d'un {@link NoteCreateDto} en une entité {@link Note}.
     */
    @Test
    @DisplayName("toEntity - Should map NoteCreateDto to Note entity")
    void toEntity_ShouldMapNoteCreateDtoToNoteEntity() {
        NoteCreateDto createDto = NoteCreateDto.of(
                1,
                "Test TestNone",
                "Le patient déclare qu'il ressent beaucoup de stress au travail"
        );

        Note entity = noteMapper.toEntity(createDto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // ID doit être nul pour une nouvelle entité
        assertThat(entity.getPatId()).isEqualTo(createDto.patId());
        assertThat(entity.getPatient()).isEqualTo(createDto.patient());
        assertThat(entity.getNote()).isEqualTo(createDto.note());
        assertThat(entity.getCreatedDate()).isNotNull(); // Date générée automatiquement
    }

    /**
     * Teste la conversion d'un {@link NoteDto} en une entité {@link Note} pour une mise à jour.
     */
    @Test
    @DisplayName("toEntity - Should map NoteDto to Note entity for update")
    void toEntity_ShouldMapNoteDtoToNoteEntityForUpdate() {
        LocalDateTime createdDate = LocalDateTime.now();
        NoteDto noteDto = NoteDto.of(
                "507f1f77bcf86cd799439011",
                2,
                "Test TestBorderline",
                "Note mise à jour avec termes: Cholestérol, Hémoglobine A1C",
                createdDate
        );

        Note entity = noteMapper.toEntity(noteDto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(noteDto.id());
        assertThat(entity.getPatId()).isEqualTo(noteDto.patId());
        assertThat(entity.getPatient()).isEqualTo(noteDto.patient());
        assertThat(entity.getNote()).isEqualTo(noteDto.note());
        assertThat(entity.getCreatedDate()).isEqualTo(noteDto.createdDate());
    }

    /**
     * Teste la mise à jour d'une entité {@link Note} existante à partir d'un {@link NoteDto}.
     * Vérifie que les champs non nuls du DTO mettent à jour l'entité, tandis que les champs nuls sont ignorés.
     */
    @Test
    @DisplayName("updateNoteFromDto - Should update existing Note entity from NoteDto ignoring nulls")
    void updateNoteFromDto_ShouldUpdateExistingNoteEntityFromNoteDtoIgnoringNulls() {
        LocalDateTime originalDate = LocalDateTime.of(2023, 12, 1, 10, 0);

        Note existingNote = Note.builder()
                .id("507f1f77bcf86cd799439011")
                .patId(1)
                .patient("Original Patient")
                .note("Note originale")
                .createdDate(originalDate)
                .build();

        NoteDto updateDto = NoteDto.of(
                "507f1f77bcf86cd799439011",
                2, // Nouveau patId
                null, // Patient null - doit être ignoré
                "Note mise à jour avec termes diabète", // Nouvelle note
                null // Date null - doit être ignorée
        );

        noteMapper.updateNoteFromDto(updateDto, existingNote);

        assertThat(existingNote).isNotNull();
        assertThat(existingNote.getId()).isEqualTo("507f1f77bcf86cd799439011"); // ID préservé
        assertThat(existingNote.getPatId()).isEqualTo(2); // Mis à jour
        assertThat(existingNote.getPatient()).isEqualTo("Original Patient"); // Préservé (null dans DTO)
        assertThat(existingNote.getNote()).isEqualTo("Note mise à jour avec termes diabète"); // Mis à jour
        assertThat(existingNote.getCreatedDate()).isEqualTo(originalDate); // Préservé (ignoré dans mapping)
    }

    /**
     * Teste le mapping avec des termes déclencheurs diabète (conformité OpenClassrooms).
     */
    @Test
    @DisplayName("toDto - Should correctly map note with diabetes trigger terms")
    void toDto_ShouldMapNotesWithDiabetesTriggerTerms() {
        Note noteWithTriggers = Note.builder()
                .id("507f1f77bcf86cd799439011")
                .patId(2)
                .patient("Test TestBorderline")
                .note("Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement. Hémoglobine A1C supérieure au niveau recommandé. Cholestérol élevé.")
                .createdDate(LocalDateTime.now())
                .build();

        NoteDto dto = noteMapper.toDto(noteWithTriggers);

        assertThat(dto).isNotNull();
        assertThat(dto.note()).contains("Hémoglobine A1C");
        assertThat(dto.note()).contains("Cholestérol");
        assertThat(dto.note()).contains("anormal");
    }

    /**
     * Teste le mapping avec formatage original conservé (exigence OpenClassrooms).
     */
    @Test
    @DisplayName("toEntity - Should preserve original formatting in note content")
    void toEntity_ShouldPreserveOriginalFormatting() {
        NoteCreateDto createDto = NoteCreateDto.of(
                3,
                "Test TestInDanger",
                "Le patient déclare qu'il fume depuis peu\n\nLe patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière\nIl se plaint également de crises d'apnée respiratoire anormales\n\nTests de laboratoire indiquant un taux de cholestérol LDL élevé"
        );

        Note entity = noteMapper.toEntity(createDto);

        assertThat(entity).isNotNull();
        assertThat(entity.getNote()).contains("\n\n");
        assertThat(entity.getNote()).contains("fume");
        assertThat(entity.getNote()).contains("cholestérol");
        assertThat(entity.getNote()).contains("anormal");
    }
}