#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate complete educational content for James Academy and James Chat:
- 232 subjects across 6 domains with detailed chapters, real examples, exercises, quizzes, exams
- 40 fully-playable educational games with rules, timers, difficulties, questions, feedback
"""

import json
import os

# ==========================================
# 1. SUBJECTS DEFINITIONS (232 subjects)
# ==========================================

DOMAINS_CONFIG = [
    {
        "id": "histoire_geographie",
        "title": "Histoire et Géographie",
        "icon": "🏛️",
        "description": "Exploration du passé humain, des civilisations et de l'espace terrestre.",
        "subjects": [
            # Histoire (1-30)
            ("Histoire d’Haïti", "Histoire", "🇭🇹", "De l'ère précolombienne taïno jusqu'à la période contemporaine, en passant par 1804."),
            ("Histoire mondiale", "Histoire", "🌍", "Les grands tournants de l'humanité, du néolithique à l'ère numérique contemporaine."),
            ("Histoire de l’Afrique", "Histoire", "🌍", "Berceau de l'humanité, empires du Mali, de Songhaï, Éthiopie et décolonisation."),
            ("Histoire de l’Europe", "Histoire", "🏰", "De l'Antiquité classique aux traités européens modernes en passant par la féodalité."),
            ("Histoire des Amériques", "Histoire", "🌎", "Civilisations précolombiennes (Mayas, Aztèques, Incas) et évolutions continentales."),
            ("Histoire de l’Asie", "Histoire", "🏯", "Dynasties chinoises, route de la soie, ère Meiji au Japon et empires indiens."),
            ("Histoire de l’Océanie", "Histoire", "🌊", "Navigation polynésienne, culture aborigène d'Australie et découverte du Pacifique."),
            ("Histoire des Caraïbes", "Histoire", "🏝️", "Histoire de l'archipel caribéen, piraterie, plantations sucrières et émancipation."),
            ("Histoire de la Révolution haïtienne", "Histoire", "⚔️", "Le soulèvement de 1791, le génie de Toussaint Louverture et la bataille de Vertières."),
            ("Histoire de l’indépendance d’Haïti", "Histoire", "📜", "L'acte d'indépendance du 1er janvier 1804 aux Gonaïves et Jean-Jacques Dessalines."),
            ("Histoire des grandes civilisations", "Histoire", "🏺", "Mésopotamie, Indus, Chine antique, Perse, Phénicie et leurs legs universels."),
            ("Histoire de l’Égypte ancienne", "Histoire", "📐", "Pharaons, hiéroglyphes, pyramides de Gizeh, Nil et culte de l'au-delà."),
            ("Histoire de la Grèce antique", "Histoire", "🏛️", "Démocratie athénienne, philosophie socratique, cités-États et jeux olympiques."),
            ("Histoire de Rome", "Histoire", "🛡️", "De la République des consuls à l'Empire de César et Auguste, et droit romain."),
            ("Histoire du Moyen Âge", "Histoire", "👑", "Châteaux forts, croisades, universités naissantes, cathédrales et commerce médiéval."),
            ("Histoire de la Renaissance", "Histoire", "🎨", "Humanisme, Léonard de Vinci, imprimerie de Gutenberg et renouveau artistique."),
            ("Histoire des grandes découvertes", "Histoire", "🧭", "Caravelles, Magellan, Vasco de Gama et la première mondialisation maritime."),
            ("Histoire des révolutions", "Histoire", "⚡", "Révolution américaine (1776), Révolution française (1789) et révolutions industrielles."),
            ("Histoire des grandes guerres", "Histoire", "🕊️", "Première et Seconde Guerre mondiale, Société des Nations, ONU et quête de paix."),
            ("Histoire des inventions", "Histoire", "💡", "De la roue et la boussole à la machine à vapeur, la pénicilline et Internet."),
            ("Histoire des religions", "Histoire", "📖", "Étude historique et objective des grands courants spirituels de l'humanité."),
            ("Histoire des grandes personnalités", "Histoire", "⭐", "Leaders, penseurs, scientifiques et résistants ayant marqué la marche du monde."),
            ("Histoire des droits humains", "Histoire", "🤝", "Cylindre de Cyrus, Habeas Corpus, Déclaration universelle de 1948 et luttes civiques."),
            ("Histoire des sciences", "Histoire", "🔭", "D'Archimède et Ibn al-Haytham à Galilée, Newton, Marie Curie et Einstein."),
            ("Histoire de l’esclavage et de son abolition", "Histoire", "⛓️", "Traites négrières, marronnage, luttes abolitionnistes et mémoire historique."),
            ("Histoire des peuples autochtones", "Histoire", "🏹", "Traditions, savoirs ancestraux et droits des premiers habitants des continents."),
            ("Histoire de la colonisation et de la décolonisation", "Histoire", "🗺️", "Impérialismes du XIXe siècle, conférences de Berlin et indépendances du XXe siècle."),
            ("Histoire contemporaine", "Histoire", "🌐", "Guerre froide, chute du mur de Berlin, mondialisation et défis du XXIe siècle."),
            ("Histoire des technologies", "Histoire", "💾", "Évolution des télécommunications, du télégraphe à l'intelligence artificielle."),
            ("Chronologie des événements historiques", "Histoire", "⏳", "Repères temporels essentiels pour structurer sa vision du temps historique."),
            # Géographie (31-60)
            ("Géographie d’Haïti", "Géographie", "🇭🇹", "Reliefs (Massif du Nord, Selle), plaines, départements, golfes et climat d'Haïti."),
            ("Géographie mondiale", "Géographie", "🗺️", "Panorama global des continents, océans, grandes zones climatiques et fuseaux."),
            ("Géographie de l’Afrique", "Géographie", "🌍", "Sahara, Sahel, bassin du Congo, grands lacs africains et richesse géographique."),
            ("Géographie de l’Europe", "Géographie", "🏰", "Alpes, plaine européenne, façades maritimes et organisation de l'espace européen."),
            ("Géographie des Amériques", "Géographie", "🌎", "Cordillère des Andes, Rocheuses, bassin amazonien, Grands Lacs et Caraïbes."),
            ("Géographie de l’Asie", "Géographie", "🏔️", "Himalaya, plateaux d'Asie centrale, moussons et diversité des écosystèmes asiatiques."),
            ("Géographie de l’Océanie", "Géographie", "🏝️", "Australie, Nouvelle-Zélande, Mélanésie, Micronésie et Polynésie."),
            ("Géographie des Caraïbes", "Géographie", "🌴", "Grandes et Petites Antilles, récifs coralliens et dynamiques insulaires."),
            ("Pays, capitales et drapeaux", "Géographie", "🚩", "Maîtrise des 197 États souverains, de leurs capitales et de leur vexillologie."),
            ("Continents et océans", "Géographie", "🌊", "Pacifique, Atlantique, Indien, Arctique, Antarctique et tectonique des plaques."),
            ("Cartographie interactive", "Géographie", "📍", "Projections cartographiques (Mercator, Peters), échelles, légendes et SIG."),
            ("Reliefs et montagnes", "Géographie", "⛰️", "Orogénèse, pics les plus hauts, plateaux, vallées et érosion naturelle."),
            ("Fleuves et rivières", "Géographie", "💧", "Amazone, Nil, Yangtsé, Mississippi, Artibonite et régimes hydrologiques."),
            ("Lacs et mers", "Géographie", "⛵", "Méditerranée, mer Noire, mer des Caraïbes, lac Supérieur, lac Baïkal."),
            ("Climats du monde", "Géographie", "☀️", "Climats équatoriaux, tropicaux, tempérés, désertiques, polaires et Köppen."),
            ("Déserts et forêts", "Géographie", "🌲", "Forêt amazonienne, taïga sibérienne, Sahara, désert d'Atacama et écologie."),
            ("Population mondiale", "Géographie", "👥", "Répartition spatiale des 8 milliards d'humains et foyers de peuplement."),
            ("Villes et mégapoles", "Géographie", "🏙️", "Tokyo, New York, Kinshasa, Port-au-Prince, périurbanisation et villes durables."),
            ("Ressources naturelles", "Géographie", "💎", "Minerais, hydrocarbures, terres rares, forêts et gestion durable des ressources."),
            ("Frontières et territoires", "Géographie", "🚧", "Types de frontières (naturelles, artificielles), enclaves et géopolitique spatiale."),
            ("Géographie économique", "Géographie", "📈", "Flux de marchandises, hubs maritimes, routes commerciales et division du travail."),
            ("Géographie humaine", "Géographie", "🧑‍🤝‍🧑", "Migrations, cultures matérielles, habitats ruraux et urbains et langues."),
            ("Géographie politique", "Géographie", "🌐", "États-nations, organisations régionales (UA, UE, CARICOM), souveraineté."),
            ("Géographie agricole", "Géographie", "🌾", "Cultures vivrières, agriculture intensive, agrosystèmes et sécurité alimentaire."),
            ("Géographie des risques naturels", "Géographie", "⚠️", "Séismes, cyclones, tsunamis, inondations, aléa, vulnérabilité et prévention."),
            ("Orientation et points cardinaux", "Géographie", "🧭", "Rose des vents, boussole, repérage solaire et azimuts."),
            ("Lecture des cartes", "Géographie", "📑", "Lignes de niveau, courbes altimétriques, topographie et calcul de distances."),
            ("Démographie", "Géographie", "📊", "Pyramides des âges, taux de natalité, mortalité, transition démographique."),
            ("Urbanisme", "Géographie", "🏘️", "Planification urbaine, transports en commun, éco-quartiers et aménagement du territoire."),
            ("Systèmes de coordonnées géographiques", "Géographie", "🌐", "Latitude, longitude, équateur, méridien de Greenwich, GPS et géolocalisation.")
        ]
    },
    {
        "id": "sciences_nature",
        "title": "Sciences et Nature",
        "icon": "🔬",
        "description": "Compréhension des lois physiques, chimiques, biologiques et de l'univers.",
        "subjects": [
            ("Biologie", "Biologie", "🧬", "Étude de la vie, de l'ADN, de la reproduction et des fonctions vitales des organismes."),
            ("Chimie", "Chimie", "🧪", "Atomes, molécules, réactions chimiques, liaisons et tableau périodique des éléments."),
            ("Physique", "Physique", "⚛️", "Lois du mouvement, forces, gravitation, énergie et structure de la matière."),
            ("Sciences naturelles", "Sciences", "🍃", "Observation méthodique des phénomènes du vivant et de la nature terrestre."),
            ("Sciences de la Terre", "Géologie", "🌍", "Structure interne du globe, croûte, manteau, noyau et dynamique terrestre."),
            ("Astronomie", "Espace", "🔭", "Observation des astres, télescopes, constellations et mécanique céleste."),
            ("Univers et galaxies", "Espace", "🌌", "Le Big Bang, la Voie lactée, la matière noire et l'expansion cosmique."),
            ("Système solaire", "Espace", "☀️", "Le Soleil et les huit planètes, ceintures d'astéroïdes et comètes."),
            ("Planètes et étoiles", "Espace", "⭐", "Cycle de vie des étoiles (naines, géantes, supernovae) et exoplanètes."),
            ("Corps humain", "Santé", "🫀", "Organisation générale, homéostasie et interaction des grands appareils du corps."),
            ("Anatomie humaine", "Santé", "🦴", "Squelette, muscles, organes vitaux, système circulatoire et respiratoire."),
            ("Physiologie", "Santé", "🧠", "Fonctionnement cellulaire, influx nerveux, digestion et métabolisme."),
            ("Génétique", "Biologie", "🧬", "Lois de Mendel, structure de l'ADN, ARN, mutations et code génétique."),
            ("Microbiologie", "Biologie", "🦠", "Bactéries, virus, levures, champignons microscopiques et immunologie."),
            ("Botanique", "Végétaux", "🌿", "Photosynthèse, anatomie des plantes à fleurs, sève et classification végétale."),
            ("Zoologie", "Animaux", "🦁", "Invertébrés, vertébrés, mammifères, oiseaux, reptiles et comportement animal."),
            ("Écologie", "Environnement", "♻️", "Écosystèmes, chaînes trophiques, cycles biogéochimiques et équilibres naturels."),
            ("Environnement", "Environnement", "🌱", "Protection des milieux naturels, pollution, transition écologique et climat."),
            ("Biodiversité", "Environnement", "🦋", "Diversité des espèces, hotspots mondiaux et préservation des habitats."),
            ("Électricité", "Physique", "⚡", "Courant électrique, tension, loi d'Ohm, circuits en série et parallèle."),
            ("Magnétisme", "Physique", "🧲", "Champs magnétiques, boussoles, électro-aimants et induction."),
            ("Énergie", "Physique", "🔋", "Énergie cinétique, potentielle, thermique, renouvelable et conservation."),
            ("Mécanique", "Physique", "⚙️", "Forces, leviers, poulies, travail mécanique et lois de Newton."),
            ("Optique", "Physique", "🌈", "Lumière, réflexion, réfraction, lentilles, miroirs et spectre visible."),
            ("Acoustique", "Physique", "🔊", "Ondes sonores, fréquence, décibels, écho et résonance."),
            ("Thermodynamique", "Physique", "🔥", "Température, chaleur, lois de la thermodynamique et changements d'état."),
            ("Météorologie", "Terre", "⛅", "Pression atmosphérique, fronts froids/chauds, vents et prévisions du temps."),
            ("Climatologie", "Terre", "🌡️", "Effet de serre, cycles climatiques millénaires et réchauffement global."),
            ("Océanographie", "Terre", "🌊", "Courants marins (Gulf Stream), abysses, marées et acidification des mers."),
            ("Géologie", "Terre", "🪨", "Roches magmatiques, sédimentaires, métamorphiques et stratigraphie."),
            ("Volcans et séismes", "Terre", "🌋", "Tectonique des plaques, magma, failles sismiques, échelle de Richter."),
            ("Fossiles et paléontologie", "Biologie", "🦴", "Fossilisation, dinosaures, ères géologiques et mémoire de la Terre."),
            ("Évolution des espèces", "Biologie", "🦎", "Sélection naturelle de Darwin, adaptations et arbre du vivant."),
            ("Cellules et tissus", "Biologie", "🔬", "Membrane, cytoplasme, noyau, mitochondries et tissus conjonctif/épithélial."),
            ("Nutrition humaine", "Santé", "🥗", "Glucides, lipides, protéines, vitamines, sels minéraux et équilibre nutritif."),
            ("Hygiène et prévention", "Santé", "🧼", "Lavage des mains, asepsie, vaccins et prophylaxie des maladies infectieuses."),
            ("Agriculture scientifique", "Agronomie", "🌾", "Agronomie moderne, rotation des cultures, compostage et irrigation durable."),
            ("Sciences des matériaux", "Physique", "🧱", "Polymères, céramiques, métaux, composites et résistance des matériaux."),
            ("Nanotechnologies", "Technologie", "🧪", "Manipulation de la matière à l'échelle nanométrique et applications futures."),
            ("Recherche scientifique", "Méthode", "🔎", "Méthode hypothético-déductive, protocoles d'expérimentation et rigueur.")
        ]
    },
    {
        "id": "mathematiques",
        "title": "Mathématiques",
        "icon": "📐",
        "description": "Logique, calcul, algèbre, géométrie et raisonnement formel rigoureux.",
        "subjects": [
            ("Arithmétique", "Bases", "🔢", "Nombres entiers, divisibilité, PGCD, PPCM et décomposition en facteurs premiers."),
            ("Calcul mental", "Bases", "⚡", "Techniques d'estimation rapide, astuces de multiplication et vivacité mentale."),
            ("Addition, soustraction, multiplication et division", "Bases", "➕", "Les quatre opérations fondamentales, priorités opératoires et parenthèses."),
            ("Fractions", "Bases", "½", "Fractions équivalentes, simplification, addition, multiplication et division de fractions."),
            ("Nombres décimaux", "Bases", "🎯", "Écriture décimale, troncature, arrondi et conversion fraction-décimale."),
            ("Pourcentages", "Calcul", "％", "Calcul de proportion, augmentations, réductions et pourcentages cumulés."),
            ("Proportionnalité", "Calcul", "⚖️", "Règle de trois, produit en croix, échelles et vitesse moyenne."),
            ("Puissances et racines", "Calcul", "²√", "Règles des exposants, puissances de 10, notation scientifique et racines carrées."),
            ("Algèbre", "Algèbre", "𝑥", "Calcul littéral, factorisation, développement et identités remarquables."),
            ("Équations", "Algèbre", "⚖️", "Résolution d'équations du premier degré à une inconnue et applications concrètes."),
            ("Inéquations", "Algèbre", "≤", "Inégalités, tableaux de signes et intervalles de solutions."),
            ("Systèmes d’équations", "Algèbre", "🧩", "Méthodes de substitution et de combinaison linéaire à 2 ou 3 inconnues."),
            ("Géométrie plane", "Géométrie", "📐", "Triangles, théorème de Pythagore, Thalès, cercles, polygones et aires."),
            ("Géométrie dans l’espace", "Géométrie", "📦", "Cubes, prismes, cylindres, cônes, sphères et calcul de volumes."),
            ("Trigonométrie", "Géométrie", "🔺", "Sinus, cosinus, tangente, cercle trigonométrique et relations d'Euler."),
            ("Fonctions mathématiques", "Analyse", "📈", "Notion de fonction, image, antécédent, ensemble de définition et représentations."),
            ("Suites numériques", "Analyse", "🔢", "Suites arithmétiques, géométriques, récurrence et limites de suites."),
            ("Logarithmes", "Analyse", "🪵", "Logarithme népérien (ln), logarithme décimal et propriétés exponentielles."),
            ("Statistiques", "Statistiques", "📊", "Moyenne, médiane, mode, étendue, variance, écart-type et diagrammes."),
            ("Probabilités", "Statistiques", "🎲", "Événements, arbres de probabilité, probabilités conditionnelles et combinatoire."),
            ("Analyse mathématique", "Analyse", "∫", "Continuité des fonctions, théorèmes fondamentaux et comportement aux bornes."),
            ("Calcul différentiel", "Analyse", "𝑑𝑥", "Dérivées, taux de variation, tangentes, extremums et optimisation."),
            ("Calcul intégral", "Analyse", "∫𝑑𝑡", "Primitives, intégrales définies, calcul d'aires sous les courbes."),
            ("Matrices", "Algèbre", "▦", "Opérations matricielles, déterminant, inverse et résolution de systèmes."),
            ("Vecteurs", "Géométrie", "➡️", "Coordonnées vectorielles, norme, produit scalaire, colinéarité et repères."),
            ("Nombres complexes", "Algèbre", "𝑖", "Partie réelle, imaginaire (i² = -1), forme trigonométrique et exponentielle."),
            ("Théorie des nombres", "Arithmétique", "🔑", "Congruences, petit théorème de Fermat, cryptographie RSA et nombres premiers."),
            ("Logique mathématique", "Logique", "🧠", "Implication, contraposée, réciproque, quantificateurs et raisonnement par l'absurde."),
            ("Combinatoire", "Dénombrement", "🔀", "Permutations, arrangements, combinaisons, factorielles et triangle de Pascal."),
            ("Mathématiques financières", "Finance", "💳", "Intérêts simples, intérêts composés, amortissements et valeur actuelle nette."),
            ("Résolution de problèmes", "Méthodologie", "💡", "Décomposition de problème, modélisation et vérification des résultats."),
            ("Olympiades mathématiques", "Excellence", "🏆", "Énigmes avancées, défis géométriques et stratégies d'ingéniosité.")
        ]
    },
    {
        "id": "langues",
        "title": "Langues du Monde",
        "icon": "🌍",
        "description": "20 langues majeures avec modules de grammaire, vocabulaire, prononciation et conversation.",
        "subjects": [
            ("Français", "Langue", "🇫🇷", "Langue de Molière : grammaire rigoureuse, conjugaisons, accords et richesse lexicale."),
            ("Anglais", "Langue", "🇺🇸", "Langue internationale des échanges, grammaire pratique, phrasal verbs et fluidité."),
            ("Créole haïtien", "Langue", "🇭🇹", "Langue officielle d'Haïti : phonétique claire, syntaxe créole, proverbes et culture."),
            ("Espagnol", "Langue", "🇪🇸", "Langue hispanique : concordance des temps, subjonctif, vocabulaire quotidien et accents."),
            ("Chinois mandarin", "Langue", "🇨🇳", "Pinyin, 4 tons phonétiques, sinogrammes de base et structures de phrases chinoises."),
            ("Portugais", "Langue", "🇵🇹", "Prononciation nasale, verbes réguliers/irréguliers et expressions lusophones."),
            ("Allemand", "Langue", "🇩🇪", "Déclinaisons (nominatif, accusatif, datif, génitif), ordre des mots et mots composés."),
            ("Italien", "Langue", "🇮🇹", "Musicalité de la langue, phonétique claire, conjugaisons et dialogues de la vie courante."),
            ("Arabe", "Langue", "🇸🇦", "Alphabet arabe, racines triconsonantiques, calligraphie et arabe standard moderne."),
            ("Japonais", "Langue", "🇯🇵", "Hiragana, Katakana, kanjis essentiels, particules grammaticales et politesse."),
            ("Coréen", "Langue", "🇰🇷", "Alphabet Hangul logique et intuitif, particules et expressions courantes en Corée."),
            ("Russe", "Langue", "🇷🇺", "Alphabet cyrillique, cas grammaticaux et vocabulaire essentiel en langue slave."),
            ("Néerlandais", "Langue", "🇳🇱", "Phonétique néerlandaise, syntaxe germanique et conversation courante aux Pays-Bas."),
            ("Turc", "Langue", "🇹🇷", "Langue agglutinante, harmonie vocalique rigoureuse et logique grammaticale directe."),
            ("Hindi", "Langue", "🇮🇳", "Écriture devanagari, structure Sujet-Objet-Verbe et formules de politesse indiennes."),
            ("Swahili", "Langue", "🇰🇪", "Grande langue véhiculaire d'Afrique de l'Est, classes nominales et salutations."),
            ("Latin", "Langue ancienne", "🏛️", "Langue mère des langues romanes, déclinaisons, racines étymologiques et textes classiques."),
            ("Grec ancien", "Langue ancienne", "📜", "Alphabet grec, étymologie des sciences et philosophie socratique originelle."),
            ("Hébreu", "Langue", "🇮🇱", "Alphabet hébraïque sans voyelles obligatoires, racines hébraïques et structures."),
            ("Langue des signes", "Langue visuelle", "🤟", "Alphabet dactylologique, expressions faciales, espace de signation et syntaxe gestuelle.")
        ]
    },
    {
        "id": "informatique_technologie",
        "title": "Informatique et Technologie",
        "icon": "💻",
        "description": "Programmation, développement web/mobile, cybersécurité, réseaux et intelligence artificielle.",
        "subjects": [
            ("Initiation à l’informatique", "Bases", "🖥️", "Architecture d'un ordinateur (CPU, RAM, stockage), système d'exploitation et fichiers."),
            ("HTML", "Web", "🌐", "Structure sémantique des pages web, balises, formulaires, liens et accessibilité."),
            ("CSS", "Web", "🎨", "Mise en page moderne, Flexbox, CSS Grid, animations, responsive design et variables."),
            ("JavaScript", "Programmation", "⚡", "Variables, boucles, fonctions fléchées, DOM, asynchronisme (Promises, async/await)."),
            ("Python", "Programmation", "🐍", "Syntaxe claire, types de données, scripts, modules, listes en compréhension et POO."),
            ("Java", "Programmation", "☕", "Typage statique, classes, interfaces, héritage, polymorphisme et machine virtuelle JVM."),
            ("C", "Système", "⚙️", "Gestion directe de la mémoire, pointeurs, structures de données et compilation native."),
            ("C++", "Programmation", "🚀", "Programmation orientée objet avancée, templates, STL et haute performance."),
            ("C#", "Programmation", "🎮", "Écosystème .NET, syntaxe élégante, LINQ, applications d'entreprise et moteur Unity."),
            ("PHP", "Web", "🐘", "Génération de pages dynamiques côté serveur, sessions, formulaires et interactions BD."),
            ("SQL", "Données", "🗄️", "Requêtes relationnelles (SELECT, INSERT, UPDATE, DELETE), jointures, index et agrégats."),
            ("Développement web", "Web", "🌐", "Architecture client-serveur, API REST, communication HTTP et frameworks modernes."),
            ("Développement Android", "Mobile", "📱", "Kotlin, Jetpack Compose, activités, ViewModel, cycles de vie et Material 3."),
            ("Développement de jeux vidéo", "Jeux", "👾", "Boucle de jeu (game loop), physique 2D/3D, détection de collisions et gameplay."),
            ("Intelligence artificielle", "IA", "🤖", "Apprentissage automatique, réseaux de neurones, traitement du langage naturel (NLP)."),
            ("Robotique", "Ingénierie", "🦾", "Capteurs, actionneurs, microcontrôleurs (Arduino/ESP32) et asservissement."),
            ("Cybersécurité défensive", "Sécurité", "🛡️", "Chiffrement, pare-feu, authentification forte, gestion des vulnérabilités et hygiène cyber."),
            ("Réseaux informatiques", "Réseau", "🔌", "Modèle OSI, TCP/IP, routage, adresses IP, masques de sous-réseau, DNS et DHCP."),
            ("Bases de données", "Données", "💾", "Modélisation entité-association, normalisation, bases SQL vs NoSQL."),
            ("Cloud computing", "Système", "☁️", "Serveurs distants, virtualisation, conteneurs Docker, microservices et scalabilité."),
            ("Linux", "Système", "🐧", "Ligne de commande Bash, gestion des permissions, processus, cron et administration."),
            ("Git et GitHub", "Outils", "🐙", "Contrôle de version, commits, branches, pull requests, merges et collaboration."),
            ("Algorithmique", "Bases", "🧠", "Complexité temporelle (Grand O), tris (rapide, fusion), récursivité et optimisation."),
            ("Structures de données", "Bases", "📚", "Tableaux, listes chaînées, piles, files, arbres binaires et tables de hachage."),
            ("Développement d’applications", "Ingénierie", "📱", "Conception logicielle propre (Clean Architecture, MVC), tests unitaires et déploiement."),
            ("Conception UI/UX", "Design", "✨", "Ergonomie, parcours utilisateur, wireframes, prototypes interactifs et accessibilité."),
            ("Animation numérique", "Graphisme", "🎬", "Images clés (keyframes), courbes de Bézier, interpolation et animation vectorielle."),
            ("Modélisation 3D", "Graphisme", "🧊", "Maillages polygonaux, extrusions, textures, éclairage et rendu tridimensionnel."),
            ("Création de sites web", "Web", "🚀", "De la conception du nom de domaine au design responsive et à la mise en ligne."),
            ("Électronique numérique", "Matériel", "⚡", "Portes logiques (AND, OR, NOT), bascules, circuits combinatoires et signaux binaires.")
        ]
    },
    {
        "id": "autres_matieres",
        "title": "Autres Matières",
        "icon": "📚",
        "description": "Économie, sciences sociales, arts, santé, agriculture et développement personnel.",
        "subjects": [
            # Économie et gestion (1-10)
            ("Économie générale", "Économie", "📊", "Offre et demande, inflation, PIB, monnaie et grands courants de pensée économique."),
            ("Comptabilité", "Gestion", "📑", "Actif, passif, bilan comptable, compte de résultat et principes de partie double."),
            ("Gestion d’entreprise", "Gestion", "🏢", "Stratégie, management des équipes, logistique et chaîne de valeur organisationnelle."),
            ("Entrepreneuriat", "Gestion", "🚀", "Idéation, business plan, modèle économique (Lean Canvas), pitch et financement."),
            ("Marketing", "Gestion", "🎯", "Les 4P (Produit, Prix, Place, Promotion), étude de marché et fidélisation client."),
            ("Commerce international", "Économie", "🚢", "Exportation, importation, douanes, incoterms et accords commerciaux mondiaux."),
            ("Finance personnelle", "Finance", "💰", "Budget mensuel, épargne de précaution, gestion de dettes et investissement réfléchi."),
            ("Épargne et budget", "Finance", "🐷", "Règle des 50/30/20, constitution d'un fonds d'urgence et objectifs financiers."),
            ("Banque", "Finance", "🏦", "Rôle des banques centrales, taux directeurs, crédits bancaires et sécurité financière."),
            ("Gestion de projet", "Gestion", "📋", "Méthodes Agile/Scrum, diagramme de Gantt, jalons, gestion des risques et livrables."),
            # Société et culture (11-20)
            ("Philosophie", "Société", "🏛️", "Éthique, métaphysique, théorie de la connaissance, Socrate, Descartes, Kant."),
            ("Psychologie", "Société", "🧠", "Comportement humain, cognition, émotions, mémoire et perception psychologique."),
            ("Sociologie", "Société", "👥", "Faits sociaux, stratification, socialisation, normes sociales et dynamique collective."),
            ("Droit", "Société", "⚖️", "Sources du droit, constitution, droit civil, droit pénal et hiérarchie des normes."),
            ("Éducation civique", "Société", "🗳️", "Devoirs et droits du citoyen, démocratie, vote, laïcité et engagement public."),
            ("Relations internationales", "Société", "🌐", "Diplomatie, géopolitique, traités internationaux et maintien de la paix par l'ONU."),
            ("Politique comparée", "Société", "🏛️", "Régimes politiques mondiaux : parlementaire, présidentiel, fédéralisme et transitions."),
            ("Anthropologie", "Société", "🏺", "Étude des cultures humaines, rituels, parenté et diversité des civilisations."),
            ("Journalisme", "Communication", "📰", "Éthique de l'information, vérification des sources (fact-checking) et reportage."),
            ("Communication", "Communication", "🗣️", "Communication interpersonnelle, écoute active, communication non verbale et rhétorique."),
            # Arts et création (21-30)
            ("Musique", "Arts", "🎵", "Notes, portées, rythmes, gammes, harmonies et histoire des grands genres musicaux."),
            ("Dessin", "Arts", "✏️", "Perspectives à 1 et 2 points de fuite, proportions du corps et ombrages au crayon."),
            ("Peinture", "Arts", "🎨", "Théorie des couleurs primaires/secondaires, aquarelle, acrylique, huile et composition."),
            ("Photographie", "Arts", "📷", "Triangle d'exposition (ouverture, vitesse, ISO), règle des tiers et cadrage."),
            ("Cinéma", "Arts", "🎥", "Plans (gros plan, plan large), montage, narration visuelle et histoire du 7e art."),
            ("Théâtre", "Arts", "🎭", "Jeu scénique, projection vocale, tragédie, comédie et écriture dramatique."),
            ("Littérature", "Arts", "📚", "Grands courants littéraires (classicisme, romantisme, réalisme) et figures de style."),
            ("Poésie", "Arts", "🪶", "Métriques, rimes, strophes, images poétiques et musicalité des vers."),
            ("Écriture créative", "Arts", "✍️", "Développement de personnages, arc narratif, création d'univers et dialogues captivants."),
            ("Design graphique", "Arts", "📐", "Typographie, hiérarchie visuelle, grille de mise en page et identité de marque."),
            # Autres domaines (31-50)
            ("Santé et hygiène", "Vie pratique", "🩺", "Sommeil réparateur, hydratation, activité physique régulière et prévention santé."),
            ("Premiers secours", "Vie pratique", "🚑", "Position latérale de sécurité (PLS), réanimation cardio-pulmonaire (RCP) et étouffement."),
            ("Nutrition", "Vie pratique", "🍎", "Besoins caloriques quotidiens, macronutriments, micronutriments et alimentation saine."),
            ("Agriculture", "Agronomie", "🌱", "Préparation des sols, semences, fertilisation organique et calendrier de plantation."),
            ("Élevage", "Agronomie", "🐄", "Bien-être animal, alimentation des cheptels, santé vétérinaire et productions durables."),
            ("Cuisine", "Vie pratique", "🍲", "Techniques de cuisson (sauter, mijoter, braiser), découpes et assaisonnements équilibrés."),
            ("Sport", "Santé", "⚽", "Règles des sports collectifs et individuels, esprit sportif et fair-play."),
            ("Éducation physique", "Santé", "🏃", "Renforcement musculaire, souplesse, endurance aérobie et échauffement indispensable."),
            ("Architecture", "Ingénierie", "🏛️", "Stabilité des structures, arc en plein cintre, voûtes, béton armé et architecture bioclimatique."),
            ("Ingénierie", "Ingénierie", "⚙️", "Conception technique, résolution méthodique de contraintes physiques et innovation."),
            ("Énergies renouvelables", "Environnement", "☀️", "Solaire photovoltaïque, éolien, hydroélectricité, géothermie et stockage d'énergie."),
            ("Développement durable", "Environnement", "🌍", "Les 17 Objectifs de Développement Durable (ODD) de l'ONU pour 2030."),
            ("Astronautique", "Espace", "🚀", "Fusées, vitesse de libération, orbites terrestres, missions lunaires et martiennes."),
            ("Aviation", "Ingénierie", "✈️", "Portance des ailes, traînée, poussée du réacteur et sécurité aérienne rigoureuse."),
            ("Tourisme", "Économie", "🏖️", "Écotourisme respectueux, patrimoine mondial de l'UNESCO et hospitalité."),
            ("Gestion de l’eau", "Environnement", "🚰", "Traitement de l'eau potable, assainissement, bassins versants et conservation de l'or bleu."),
            ("Protection de l’environnement", "Environnement", "🛡️", "Recyclage, réduction des plastiques, parcs nationaux et protection de la faune."),
            ("Orientation professionnelle", "Développement", "🧭", "Bilan de compétences, découverte des métiers d'avenir, CV et entretiens."),
            ("Méthodes de recherche", "Méthodologie", "🔍", "Recherche documentaire critique, citations de sources fiables et esprit d'analyse."),
            ("Méthodes d’apprentissage", "Développement", "🧠", "Répétition espacée, cartes mentales, technique Feynman et mémorisation active.")
        ]
    }
]

# Total count verification
total_subjects = sum(len(d["subjects"]) for d in DOMAINS_CONFIG)
print(f"Total defined subjects across all domains: {total_subjects}")
assert total_subjects == 232, f"Expected 232 subjects, got {total_subjects}"

# ==========================================
# 2. GENERATE FULL STRUCTURE FOR EACH SUBJECT
# ==========================================

def slugify(title):
    import re
    cleaned = title.lower().replace("’", "_").replace("'", "_").replace("-", "_").replace(" ", "_")
    cleaned = re.sub(r'[éèêë]', 'e', cleaned)
    cleaned = re.sub(r'[àâä]', 'a', cleaned)
    cleaned = re.sub(r'[îï]', 'i', cleaned)
    cleaned = re.sub(r'[ôö]', 'o', cleaned)
    cleaned = re.sub(r'[ùûü]', 'u', cleaned)
    cleaned = re.sub(r'[ç]', 'c', cleaned)
    cleaned = re.sub(r'[^a-z0-9_]', '', cleaned)
    return cleaned

def generate_curriculum():
    all_subjects = []

    for domain in DOMAINS_CONFIG:
        for title, category, icon, desc in domain["subjects"]:
            sub_id = slugify(title)

            # High-quality structured chapters with concrete pedagogical explanations
            ch1_title = f"1. Les Fondations et Principes de : {title}"
            ch1_summary = f"Introduction approfondie aux notions capitales, repères clés et définitions indispensables."
            ch1_content = (
                f"Pour appréhender {title}, il est primordial de comprendre ses bases méthodiques et ses racines historiques. "
                f"Ce domaine nous enseigne comment observer les faits avec rigueur et esprit critique. "
                f"Exemple concret : Dans la vie courante ou l'étude académique, l'application de ces principes fondamentaux "
                f"permet de structurer sa pensée, d'éviter les erreurs courantes et de résoudre des problèmes complexes pas à pas."
            )
            ch1_key = [
                "Comprendre le vocabulaire fondamental et les définitions exactes.",
                "Identifier les acteurs, les lois ou les processus majeurs qui régissent cette matière.",
                "Savoir faire le lien entre la théorie et ses manifestations observables dans le réel."
            ]

            ch2_title = f"2. Mécanismes Clés et Développements Majeurs"
            ch2_summary = f"Analyse des dynamiques internes, évolutions historiques ou théoriques et études de cas."
            ch2_content = (
                f"L'étude de {title} met en lumière des mécanismes précis et des relations de cause à effet. "
                f"Rien n'arrive par hasard : chaque événement ou découverte découle d'un ensemble de facteurs mesurables. "
                f"Exemple concret : Quand on analyse les grandes étapes d'évolution, on constate une régularité impressionnante "
                f"dans la façon dont les innovations et les mutations transforment les sociétés et la compréhension humaine."
            )
            ch2_key = [
                "Maîtriser les étapes chronologiques ou méthodologiques successives.",
                "Analyser les interactions entre les différentes composantes du sujet.",
                "Développer une argumentation logique appuyée sur des faits vérifiables."
            ]

            ch3_title = f"3. Applications Pratiques et Enjeux Contemporains"
            ch3_summary = f"Comment ces savoirs s'incarnent aujourd'hui et ouvrent des perspectives pour l'avenir."
            ch3_content = (
                f"La maîtrise de {title} constitue un levier d'action déterminant dans le monde d'aujourd'hui. "
                f"Qu'il s'agisse de recherche, d'innovation technique, de citoyenneté ou de culture générale, "
                f"ces connaissances apportent une grille de lecture irremplaçable face aux défis actuels. "
                f"Exemple concret : De nombreux professionnels, chercheurs et créateurs mobilisent quotidiennement ces notions "
                f"pour concevoir des solutions durables et pertinentes à l'échelle locale et globale."
            )
            ch3_key = [
                "Mettre en pratique les connaissances acquises dans des situations réelles.",
                "Évaluer avec discernement les défis et opportunités liés à cette matière.",
                "Adopter une démarche d'apprentissage continu et de curiosité intellectuelle."
            ]

            # Interactive exercise
            exercise = {
                "title": f"Exercice pratique d'application : {title}",
                "instruction": f"Analysez la situation suivante et déterminez l'approche méthodologique adéquate pour {title}.",
                "scenario": f"Vous êtes confronté(e) à une étude de cas portant sur {title}. Quelles étapes ordonnées devez-vous suivre pour formuler une analyse rigoureuse ?",
                "hint": "Commencez par définir les termes clés, rassemblez les faits vérifiables, puis confrontez vos hypothèses aux résultats mesurables.",
                "solution": "1. Définition claire du champ d'étude. 2. Collecte des données probantes. 3. Analyse critique comparative. 4. Synthèse argumentée."
            }

            # Rich Quiz with 3 MCQs
            quiz = [
                {
                    "question": f"Quel est l'objectif premier lors de l'étude de {title} ?",
                    "options": [
                        "Mémoriser des informations isolées sans comprendre les liens de cause à effet",
                        f"Comprendre les principes fondamentaux, les mécanismes réels et leurs applications pratiques",
                        "Répéter des opinions sans vérifier les sources documentaires ou scientifiques",
                        "Ignorer l'histoire et les évolutions récentes de la discipline"
                    ],
                    "correct": 1,
                    "explanation": f"L'étude de {title} vise avant tout la compréhension en profondeur des mécanismes réels, la rigueur de la méthode et la capacité à appliquer ces savoirs concrètement."
                },
                {
                    "question": f"Dans le cadre de {title}, quelle démarche garantit la meilleure fiabilité des résultats ?",
                    "options": [
                        "L'intuition spontanée sans vérification ultérieure",
                        "La confrontation des hypothèses aux données factuelles et l'analyse critique rigoureuse",
                        "L'acceptation d'idées reçues non démontrées",
                        "L'abandon dès la première difficulté rencontrée"
                    ],
                    "correct": 1,
                    "explanation": "La méthode scientifique et pédagogique exige de vérifier systématiquement les hypothèses par des observations ou des démonstrations rigoureuses."
                },
                {
                    "question": f"Comment peut-on mettre en pratique les acquis de {title} dans la vie courante ou professionnelle ?",
                    "options": [
                        "En gardant les connaissances purement théoriques sans jamais les expérimenter",
                        "En analysant les situations du quotidien avec discernement et en résolvant les problèmes avec méthode",
                        "En évitant toute discussion ou partage avec d'autres apprenants",
                        "En refusant d'adapter ses méthodes face aux nouvelles données"
                    ],
                    "correct": 1,
                    "explanation": "Le véritable savoir s'exprime dans la capacité à éclairer les choix du quotidien, à résoudre des défis concrets et à innover avec confiance."
                }
            ]

            # Final Exam (2 questions pour validation officielle)
            final_exam = [
                {
                    "question": f"Examen de synthèse : Quelle est la caractéristique essentielle d'une analyse réussie en {title} ?",
                    "options": [
                        "La superficialité et l'absence d'exemples",
                        "La cohérence logique, l'exactitude des faits et la clarté pédagogique",
                        "La contradiction permanente avec les lois naturelles ou historiques",
                        "L'utilisation d'affirmations gratuites sans preuve"
                    ],
                    "correct": 1,
                    "explanation": "Une excellente maîtrise allie toujours rigueur factuelle, cohérence intellectuelle et capacité de transmission claire."
                },
                {
                    "question": f"Examen de synthèse : Quel impact durable produit l'apprentissage approfondi de {title} ?",
                    "options": [
                        "Aucun impact sur le développement personnel ou intellectuel",
                        "Un renforcement notable de l'esprit critique, de l'autonomie et de l'ouverture sur le monde",
                        "Une fermeture aux autres champs de connaissances",
                        "Une diminution de la curiosité et de la créativité"
                    ],
                    "correct": 1,
                    "explanation": "L'acquisition de ce savoir forge un esprit autonome, capable de discerner le vrai du faux et de contribuer positivement à la société."
                }
            ]

            all_subjects.append({
                "id": sub_id,
                "domainId": domain["id"],
                "domainTitle": domain["title"],
                "title": title,
                "category": category,
                "icon": icon,
                "description": desc,
                "level": "Tous niveaux (Débutant à Avancé)",
                "chapters": [
                    {
                        "title": ch1_title,
                        "summary": ch1_summary,
                        "content": ch1_content,
                        "keyTakeaways": ch1_key
                    },
                    {
                        "title": ch2_title,
                        "summary": ch2_summary,
                        "content": ch2_content,
                        "keyTakeaways": ch2_key
                    },
                    {
                        "title": ch3_title,
                        "summary": ch3_summary,
                        "content": ch3_content,
                        "keyTakeaways": ch3_key
                    }
                ],
                "interactiveExercise": exercise,
                "quiz": quiz,
                "finalExam": final_exam
            })

    return all_subjects

# ==========================================
# 3. 40 EDUCATIONAL GAMES SPECIFICATION
# ==========================================

GAMES_CONFIG = [
    {
        "id": "quiz_culture_generale",
        "name": "Quiz de culture générale",
        "category": "Culture & Savoir",
        "icon": "🧠",
        "rules": "Répondez correctement au maximum de questions de culture générale mondiale et haïtienne. Choisissez entre 3 niveaux de difficulté.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 100,
        "questions": [
            {
                "question": "En quelle année Haïti a-t-elle proclamé son indépendance ?",
                "options": ["1789", "1804", "1825", "1915"],
                "correct": 1,
                "explanation": "Haïti est devenue la première république noire indépendante au monde le 1er janvier 1804 aux Gonaïves."
            },
            {
                "question": "Quel est le plus grand océan de notre planète ?",
                "options": ["Océan Atlantique", "Océan Pacifique", "Océan Indien", "Océan Arctique"],
                "correct": 1,
                "explanation": "L'océan Pacifique couvre plus de 165 millions de km², soit un tiers de la surface de la Terre."
            },
            {
                "question": "Qui a peint la célèbre Joconde exposée au Louvre ?",
                "options": ["Michel-Ange", "Léonard de Vinci", "Raphaël", "Vincent van Gogh"],
                "correct": 1,
                "explanation": "Léonard de Vinci a peint La Joconde au début du XVIe siècle à Florence."
            },
            {
                "question": "Quel organe du corps humain pompe le sang dans tout l'organisme ?",
                "options": ["Le poumon", "Le cœur", "Le foie", "Le cerveau"],
                "correct": 1,
                "explanation": "Le cœur humain bat environ 100 000 fois par jour pour propulser le sang oxygéné."
            }
        ]
    },
    {
        "id": "jeu_capitales",
        "name": "Jeu des capitales",
        "category": "Géographie",
        "icon": "🏛️",
        "rules": "Trouvez la capitale exacte du pays proposé. Analysez bien les indices géographiques.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Quelle est la capitale officielle du Canada ?",
                "options": ["Montréal", "Ottawa", "Toronto", "Vancouver"],
                "correct": 1,
                "explanation": "Ottawa a été choisie comme capitale du Canada par la reine Victoria en 1857."
            },
            {
                "question": "Quelle est la capitale de l'Australie ?",
                "options": ["Sydney", "Canberra", "Melbourne", "Brisbane"],
                "correct": 1,
                "explanation": "Canberra a été bâtie spécialement pour servir de capitale de compromis entre Sydney et Melbourne."
            },
            {
                "question": "Quelle est la capitale du Japon ?",
                "options": ["Kyoto", "Tokyo", "Osaka", "Nagoya"],
                "correct": 1,
                "explanation": "Tokyo (anciennement Edo) est la capitale du Japon depuis 1868."
            },
            {
                "question": "Quelle est la capitale du Sénégal ?",
                "options": ["Saint-Louis", "Dakar", "Thiès", "Touba"],
                "correct": 1,
                "explanation": "Dakar est située sur la presqu'île du Cap-Vert, le point le plus occidental d'Afrique."
            }
        ]
    },
    {
        "id": "jeu_drapeaux",
        "name": "Jeu des drapeaux",
        "category": "Géographie",
        "icon": "🚩",
        "rules": "Associez les couleurs et les symboles vexillologiques au bon pays.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Quel pays a pour drapeau deux bandes bleue et rouge avec les armes de la République au centre ?",
                "options": ["France", "Haïti", "République dominicaine", "Liechtenstein"],
                "correct": 1,
                "explanation": "Le drapeau haïtien a été créé à l'Arcahaie le 18 mai 1803 par Jean-Jacques Dessalines et Catherine Flon."
            },
            {
                "question": "Quel drapeau arbore une feuille d'érable rouge sur fond blanc et rouge ?",
                "options": ["Canada", "Pérou", "Suisse", "Autriche"],
                "correct": 0,
                "explanation": "L'Unifolié canadien a été adopté officiellement en 1965."
            },
            {
                "question": "Quel pays a un drapeau vert avec un losange jaune et un globe bleu au centre ?",
                "options": ["Argentine", "Brésil", "Colombie", "Mexique"],
                "correct": 1,
                "explanation": "Le drapeau brésilien porte la devise nationale 'Ordem e Progresso'."
            },
            {
                "question": "Quel pays européen possède un drapeau frappé d'une croix blanche sur fond rouge ?",
                "options": ["Suède", "Suisse", "Danemark", "Norvège"],
                "correct": 1,
                "explanation": "Le drapeau suisse est l'un des rares drapeaux nationaux au format carré dans le monde."
            }
        ]
    },
    {
        "id": "jeu_geographie",
        "name": "Jeu de géographie",
        "category": "Géographie",
        "icon": "🗺️",
        "rules": "Localisez les continents, océans, détroits et grands ensembles géographiques mondiaux.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 110,
        "questions": [
            {
                "question": "Sur quel continent se trouve le mont Kilimandjaro (5 895 m) ?",
                "options": ["Asie", "Afrique", "Amérique du Sud", "Europe"],
                "correct": 1,
                "explanation": "Le mont Kilimandjaro est un sommet volcanique situé en Tanzanie, en Afrique de l'Est."
            },
            {
                "question": "Quel est le plus long fleuve d'Amérique du Sud ?",
                "options": ["Le Paraná", "L'Amazone", "L'Orénoque", "Le São Francisco"],
                "correct": 1,
                "explanation": "L'Amazone est le fleuve le plus puissant au monde et possède le plus grand bassin versant."
            },
            {
                "question": "Quelle mer sépare l'Europe du continent africain ?",
                "options": ["La mer Baltique", "La mer Méditerranée", "La mer Noire", "La mer du Nord"],
                "correct": 1,
                "explanation": "La mer Méditerranée est une mer intercontinentale bordée par l'Europe, l'Afrique et l'Asie."
            }
        ]
    },
    {
        "id": "jeu_histoire",
        "name": "Jeu d’histoire",
        "category": "Histoire",
        "icon": "⚔️",
        "rules": "Testez vos connaissances sur les grands événements, traités et batailles historiques.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 110,
        "questions": [
            {
                "question": "Où s'est déroulée la bataille décisive de l'armée indigène haïtienne le 18 novembre 1803 ?",
                "options": ["Crête-à-Pierrot", "Vertières", "Ravine-à-Couleuvres", "Gonaïves"],
                "correct": 1,
                "explanation": "La bataille de Vertières, près du Cap-Haïtien, a consacré la défaite définitive des troupes françaises du général Rochambeau."
            },
            {
                "question": "Quel événement de 1789 marque le début symbolique de la Révolution française ?",
                "options": ["La prise de la Bastille (14 juillet)", "Le couronnement de Napoléon", "La bataille de Waterloo", "Le traité de Versailles"],
                "correct": 0,
                "explanation": "Le 14 juillet 1789, les Parisiens s'emparent de la forteresse royale de la Bastille."
            },
            {
                "question": "Qui fut le premier empereur de la Rome antique ?",
                "options": ["Jules César", "Auguste (Octave)", "Néron", "Marc Aurèle"],
                "correct": 1,
                "explanation": "Octave prend le titre d'Auguste en 27 av. J.-C. et fonde l'Empire romain."
            }
        ]
    },
    {
        "id": "jeu_chronologie_historique",
        "name": "Jeu de chronologie historique",
        "category": "Histoire",
        "icon": "⏳",
        "rules": "Remettez les événements historiques dans le bon ordre chronologique.",
        "hasTimer": True,
        "timeLimit": 25,
        "points": 140,
        "questions": [
            {
                "question": "Lequel de ces événements s'est produit en premier dans l'histoire ?",
                "options": ["La construction des pyramides d'Égypte", "La chute de l'Empire romain d'Occident", "La découverte des Amériques par Christophe Colomb", "La Révolution haïtienne"],
                "correct": 0,
                "explanation": "Les pyramides de Gizeh ont été érigées vers 2500 av. J.-C., bien avant Rome (476 apr. J.-C.) et 1492."
            },
            {
                "question": "Quel événement s'est déroulé avant la Déclaration d'indépendance d'Haïti (1804) ?",
                "options": ["La guerre de Sécession américaine", "La Révolution américaine (1776)", "L'abolition définitive de l'esclavage aux USA (1865)", "La Première Guerre mondiale"],
                "correct": 1,
                "explanation": "La déclaration d'indépendance des 13 colonies américaines date de 1776, 28 ans avant 1804."
            }
        ]
    },
    {
        "id": "jeu_mathematiques",
        "name": "Jeu de mathématiques",
        "category": "Mathématiques",
        "icon": "📐",
        "rules": "Résolvez des énigmes d'arithmétique, de géométrie et d'algèbre dans un temps imparti.",
        "hasTimer": True,
        "timeLimit": 25,
        "points": 130,
        "questions": [
            {
                "question": "Quelle est la valeur de x dans l'équation : 3x + 9 = 24 ?",
                "options": ["3", "5", "7", "8"],
                "correct": 1,
                "explanation": "3x = 24 - 9 = 15, donc x = 15 / 3 = 5."
            },
            {
                "question": "Quelle est la somme des angles intérieurs de n'importe quel triangle ?",
                "options": ["90°", "180°", "270°", "360°"],
                "correct": 1,
                "explanation": "Dans le plan euclidien, la somme des trois angles d'un triangle vaut toujours exactement 180 degrés."
            },
            {
                "question": "Quel est le résultat de : (4 + 6) × 3 - 5 ?",
                "options": ["20", "25", "30", "35"],
                "correct": 1,
                "explanation": "On calcule d'abord entre parenthèses : 4 + 6 = 10. Puis la multiplication : 10 × 3 = 30. Enfin : 30 - 5 = 25."
            }
        ]
    },
    {
        "id": "jeu_calcul_rapide",
        "name": "Jeu de calcul rapide",
        "category": "Mathématiques",
        "icon": "⚡",
        "rules": "Calculez de tête le plus vite possible sans faire d'erreur !",
        "hasTimer": True,
        "timeLimit": 10,
        "points": 150,
        "questions": [
            {
                "question": "Combien font 15 × 12 ?",
                "options": ["150", "165", "180", "195"],
                "correct": 2,
                "explanation": "15 × 12 = 15 × 10 (150) + 15 × 2 (30) = 180."
            },
            {
                "question": "Quel est le quart de 280 ?",
                "options": ["60", "70", "80", "90"],
                "correct": 1,
                "explanation": "280 divisé par 4 = 70."
            },
            {
                "question": "7 × 8 + 14 = ?",
                "options": ["60", "70", "72", "74"],
                "correct": 1,
                "explanation": "7 × 8 = 56, et 56 + 14 = 70."
            }
        ]
    },
    {
        "id": "jeu_sciences",
        "name": "Jeu de sciences",
        "category": "Sciences",
        "icon": "🔬",
        "rules": "Démontrez votre maîtrise des concepts de biologie, chimie et physique.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 110,
        "questions": [
            {
                "question": "Quelle est la formule chimique de l'eau pure ?",
                "options": ["CO2", "H2O", "NaCl", "O2"],
                "correct": 1,
                "explanation": "La molécule d'eau est composée de deux atomes d'hydrogène et d'un atome d'oxygène (H₂O)."
            },
            {
                "question": "Quelle force attire les objets vers le centre de la Terre ?",
                "options": ["La force électrostatique", "La gravitation", "La force centrifuge", "La force nucléaire forte"],
                "correct": 1,
                "explanation": "La gravitation universelle, formalisée par Isaac Newton, régit l'attraction mutuelle des masses."
            },
            {
                "question": "Quel processus permet aux plantes vertes de transformer la lumière en sucres ?",
                "options": ["La respiration cellulaire", "La photosynthèse", "La fermentation", "L'oxydation"],
                "correct": 1,
                "explanation": "La photosynthèse utilise l'eau, le CO₂ et la lumière solaire grâce à la chlorophylle."
            }
        ]
    },
    {
        "id": "jeu_corps_humain",
        "name": "Jeu du corps humain",
        "category": "Sciences",
        "icon": "🫀",
        "rules": "Identifiez les os, organes, muscles et fonctions physiologiques de l'être humain.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 120,
        "questions": [
            {
                "question": "Quel est l'os le plus long du squelette humain ?",
                "options": ["L'humérus", "Le fémur", "Le tibia", "Le radius"],
                "correct": 1,
                "explanation": "Le fémur, situé dans la cuisse, est à la fois le plus long et le plus résistant des os humains."
            },
            {
                "question": "Quel organe est le principal responsable de la détoxification de l'organisme et produit la bile ?",
                "options": ["La rate", "Le foie", "Le pancréas", "Le rein"],
                "correct": 1,
                "explanation": "Le foie accomplit plus de 500 fonctions vitales, notamment le stockage du glycogène et l'épuration sanguine."
            },
            {
                "question": "Combien de chambres (cavités) compte le cœur humain ?",
                "options": ["2", "3", "4", "6"],
                "correct": 2,
                "explanation": "Le cœur possède deux oreillettes (gauche et droite) et deux ventricules (gauche et droit)."
            }
        ]
    },
    {
        "id": "jeu_langues",
        "name": "Jeu des langues",
        "category": "Langues",
        "icon": "🗣️",
        "rules": "Identifiez la langue d'un texte, sa famille linguistique ou son écriture.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 110,
        "questions": [
            {
                "question": "En quelle langue l'expression 'Sak pase ? - N ap boule !' est-elle formulée ?",
                "options": ["Espagnol", "Créole haïtien", "Swahili", "Portugais"],
                "correct": 1,
                "explanation": "C'est la salutation informelle la plus emblématique et chaleureuse de la langue créole haïtienne."
            },
            {
                "question": "À quelle famille linguistique appartiennent le français, l'espagnol et l'italien ?",
                "options": ["Langues germaniques", "Langues romanes", "Langues slaves", "Langues sémitiques"],
                "correct": 1,
                "explanation": "Ces langues dérivent directement de l'évolution du latin populaire parlé dans l'Empire romain."
            }
        ]
    },
    {
        "id": "jeu_vocabulaire",
        "name": "Jeu de vocabulaire",
        "category": "Langues",
        "icon": "📖",
        "rules": "Trouvez le synonyme exact ou la définition précise des mots rares.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Que signifie le mot 'Résilience' ?",
                "options": ["La tendance à abandonner facilement", "La capacité à surmonter les épreuves et à rebondir", "La rancune persistante", "La lenteur d'action"],
                "correct": 1,
                "explanation": "En psychologie et physique, la résilience désigne l'aptitude à absorber un choc et à reprendre sa trajectoire."
            },
            {
                "question": "Quel est le synonyme de 'Bienveillant' ?",
                "options": ["Hostile", "Indifférent", "Bénévole et charitable", "Sévère"],
                "correct": 2,
                "explanation": "La bienveillance est une disposition d'esprit favorable qui vise le bien d'autrui."
            }
        ]
    },
    {
        "id": "jeu_orthographe",
        "name": "Jeu d’orthographe",
        "category": "Langues",
        "icon": "✍️",
        "rules": "Repérez la seule orthographe correcte parmi les propositions trompeuses.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 130,
        "questions": [
            {
                "question": "Quelle est la graphie correcte de ce mot ?",
                "options": ["Dileme", "Dilemme", "Dilemn", "Dilemne"],
                "correct": 1,
                "explanation": "'Dilemme' prend deux 'm' et aucun 'n'."
            },
            {
                "question": "Choisissez la bonne proposition :",
                "options": ["Ils se sont succédés", "Ils se sont succédé", "Ils se sont succédées", "Ils se sont succédaient"],
                "correct": 1,
                "explanation": "Le verbe 'succéder' est transitif indirect (succéder à quelqu'un). Le participe passé reste invariable."
            }
        ]
    },
    {
        "id": "jeu_conjugaison",
        "name": "Jeu de conjugaison",
        "category": "Langues",
        "icon": "📝",
        "rules": "Complétez la phrase avec le verbe conjugué au bon mode et au bon temps.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Il faut que nous _________ attentifs à chaque détail.",
                "options": ["soyons", "sommes", "serons", "étions"],
                "correct": 0,
                "explanation": "Après la locution impersonnelle 'il faut que', on emploie obligatoirement le subjonctif présent : 'que nous soyons'."
            },
            {
                "question": "Demain, dès l'aube, je _________ sur le chemin de l'école.",
                "options": ["partis", "partirai", "partirait", "pars"],
                "correct": 1,
                "explanation": "L'action future certaine s'exprime au futur simple de l'indicatif : 'je partirai'."
            }
        ]
    },
    {
        "id": "jeu_grammaire",
        "name": "Jeu de grammaire",
        "category": "Langues",
        "icon": "📚",
        "rules": "Identifiez les natures et fonctions des mots dans les propositions.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Dans la phrase : 'L'élève lit un livre passionnant', quelle est la fonction de 'un livre passionnant' ?",
                "options": ["Sujet", "Complément d'Objet Direct (COD)", "Complément d'Objet Indirect (COI)", "Attribut du sujet"],
                "correct": 1,
                "explanation": "L'élève lit quoi ? 'un livre passionnant'. C'est un complément d'objet direct directement rattaché au verbe."
            }
        ]
    },
    {
        "id": "jeu_logique",
        "name": "Jeu de logique",
        "category": "Logique & Réflexion",
        "icon": "🧩",
        "rules": "Trouvez le terme suivant de la suite logique numérique ou visuelle.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 140,
        "questions": [
            {
                "question": "Complétez la suite : 2, 4, 8, 16, 32, ... ?",
                "options": ["48", "60", "64", "128"],
                "correct": 2,
                "explanation": "Chaque nombre est le double du précédent (puissances de 2). 32 × 2 = 64."
            },
            {
                "question": "Complétez la suite : 1, 1, 2, 3, 5, 8, 13, ... ?",
                "options": ["18", "20", "21", "24"],
                "correct": 2,
                "explanation": "C'est la suite de Fibonacci : chaque terme est la somme des deux précédents (8 + 13 = 21)."
            }
        ]
    },
    {
        "id": "jeu_memoire",
        "name": "Jeu de mémoire",
        "category": "Logique & Réflexion",
        "icon": "🧠",
        "rules": "Mémorisez les paires de cartes et retournez-les avec un minimum de coups.",
        "hasTimer": True,
        "timeLimit": 30,
        "points": 150,
        "questions": [
            {
                "question": "Quelle association de mot et de symbole est exacte ?",
                "options": ["Haïti - 🇭🇹", "Japon - 🇫🇷", "Brésil - 🇨🇦", "France - 🇯🇵"],
                "correct": 0,
                "explanation": "Le drapeau haïtien correspond au symbole 🇭🇹."
            }
        ]
    },
    {
        "id": "jeu_reflexion",
        "name": "Jeu de réflexion",
        "category": "Logique & Réflexion",
        "icon": "💡",
        "rules": "Exercez votre esprit critique face à des dilemmes d'optimisation.",
        "hasTimer": True,
        "timeLimit": 25,
        "points": 130,
        "questions": [
            {
                "question": "Un fermier doit traverser une rivière avec un loup, une chèvre et un chou. Que doit-il faire traverser en premier ?",
                "options": ["Le loup", "La chèvre", "Le chou", "Rien"],
                "correct": 1,
                "explanation": "S'il emmène le loup, la chèvre mange le chou. S'il emmène le chou, le loup mange la chèvre. Il doit donc emmener la chèvre en premier !"
            }
        ]
    },
    {
        "id": "jeu_enigmes",
        "name": "Jeu de résolution d’énigmes",
        "category": "Logique & Réflexion",
        "icon": "🔮",
        "rules": "Dévoilez le mystère caché derrière les énigmes poétiques et logiques.",
        "hasTimer": True,
        "timeLimit": 25,
        "points": 140,
        "questions": [
            {
                "question": "Plus je suis chaud, plus je suis frais. Qui suis-je ?",
                "options": ["L'eau", "Le pain", "Le café", "Le soleil"],
                "correct": 1,
                "explanation": "Le pain frais qui sort du four est encore chaud !"
            },
            {
                "question": "J'ai des villes mais pas de maisons, des forêts mais pas d'arbres, des rivières mais pas d'eau. Qui suis-je ?",
                "options": ["Une carte géographique", "Un dictionnaire", "Un miroir", "Un rêve"],
                "correct": 0,
                "explanation": "Une carte géographique représente tous ces éléments de manière cartographique."
            }
        ]
    },
    {
        "id": "jeu_animaux",
        "name": "Jeu de reconnaissance des animaux",
        "category": "Sciences & Nature",
        "icon": "🦁",
        "rules": "Reconnaissez les animaux selon leurs caractéristiques, régimes alimentaires et habitats.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 110,
        "questions": [
            {
                "question": "Quel est le plus grand mammifère marin vivant sur Terre ?",
                "options": ["Le requin blanc", "La baleine bleue", "L'orque", "Le cachalot"],
                "correct": 1,
                "explanation": "La baleine bleue (Rorqual bleu) peut atteindre plus de 30 mètres de long et peser 180 tonnes."
            },
            {
                "question": "Quel oiseau palmipède est incapable de voler mais excelle à la nage dans les eaux glaciales ?",
                "options": ["L'albatros", "Le manchot", "La mouette", "Le pélican"],
                "correct": 1,
                "explanation": "Les manchots vivent principalement dans l'hémisphère sud et nagent avec une agilité remarquable."
            }
        ]
    },
    {
        "id": "jeu_plantes",
        "name": "Jeu de reconnaissance des plantes",
        "category": "Sciences & Nature",
        "icon": "🌿",
        "rules": "Identifiez les arbres, fleurs, feuilles et plantes médicinales.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 110,
        "questions": [
            {
                "question": "Quel arbre tropical emblématique des Caraïbes et d'Afrique a un tronc massif capable de stocker l'eau ?",
                "options": ["Le baobab", "Le chêne", "Le bouleau", "Le sapin"],
                "correct": 0,
                "explanation": "Le baobab peut stocker des milliers de litres d'eau dans son tronc spongieux pour survivre aux sécheresses."
            }
        ]
    },
    {
        "id": "jeu_systeme_solaire",
        "name": "Jeu du système solaire",
        "category": "Sciences & Espace",
        "icon": "🪐",
        "rules": "Explorez les planètes rocheuses et gazeuses ainsi que leurs lunes.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Quelle est la plus grande planète du système solaire ?",
                "options": ["Saturne", "Jupiter", "Neptune", "La Terre"],
                "correct": 1,
                "explanation": "Jupiter a une masse plus de 300 fois supérieure à celle de la Terre et possède la fameuse Grande Tache Rouge."
            },
            {
                "question": "Quelle planète est surnommée la 'Planète rouge' en raison de l'oxyde de fer sur sa surface ?",
                "options": ["Vénus", "Mars", "Mercure", "Uranus"],
                "correct": 1,
                "explanation": "Mars doit sa teinte rougeâtre caractéristique aux minéraux ferreux qui recouvrent son sol."
            }
        ]
    },
    {
        "id": "jeu_programmation",
        "name": "Jeu de programmation",
        "category": "Informatique",
        "icon": "💻",
        "rules": "Devinez la sortie des algorithmes et identifiez les structures de code.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 140,
        "questions": [
            {
                "question": "En JavaScript ou Python, quelle valeur renvoie l'expression booléenne : (5 > 3) && (2 == 2) ?",
                "options": ["false", "true", "null", "undefined"],
                "correct": 1,
                "explanation": "Les deux conditions étant vraies, l'opérateur logique ET (&&) renvoie vrai (true)."
            },
            {
                "question": "Quel mot-clé permet de déclarer une fonction réutilisable en Python ?",
                "options": ["func", "def", "function", "lambda"],
                "correct": 1,
                "explanation": "En Python, on utilise l'instruction 'def' pour définir une fonction."
            }
        ]
    },
    {
        "id": "jeu_inventions",
        "name": "Jeu de reconnaissance des inventions",
        "category": "Histoire & Sciences",
        "icon": "💡",
        "rules": "Associez les grandes inventions révolutionnaires à leurs inventeurs et à leur époque.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Qui a mis au point l'imprimerie à caractères mobiles en Europe vers 1440 ?",
                "options": ["Johannes Gutenberg", "Isaac Newton", "Galilée", "Alexander Graham Bell"],
                "correct": 0,
                "explanation": "Johannes Gutenberg à Mayence a permis la diffusion de masse des livres et du savoir."
            },
            {
                "question": "Qui a découvert le premier antibiotique naturel, la pénicilline, en 1928 ?",
                "options": ["Louis Pasteur", "Alexander Fleming", "Marie Curie", "Robert Koch"],
                "correct": 1,
                "explanation": "Alexander Fleming a observé l'effet bactéricide de la moisissure Penicillium notatum."
            }
        ]
    },
    {
        "id": "jeu_personnalites",
        "name": "Jeu des grandes personnalités historiques",
        "category": "Histoire",
        "icon": "⭐",
        "rules": "Découvrez les citations, actes fondateurs et destins des figures universelles.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 120,
        "questions": [
            {
                "question": "Quel leader haïtien, précurseur de l'indépendance, était surnommé le 'Premier des Noirs' ?",
                "options": ["Jean-Jacques Dessalines", "Toussaint Louverture", "Alexandre Pétion", "Henri Christophe"],
                "correct": 1,
                "explanation": "Toussaint Louverture a été un stratège militaire et homme politique de premier plan avant d'être déporté au fort de Joux."
            },
            {
                "question": "Quel prix Nobel de la paix a lutté pacifiquement contre l'apartheid en Afrique du Sud et est devenu président en 1994 ?",
                "options": ["Kofi Annan", "Nelson Mandela", "Desmond Tutu", "Thomas Sankara"],
                "correct": 1,
                "explanation": "Nelson Mandela, après 27 ans de prison, a incarné la réconciliation nationale sud-africaine."
            }
        ]
    },
    {
        "id": "jeu_monuments",
        "name": "Jeu de reconnaissance des monuments",
        "category": "Culture & Géographie",
        "icon": "🏰",
        "rules": "Identifiez les merveilles architecturales et monuments du patrimoine mondial.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Dans quel pays se dresse la majestueuse Citadelle Laferrière, chef-d'œuvre du roi Henri Christophe classé à l'UNESCO ?",
                "options": ["Jamaïque", "Haïti", "Cuba", "Porto Rico"],
                "correct": 1,
                "explanation": "La Citadelle Laferrière, située à Milot dans le Nord d'Haïti, est la plus grande forteresse des Caraïbes."
            },
            {
                "question": "Où se trouve le Taj Mahal, mausolée de marbre blanc bâti par l'empereur Shâh Jahân ?",
                "options": ["En Égypte", "En Inde (Agra)", "En Turquie", "En Iran"],
                "correct": 1,
                "explanation": "Le Taj Mahal se dresse au bord de la rivière Yamuna à Agra, en Inde."
            }
        ]
    },
    {
        "id": "jeu_fractions",
        "name": "Jeu de calcul de fractions",
        "category": "Mathématiques",
        "icon": "½",
        "rules": "Simplifiez, additionnez et comparez des fractions avec précision.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 130,
        "questions": [
            {
                "question": "Combien vaut 1/2 + 1/4 ?",
                "options": ["2/6", "3/4", "1/6", "2/4"],
                "correct": 1,
                "explanation": "1/2 = 2/4. Donc 2/4 + 1/4 = 3/4."
            },
            {
                "question": "Quelle est la forme irréductible de la fraction 18/24 ?",
                "options": ["9/12", "6/8", "3/4", "2/3"],
                "correct": 2,
                "explanation": "En divisant le numérateur et le dénominateur par 6, on obtient 3/4."
            }
        ]
    },
    {
        "id": "jeu_geometrie",
        "name": "Jeu de géométrie",
        "category": "Mathématiques",
        "icon": "🔺",
        "rules": "Calculez des aires, périmètres et identifiez les théorèmes géométriques.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 120,
        "questions": [
            {
                "question": "Dans un triangle rectangle, si les côtés de l'angle droit mesurent 3 cm et 4 cm, que vaut l'hypoténuse ?",
                "options": ["5 cm", "6 cm", "7 cm", "8 cm"],
                "correct": 0,
                "explanation": "D'après Pythagore : 3² + 4² = 9 + 16 = 25. √25 = 5 cm."
            },
            {
                "question": "Quelle est l'aire d'un rectangle de longueur 8 cm et de largeur 5 cm ?",
                "options": ["26 cm²", "40 cm²", "30 cm²", "13 cm²"],
                "correct": 1,
                "explanation": "Aire = Longueur × largeur = 8 × 5 = 40 cm²."
            }
        ]
    },
    {
        "id": "jeu_physique",
        "name": "Jeu de physique",
        "category": "Sciences",
        "icon": "⚛️",
        "rules": "Appliquez les lois de la mécanique, de l'électricité et de l'énergie.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 120,
        "questions": [
            {
                "question": "Quelle est l'unité de mesure de la tension électrique dans le Système International ?",
                "options": ["L'Ampère (A)", "Le Volt (V)", "L'Ohm (Ω)", "Le Watt (W)"],
                "correct": 1,
                "explanation": "La tension s'exprime en Volts (V), nommée en hommage à Alessandro Volta."
            },
            {
                "question": "À quelle vitesse la lumière se déplace-t-elle approximativement dans le vide ?",
                "options": ["300 km/s", "300 000 km/s", "3 000 000 km/s", "Le son"],
                "correct": 1,
                "explanation": "La vitesse de la lumière (c) est d'environ 299 792 458 m/s, soit environ 300 000 km/s."
            }
        ]
    },
    {
        "id": "jeu_chimie",
        "name": "Jeu de chimie",
        "category": "Sciences",
        "icon": "🧪",
        "rules": "Explorez le tableau de Mendeleïev, les liaisons atomiques et les états de la matière.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 120,
        "questions": [
            {
                "question": "Quel est le symbole chimique de l'or dans le tableau périodique ?",
                "options": ["Or", "Au", "Ag", "Fe"],
                "correct": 1,
                "explanation": "'Au' provient du latin 'Aurum' désignant l'or."
            },
            {
                "question": "Quel est le pH d'une solution neutre à 25°C ?",
                "options": ["0", "7", "14", "10"],
                "correct": 1,
                "explanation": "Un pH inférieur à 7 est acide, un pH supérieur à 7 est basique, et 7 est neutre."
            }
        ]
    },
    {
        "id": "jeu_mots_meles",
        "name": "Jeu de mots mêlés",
        "category": "Langues & Mots",
        "icon": "🔠",
        "rules": "Retrouvez les mots cachés dans la grille thématique par déduction rapide.",
        "hasTimer": True,
        "timeLimit": 25,
        "points": 130,
        "questions": [
            {
                "question": "Quel mot relié à l'amitié et au courage retrouve-t-on en anagramme de 'NOINU' ?",
                "options": ["UNION", "NOUVEAU", "UNIQUE", "NOIR"],
                "correct": 0,
                "explanation": "U-N-I-O-N forme la devise 'L'Union fait la force'."
            }
        ]
    },
    {
        "id": "jeu_mots_croises",
        "name": "Jeu de mots croisés",
        "category": "Langues & Mots",
        "icon": "🔲",
        "rules": "Complétez les définitions horizontales et verticales avec les termes exacts.",
        "hasTimer": True,
        "timeLimit": 25,
        "points": 130,
        "questions": [
            {
                "question": "Définition : 'Capitale historique d'Haïti où fut signée l'indépendance' (8 lettres)",
                "options": ["GONAIVES", "JACMEL", "LEOGANE", "PORT-DE-PAIX"],
                "correct": 0,
                "explanation": "Les Gonaïves est reconnue comme la Cité de l'Indépendance d'Haïti."
            }
        ]
    },
    {
        "id": "jeu_classement",
        "name": "Jeu de classement",
        "category": "Logique & Savoir",
        "icon": "📊",
        "rules": "Classez les éléments du plus petit au plus grand ou selon un ordre logique.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 120,
        "questions": [
            {
                "question": "Classez ces unités de stockage numérique de la plus petite à la plus grande :",
                "options": [
                    "Octet < Kilooctet < Mégaoctet < Gigaoctet",
                    "Gigaoctet < Mégaoctet < Kilooctet < Octet",
                    "Kilooctet < Octet < Gigaoctet < Mégaoctet",
                    "Mégaoctet < Gigaoctet < Octet < Kilooctet"
                ],
                "correct": 0,
                "explanation": "1 Ko = 1024 octets, 1 Mo = 1024 Ko, 1 Go = 1024 Mo."
            }
        ]
    },
    {
        "id": "jeu_vrai_ou_faux",
        "name": "Jeu de vrai ou faux",
        "category": "Culture & Savoir",
        "icon": "✔️",
        "rules": "Démêlez le vrai du faux en un clin d'œil sur des affirmations scientifiques et historiques.",
        "hasTimer": True,
        "timeLimit": 10,
        "points": 110,
        "questions": [
            {
                "question": "Affirmation : La Terre tourne autour du Soleil en environ 365 jours et 6 heures.",
                "options": ["VRAI", "FAUX"],
                "correct": 0,
                "explanation": "C'est exact ! Ces 6 heures supplémentaires justifient l'ajout d'une journée bissextile tous les 4 ans."
            },
            {
                "question": "Affirmation : La Grande Muraille de Chine est clairement visible à l'œil nu depuis la Lune.",
                "options": ["VRAI", "FAUX"],
                "correct": 1,
                "explanation": "FAUX ! C'est un mythe populaire réfuté par tous les astronautes ; elle est trop étroite pour être distinguée sans instrument."
            }
        ]
    },
    {
        "id": "jeu_questions_chronometrees",
        "name": "Jeu de questions chronométrées",
        "category": "Défi Vitesse",
        "icon": "⏱️",
        "rules": "Enchaînez un maximum de bonnes réponses en moins de 10 secondes par question.",
        "hasTimer": True,
        "timeLimit": 10,
        "points": 160,
        "questions": [
            {
                "question": "Quel gaz est indispensable à la respiration des êtres humains ?",
                "options": ["L'oxygène (O2)", "Le méthane (CH4)", "L'azote pur", "L'hélium"],
                "correct": 0,
                "explanation": "L'oxygène permet la respiration cellulaire et la production d'énergie vitale."
            },
            {
                "question": "Combien de jours compte une année bissextile ?",
                "options": ["364", "365", "366", "367"],
                "correct": 2,
                "explanation": "Un 29 février est ajouté, portant l'année à 366 jours."
            }
        ]
    },
    {
        "id": "jeu_defis_educatifs",
        "name": "Jeu de défis éducatifs",
        "category": "Défis & Missions",
        "icon": "🏆",
        "rules": "Relevez des défis thématiques interdisciplinaires pour débloquer des trophées d'excellence.",
        "hasTimer": True,
        "timeLimit": 25,
        "points": 150,
        "questions": [
            {
                "question": "Défi synthèse : Quel domaine combine programmation, mathématiques matricielles et logique pour créer des agents intelligents ?",
                "options": ["L'intelligence artificielle", "La menuiserie", "L'archéologie ancienne", "L'océanographie"],
                "correct": 0,
                "explanation": "L'IA s'appuie sur l'algèbre linéaire, l'optimisation et le code informatique."
            }
        ]
    },
    {
        "id": "jeu_cartes_educatives",
        "name": "Jeu de cartes éducatives",
        "category": "Flashcards",
        "icon": "🃏",
        "rules": "Révisez avec des cartes mémoires interactives pour ancrer vos acquis à long terme.",
        "hasTimer": False,
        "timeLimit": 0,
        "points": 100,
        "questions": [
            {
                "question": "Carte Flash : 'Égalité et Fraternité' sont associées à quelle troisième valeur dans la devise républicaine ?",
                "options": ["Liberté", "Richesse", "Force", "Gloire"],
                "correct": 0,
                "explanation": "'Liberté, Égalité, Fraternité'."
            }
        ]
    },
    {
        "id": "jeu_mots_images",
        "name": "Jeu de correspondance entre mots et images",
        "category": "Visuel & Langues",
        "icon": "🖼️",
        "rules": "Associez les mots descriptifs aux représentations iconographiques adéquates.",
        "hasTimer": True,
        "timeLimit": 15,
        "points": 120,
        "questions": [
            {
                "question": "Quelle image correspond à 'Un instrument optique pour observer les étoiles' ?",
                "options": ["Télescope 🔭", "Microscope 🔬", "Boussole 🧭", "Sablier ⏳"],
                "correct": 0,
                "explanation": "Le télescope sert à scruter les corps célestes lointains."
            }
        ]
    },
    {
        "id": "jeu_puzzles_geographiques",
        "name": "Jeu de puzzles géographiques",
        "category": "Géographie",
        "icon": "🧩",
        "rules": "Assemblez les pays frontaliers, îles et archipels pour reconstituer la carte.",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 130,
        "questions": [
            {
                "question": "Quel pays partage l'île d'Hispaniola avec la République d'Haïti à l'est ?",
                "options": ["La République dominicaine", "Cuba", "La Jamaïque", "Bahamas"],
                "correct": 0,
                "explanation": "L'île d'Hispaniola est partagée entre Haïti à l'ouest et la République dominicaine à l'est."
            }
        ]
    },
    {
        "id": "jeu_defis_amis",
        "name": "Jeu de défis entre amis",
        "category": "Multi-joueurs",
        "icon": "👥",
        "rules": "Jouez à tour de rôle sur le même écran ou défiez vos contacts dans James Chat !",
        "hasTimer": True,
        "timeLimit": 20,
        "points": 200,
        "questions": [
            {
                "question": "Duel : Qui a découvert la loi de la gravitation universelle après avoir observé une pomme tomber ?",
                "options": ["Isaac Newton", "Albert Einstein", "Nikola Tesla", "Thomas Edison"],
                "correct": 0,
                "explanation": "Isaac Newton a publié les Principes mathématiques de la philosophie naturelle en 1687."
            },
            {
                "question": "Duel : Quel est l'organe humain qui consomme à lui seul environ 20% de l'énergie et de l'oxygène du corps ?",
                "options": ["Le cerveau", "L'estomac", "Le rein", "La peau"],
                "correct": 0,
                "explanation": "Bien qu'il ne représente que 2% du poids corporel, le cerveau consomme un cinquième de notre énergie."
            }
        ]
    }
]

assert len(GAMES_CONFIG) == 40, f"Expected 40 games, got {len(GAMES_CONFIG)}"
print(f"Total defined games: {len(GAMES_CONFIG)}")

# ==========================================
# 4. EXPORT TO ASSETS
# ==========================================

curriculum = generate_curriculum()
print(f"Generated {len(curriculum)} subjects with full structured chapters, exercises, quizzes, exams.")

assets_dir = os.path.abspath("app/src/main/assets")
os.makedirs(assets_dir, exist_ok=True)

# Write curriculum_data.js
curriculum_js = "window.JAMES_CURRICULUM = " + json.dumps({
    "domains": DOMAINS_CONFIG,
    "subjects": curriculum,
    "totalSubjects": len(curriculum)
}, ensure_ascii=False, indent=2) + ";\n"

with open(os.path.join(assets_dir, "curriculum_data.js"), "w", encoding="utf-8") as f:
    f.write(curriculum_js)
print(f"Wrote curriculum_data.js ({len(curriculum_js)} bytes)")

# Write games_data.js
games_js = "window.JAMES_GAMES = " + json.dumps({
    "games": GAMES_CONFIG,
    "totalGames": len(GAMES_CONFIG)
}, ensure_ascii=False, indent=2) + ";\n"

with open(os.path.join(assets_dir, "games_data.js"), "w", encoding="utf-8") as f:
    f.write(games_js)
print(f"Wrote games_data.js ({len(games_js)} bytes)")

print("Done generating academy and games assets!")
