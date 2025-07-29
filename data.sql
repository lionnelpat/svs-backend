INSERT INTO payment_methods 
    (actif, active, created_at, updated_at, code, nom, description, created_by, updated_by)
VALUES
    (true, true, NOW(), NOW(), 'CASH', 'Paiement en espèces', 'Paiement directement effectué en espèces au guichet.', 'admin', 'admin'),
    (true, true, NOW(), NOW(), 'WAVE', 'Paiement via WAVE Money', 'Paiement effectué via l’application WAVE Money.', 'admin', 'admin'),
    (true, true, NOW(), NOW(), 'OM', 'Paiement via ORANGE Money', 'Paiement effectué via l’application ORANGE Money.', 'admin', 'admin'),
    (true, true, NOW(), NOW(), 'VIREMENT', 'Paiement par virement bancaire', 'Paiement effectué par virement bancaire sur le compte de l’entreprise.', 'admin', 'admin');

INSERT INTO expense_categories 
    (active, created_at, updated_at, code, nom, description, created_by, updated_by)
VALUES
    (true, NOW(), NOW(), 'FUEL', 'Carburant et lubrifiants', 'Dépenses liées à l’achat de carburant et lubrifiants pour les opérations maritimes.', 'admin', 'admin'),
    (true, NOW(), NOW(), 'REP', 'Réparations et maintenance', 'Frais pour la réparation et la maintenance des navires et équipements.', 'admin', 'admin'),
    (true, NOW(), NOW(), 'FRAISPORT', 'Frais portuaires', 'Paiement des taxes et frais liés aux services portuaires.', 'admin', 'admin'),
    (true, NOW(), NOW(), 'SALAIRES', 'Salaires et charges sociales', 'Paiement des salaires, cotisations sociales et primes du personnel.', 'admin', 'admin'),
    (true, NOW(), NOW(), 'FOURNITURES', 'Fournitures et consommables', 'Achats de fournitures de bureau et consommables nécessaires au fonctionnement.', 'admin', 'admin');


INSERT INTO operations 
    (active, prix_euro, prix_xof, created_at, updated_at, code, nom, description, created_by, updated_by)
VALUES
    (true, 0, 250000, NOW(), NOW(), 'ACCOST', 'Assistance à l’accostage', 'Assistance technique pour manœuvrer et accoster le navire au port.', 'admin', 'admin'),
    (true, 0, 150000, NOW(), NOW(), 'DEP', 'Assistance au départ', 'Assistance technique pour le départ et le dégagement du quai.', 'admin', 'admin'),
    (true, 0, 500000, NOW(), NOW(), 'REMORQ', 'Service de remorquage', 'Remorquage du navire dans la zone portuaire.', 'admin', 'admin'),
    (true, 0, 200000, NOW(), NOW(), 'VEILLE', 'Veille et surveillance', 'Surveillance et veille du navire pendant le séjour au port.', 'admin', 'admin'),
    (true, 0, 300000, NOW(), NOW(), 'AVIT', 'Avitaillement', 'Fourniture de vivres, eau et carburant au navire.', 'admin', 'admin');


INSERT INTO expense_suppliers 
    (active, created_at, updated_at, ninea, telephone, created_by, rccm, updated_by, email, nom, adresse)
VALUES
    (true, NOW(), NOW(), 'SN123456789', '+221771234567', 'admin', 'SN-DKR-2025-A1', 'admin', 'contact@senpetrol.sn', 'SEN PETROL SERVICES', 'Km 4, Boulevard du Centenaire, Dakar'),
    (true, NOW(), NOW(), 'SN987654321', '+221776543210', 'admin', 'SN-DKR-2025-B2', 'admin', 'info@maritech.sn', 'MARITECH SARL', 'Zone portuaire, Dakar'),
    (true, NOW(), NOW(), 'SN112233445', '+221775551122', 'admin', 'SN-DKR-2025-C3', 'admin', 'compta@dakarrepair.sn', 'DAKAR REPAIR & MAINTENANCE', 'Route du Port, Dakar'),
    (true, NOW(), NOW(), 'SN556677889', '+221778899445', 'admin', 'SN-DKR-2025-D4', 'admin', 'contact@africaship.sn', 'AFRICA SHIP SUPPLY', 'Port Autonome de Dakar'),
    (true, NOW(), NOW(), 'SN998877665', '+221770011223', 'admin', 'SN-DKR-2025-E5', 'admin', 'contact@fournituresdakar.sn', 'FOURNITURES DAKAR', 'Avenue Blaise Diagne, Dakar');


INSERT INTO companies
(id, active, created_at, updated_at, ninea, telephone, telephone_contact, created_by, rccm, updated_by, contact_principal, email, email_contact, nom, pays, ville, raison_sociale, adresse, site_web)
VALUES
    (1,true, NOW(), NOW(), 'SN001122334', '+221 77 123 45 67', '+221 77 234 56 78', 'admin', 'DKR2025-001', 'admin', 'Fatou NDIAYE', 'contact@bollore.sn', 'fatou.ndiaye@bollore.sn', 'Bolloré Transport & Logistics', 'Sénégal', 'Dakar', 'Logistique et transport maritime', 'Avenue du Port, Dakar', 'www.bollore-africa.com'),

    (2, true, NOW(), NOW(), 'SN223344556', '+221 76 987 65 43', '+221 76 876 54 32', 'admin', 'DKR2025-002', 'admin', 'Ibrahima SARR', 'contact@maersk.sn', 'ibrahima.sarr@maersk.sn', 'MAERSK Sénégal', 'Sénégal', 'Dakar', 'Compagnie maritime et transit', 'Zone portuaire, Mole 10, Dakar', 'www.maersk.com'),

    (3, true, NOW(), NOW(), 'SN334455667', '+221 70 998 77 66', '+221 70 887 66 55', 'admin', 'DKR2025-003', 'admin', 'Aminata BA', 'info@senamar.sn', 'aminata.ba@senamar.sn', 'SENAMAR', 'Sénégal', 'Dakar', 'Agence maritime et consignation', 'Rue du Port, Dakar', 'www.senamar.sn'),

    (4, true, NOW(), NOW(), 'SN445566778', '+221 78 112 23 34', '+221 78 223 34 45', 'admin', 'DKR2025-004', 'admin', 'Mamadou DIALLO', 'contact@transafrica.sn', 'mamadou.diallo@transafrica.sn', 'TransAfrica Shipping', 'Sénégal', 'Dakar', 'Transport et affrètement maritime', 'Km 5, Route de Rufisque, Dakar', 'www.transafrica.sn'),

    (5, true, NOW(), NOW(), 'SN556677889', '+221 75 334 45 56', '+221 75 445 56 67', 'admin', 'DKR2025-005', 'admin', 'Khady GUEYE', 'info@dakarlogistics.sn', 'khady.gueye@dakarlogistics.sn', 'Dakar Logistics Services', 'Sénégal', 'Dakar', 'Prestataire logistique et maritime', 'Sacré-Cœur 3, Dakar', 'www.dakarlogistics.sn');

INSERT INTO ships
    (id, active, nombre_passagers, compagnie_id, created_at, updated_at, numero_mmsi, numero_imo, numero_appel, classification, created_by, pavillon, type_navire, updated_by, nom, port_attache)
VALUES
    (2, true, 12, 2, NOW(), NOW(), '636015001', 'IMO1234567', 'D5AB1', 'BUREAU_VERITAS', 'admin', 'LIBERIA', 'CARGO', 'admin', 'Dakar Trader', 'Port Autonome de Dakar'),

    (3, true, 0, 2, NOW(), NOW(), '636015002', 'IMO1234568', 'D5AB2', 'DNV_GL', 'admin', 'LIBERIA', 'PETROLIER', 'admin', 'Atlantic Fuel', 'Port Autonome de Dakar'),

    (4, true, 50, 3, NOW(), NOW(), '636015003', 'IMO1234569', 'D5AB3', 'ABS', 'admin', 'PANAMA', 'RO_RO', 'admin', 'Dakar Carrier', 'Port Autonome de Dakar'),

    (5, true, 8, 3, NOW(), NOW(), '636015004', 'IMO1234570', 'D5AB4', 'CLASS_NK', 'admin', 'SENEGAL', 'REMORQUEUR', 'admin', 'Sen Tug 1', 'Dakar'),

    (6, true, 0, 4, NOW(), NOW(), '636015005', 'IMO1234571', 'D5AB5', 'RINA', 'admin', 'BAHAMAS', 'VRAQUEUR', 'admin', 'Africa Bulk', 'Port Autonome de Dakar'),

    (7, true, 0, 4, NOW(), NOW(), '636015006', 'IMO1234572', 'D5AB6', 'CCS', 'admin', 'MALTA', 'CONTENEUR', 'admin', 'Dakar Box', 'Dakar'),

    (8, true, 100, 5, NOW(), NOW(), '636015007', 'IMO1234573', 'D5AB7', 'RS', 'admin', 'SENEGAL', 'PASSAGERS', 'admin', 'Casamance Express', 'Ziguinchor'),

    (9, true, 10, 5, NOW(), NOW(), '636015008', 'IMO1234574', 'D5AB8', 'KR', 'admin', 'SENEGAL', 'REMORQUEUR', 'admin', 'Sen Tug 2', 'Dakar'),

    (10, true, 0, 1, NOW(), NOW(), '636015009', 'IMO1234575', 'D5AB9', 'IRS', 'admin', 'GREECE', 'PETROLIER', 'admin', 'Petro Dakar', 'Port Autonome de Dakar'),

    (11, true, 60, 1, NOW(), NOW(), '636015010', 'IMO1234576', 'D5AC0', 'LLOYDS_REGISTER', 'admin', 'FRANCE', 'PASSAGERS', 'admin', 'Cap Skirring', 'Cap Skirring');




