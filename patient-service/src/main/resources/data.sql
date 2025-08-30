-- Données de test pour les cas d'évaluation du diabète

-- Insertion des adresses de test
INSERT IGNORE INTO adresses (rue, ville, code_postal, pays) VALUES
('1 Brookside St', 'Anytown', '12345', 'USA'),
('2 High St', 'Anytown', '12345', 'USA'),
('3 Club Road', 'Anytown', '12345', 'USA'),
('4 Valley Dr', 'Anytown', '12345', 'USA');

-- Insertion des patients de test
-- adresse_id correspond à l'ordre d'insertion des adresses (1-4)
INSERT IGNORE INTO patients (prenom, nom, date_naissance, genre, telephone, adresse_id) VALUES
('Test', 'TestNone', '1966-12-31', 'F', '100-222-3333', 1),
('Test', 'TestBorderline', '1945-06-24', 'M', '200-333-4444', 2),
('Test', 'TestInDanger', '2004-06-18', 'M', '300-444-5555', 3),
('Test', 'TestEarlyOnset', '2002-06-28', 'F', '400-555-6666', 4);
