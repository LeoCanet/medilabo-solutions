package com.mediscreen.frontend.dto;

/**
 * Enum RiskLevel pour frontend (copie conforme de assessment-service)
 * Les 4 niveaux de risque diabète obligatoires OpenClassrooms
 */
public enum RiskLevel {
    NONE("None", "Aucun risque", "success"),
    BORDERLINE("Borderline", "Risque limité", "warning"),
    IN_DANGER("In Danger", "En danger", "danger"),
    EARLY_ONSET("Early onset", "Apparition précoce", "dark");

    private final String code;
    private final String description;
    private final String cssClass;

    RiskLevel(String code, String description, String cssClass) {
        this.code = code;
        this.description = description;
        this.cssClass = cssClass;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getCssClass() {
        return cssClass;
    }

    @Override
    public String toString() {
        return code;
    }
}
