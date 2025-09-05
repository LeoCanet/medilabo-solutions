-- Script d'initialisation de la base de données Mediscreen Patient Service
-- Crée uniquement la base de données et les tables (sans données)

-- ===== CRÉATION DE LA BASE DE DONNÉES =====
DROP DATABASE IF EXISTS mediscreen_patients;
CREATE DATABASE mediscreen_patients 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE mediscreen_patients;

-- ===== CRÉATION DES TABLES =====

-- Table des adresses (normalisation 3NF)
CREATE TABLE adresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rue VARCHAR(100),
    ville VARCHAR(50),
    code_postal VARCHAR(10),
    pays VARCHAR(50),
    
    -- Index pour les recherches fréquentes
    INDEX idx_adresse_ville (ville),
    INDEX idx_adresse_code_postal (code_postal),
    INDEX idx_adresse_pays (pays)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des patients
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prenom VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL,
    date_naissance DATE NOT NULL,
    genre CHAR(1) NOT NULL CHECK (genre IN ('M', 'F')),
    telephone VARCHAR(15),
    adresse_id BIGINT,
    
    -- Contraintes
    CONSTRAINT fk_patient_adresse FOREIGN KEY (adresse_id) REFERENCES adresses(id) ON DELETE SET NULL,
    
    -- Index pour les performances (comme défini dans l'entité JPA)
    INDEX idx_patient_nom_prenom (nom, prenom),
    INDEX idx_patient_genre (genre),
    INDEX idx_patient_date_naissance (date_naissance),
    INDEX idx_patient_telephone (telephone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===== VÉRIFICATION =====
SELECT 'Base de données et tables créées avec succès!' as message;
SHOW TABLES;