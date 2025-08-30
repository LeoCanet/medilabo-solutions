// Script d'initialisation MongoDB pour Mediscreen
// Création de la base de données et collection pour les notes médicales (Sprint 2)

// Utilisation de la base de données mediscreen_notes
db = db.getSiblingDB('mediscreen_notes');

// Création d'un utilisateur applicatif
db.createUser({
  user: 'mediscreen',
  pwd: 'mediscreen123',
  roles: [
    {
      role: 'readWrite',
      db: 'mediscreen_notes'
    }
  ]
});

// Création de la collection notes avec validation schema
db.createCollection('notes', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['patId', 'patient', 'note'],
      properties: {
        patId: {
          bsonType: 'int',
          description: 'ID du patient - obligatoire et doit être un entier'
        },
        patient: {
          bsonType: 'string',
          description: 'Nom du patient - obligatoire et doit être une chaîne'
        },
        note: {
          bsonType: 'string',
          description: 'Note médicale - obligatoire et doit être une chaîne'
        },
        createdDate: {
          bsonType: 'date',
          description: 'Date de création de la note'
        }
      }
    }
  }
});

// Insertion des données de test (Sprint 2)
// Données correspondant aux patients de test
db.notes.insertMany([
  {
    patId: 1,
    patient: 'TestNone',
    note: "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé",
    createdDate: new Date()
  },
  {
    patId: 2,
    patient: 'TestBorderline',
    note: "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement",
    createdDate: new Date()
  },
  {
    patId: 2,
    patient: 'TestBorderline',
    note: "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois Il remarque également que son audition continue d'être anormale",
    createdDate: new Date()
  },
  {
    patId: 3,
    patient: 'TestInDanger',
    note: "Le patient déclare qu'il fume depuis peu",
    createdDate: new Date()
  },
  {
    patId: 3,
    patient: 'TestInDanger',
    note: "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière Il se plaint également de crises d'apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol LDL élevé",
    createdDate: new Date()
  },
  {
    patId: 4,
    patient: 'TestEarlyOnset',
    note: "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d'être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments",
    createdDate: new Date()
  },
  {
    patId: 4,
    patient: 'TestEarlyOnset',
    note: "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps",
    createdDate: new Date()
  },
  {
    patId: 4,
    patient: 'TestEarlyOnset',
    note: "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C supérieure au niveau recommandé",
    createdDate: new Date()
  },
  {
    patId: 4,
    patient: 'TestEarlyOnset',
    note: "Taille, Poids, Cholestérol, Vertige et Réaction",
    createdDate: new Date()
  }
]);

// Création d'index pour optimiser les performances
db.notes.createIndex({ "patId": 1 });
db.notes.createIndex({ "patient": 1 });
db.notes.createIndex({ "createdDate": -1 });

// Affichage du résultat
print("Base de données MongoDB initialisée avec succès !");
print("Nombre de notes insérées:", db.notes.countDocuments());