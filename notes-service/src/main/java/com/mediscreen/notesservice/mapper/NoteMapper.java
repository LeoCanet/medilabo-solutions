package com.mediscreen.notesservice.mapper;

import com.mediscreen.notesservice.dto.*;
import com.mediscreen.notesservice.entity.Note;
import org.mapstruct.*;
import java.util.List;

/**
 * Mapper automatique avec MapStruct 1.6.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    builder = @Builder(disableBuilder = true)  // Utilise les Records au lieu de builders
)
public interface NoteMapper {
    
    // === NOTE MAPPINGS ===
    
    /**
     * Conversion Note -> NoteDto
     */
    NoteDto toDto(Note note);
    
    /**
     * Conversion NoteCreateDto -> Note
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())")
    Note toEntity(NoteCreateDto createDto);
    
    /**
     * Conversion NoteDto -> Note (pour updates)
     */
    Note toEntity(NoteDto noteDto);

    /**
     * Conversion List<Note> -> List<NoteDto>
     */
    List<NoteDto> toDtoList(List<Note> notes);
    
    // === UPDATE MAPPINGS (MapStruct 1.6.0) ===
    
    /**
     * Met à jour une entité Note existante avec les données d'un DTO
     * Ignore les valeurs null du DTO (mise à jour partielle)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    void updateNoteFromDto(NoteDto dto, @MappingTarget Note entity);
}