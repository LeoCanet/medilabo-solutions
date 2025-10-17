package com.mediscreen.assessmentservice.service;

import com.mediscreen.assessmentservice.enums.RiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Calculateur de risque diabète (algorithme pur)
 *
 * Responsabilité UNIQUE : Appliquer les règles métier OpenClassrooms
 * AUCUNE dépendance externe (pas de clients Feign, pas de repository)
 *
 * Architecture : Séparation claire algorithme vs orchestration
 * - Cette classe = Algorithme pur (calcul risque)
 * - AssessmentService = Orchestrateur (coordination appels externes)
 *
 * Facilite les tests : entrées simples (âge, genre, termes) → sortie (RiskLevel)
 * Tests sans mocks, rapides et exhaustifs
 */
@Slf4j
@Component
public class DiabetesRiskCalculator {

    /**
     * Calcule le niveau de risque diabète selon l'algorithme OpenClassrooms
     *
     * Règles exactes :
     * - NONE: 0 terme déclencheur
     * - BORDERLINE: 2-5 termes ET >30 ans
     * - IN_DANGER:
     *   * Homme <=30 ans: 3-4 termes
     *   * Femme <=30 ans: 4-6 termes
     *   * >30 ans: 6-7 termes
     * - EARLY_ONSET:
     *   * Homme <=30 ans: 5+ termes
     *   * Femme <=30 ans: 7+ termes
     *   * >30 ans: 8+ termes
     *
     * @param age âge du patient en années
     * @param isMale true si le patient est un homme
     * @param triggerTermsCount nombre de termes déclencheurs uniques
     * @return niveau de risque calculé
     */
    public RiskLevel calculateRisk(int age, boolean isMale, int triggerTermsCount) {
        log.debug("Calcul risque: âge={}, male={}, termes={}", age, isMale, triggerTermsCount);

        // None: Aucun terme déclencheur
        if (triggerTermsCount == 0) {
            return RiskLevel.NONE;
        }

        // Patients > 30 ans
        if (age > 30) {
            if (triggerTermsCount >= 2 && triggerTermsCount <= 5) {
                return RiskLevel.BORDERLINE;
            } else if (triggerTermsCount >= 6 && triggerTermsCount <= 7) {
                return RiskLevel.IN_DANGER;
            } else if (triggerTermsCount >= 8) {
                return RiskLevel.EARLY_ONSET;
            }
        }
        // Patients <= 30 ans
        else {
            if (isMale) {
                if (triggerTermsCount >= 3 && triggerTermsCount <= 4) {
                    return RiskLevel.IN_DANGER;
                } else if (triggerTermsCount >= 5) {
                    return RiskLevel.EARLY_ONSET;
                }
            } else {
                if (triggerTermsCount >= 4 && triggerTermsCount <= 6) {
                    return RiskLevel.IN_DANGER;
                } else if (triggerTermsCount >= 7) {
                    return RiskLevel.EARLY_ONSET;
                }
            }
        }

        // Cas par défaut (nombre de termes insuffisant pour déclencher un risque)
        return RiskLevel.NONE;
    }
}
