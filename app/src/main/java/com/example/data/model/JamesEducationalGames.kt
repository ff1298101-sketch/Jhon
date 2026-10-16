package com.example.data.model

data class JamesEducationalGame(
    val id: String,
    val name: String,
    val category: String,
    val icon: String,
    val rules: String,
    val difficulty: String = "Débutant à Expert",
    val points: Int = 100,
    val hasTimer: Boolean = true
)

object JamesEducationalGamesCatalog {
    val ALL_40_GAMES = listOf(
        JamesEducationalGame(
            id = "quiz_culture_generale",
            name = "Quiz de culture générale",
            category = "Culture & Savoir",
            icon = "🧠",
            rules = "Répondez correctement au maximum de questions de culture générale mondiale et caribéenne en un temps record.",
            points = 100
        ),
        JamesEducationalGame(
            id = "jeu_capitales",
            name = "Jeu des capitales",
            category = "Géographie",
            icon = "🏛️",
            rules = "Identifiez la capitale exacte des pays du monde entier à partir de leur situation géographique.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_drapeaux",
            name = "Jeu des drapeaux",
            category = "Géographie",
            icon = "🚩",
            rules = "Reconnaissez le pays correspondant aux couleurs, emblèmes et symboles de son drapeau national.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_geographie",
            name = "Jeu de géographie",
            category = "Géographie",
            icon = "🗺️",
            rules = "Localisez les continents, océans, fleuves majeurs, reliefs et détroits de notre planète.",
            points = 110
        ),
        JamesEducationalGame(
            id = "jeu_histoire",
            name = "Jeu d’histoire",
            category = "Histoire",
            icon = "⚔️",
            rules = "Testez vos connaissances sur l'histoire d'Haïti (1804), de l'Afrique, des Amériques et du monde.",
            points = 110
        ),
        JamesEducationalGame(
            id = "jeu_chronologie_historique",
            name = "Jeu de chronologie historique",
            category = "Histoire",
            icon = "⏳",
            rules = "Replacez les grands événements historiques, traités et découvertes dans leur ordre temporel exact.",
            points = 140
        ),
        JamesEducationalGame(
            id = "jeu_mathematiques",
            name = "Jeu de mathématiques",
            category = "Mathématiques",
            icon = "📐",
            rules = "Résolvez des équations, énigmes de géométrie et calculs d'algèbre avec logique et méthode.",
            points = 130
        ),
        JamesEducationalGame(
            id = "jeu_calcul_rapide",
            name = "Jeu de calcul rapide",
            category = "Mathématiques",
            icon = "⚡",
            rules = "Enchaînez les additions, multiplications et divisions de tête en moins de 10 secondes par calcul.",
            points = 150
        ),
        JamesEducationalGame(
            id = "jeu_sciences",
            name = "Jeu de sciences",
            category = "Sciences",
            icon = "🔬",
            rules = "Explorez les lois fondamentales de la physique, de la chimie et de la biologie du vivant.",
            points = 110
        ),
        JamesEducationalGame(
            id = "jeu_corps_humain",
            name = "Jeu du corps humain",
            category = "Sciences",
            icon = "🫀",
            rules = "Identifiez les organes vitaux, os du squelette, vaisseaux sanguins et fonctions physiologiques.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_langues",
            name = "Jeu des langues",
            category = "Langues",
            icon = "🗣️",
            rules = "Découvrez les idiomes du monde, comparez le créole, le français, l'anglais, l'espagnol et d'autres langues.",
            points = 110
        ),
        JamesEducationalGame(
            id = "jeu_vocabulaire",
            name = "Jeu de vocabulaire",
            category = "Langues",
            icon = "📖",
            rules = "Enrichissez votre lexique avec des synonymes, antonymes et expressions littéraires choisies.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_orthographe",
            name = "Jeu d’orthographe",
            category = "Langues",
            icon = "✍️",
            rules = "Dénichez les pièges orthographiques, consonnes doubles et participes passés délicats.",
            points = 130
        ),
        JamesEducationalGame(
            id = "jeu_conjugaison",
            name = "Jeu de conjugaison",
            category = "Langues",
            icon = "📝",
            rules = "Conjuguez les verbes au subjonctif, conditionnel, futur simple et temps du passé sans faute.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_grammaire",
            name = "Jeu de grammaire",
            category = "Langues",
            icon = "📚",
            rules = "Analysez la nature et la fonction des mots dans les propositions pour maîtriser la syntaxe.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_logique",
            name = "Jeu de logique",
            category = "Logique & Réflexion",
            icon = "🧩",
            rules = "Complétez les suites logiques numériques et graphiques par déduction méthodique.",
            points = 140
        ),
        JamesEducationalGame(
            id = "jeu_memoire",
            name = "Jeu de mémoire",
            category = "Logique & Réflexion",
            icon = "🧠",
            rules = "Mémorisez les emplacements de paires de cartes éducatives et retournez-les avec un minimum de coups.",
            points = 150
        ),
        JamesEducationalGame(
            id = "jeu_reflexion",
            name = "Jeu de réflexion",
            category = "Logique & Réflexion",
            icon = "💡",
            rules = "Résolvez des dilemmes d'optimisation et des casse-têtes pour aiguiser votre pensée critique.",
            points = 130
        ),
        JamesEducationalGame(
            id = "jeu_enigmes",
            name = "Jeu de résolution d’énigmes",
            category = "Logique & Réflexion",
            icon = "🔮",
            rules = "Décryptez les devinettes poétiques et les énigmes culturelles du patrimoine mondial.",
            points = 140
        ),
        JamesEducationalGame(
            id = "jeu_animaux",
            name = "Jeu de reconnaissance des animaux",
            category = "Sciences",
            icon = "🦁",
            rules = "Reconnaissez les espèces terrestres, marines et aériennes selon leurs cris, pelages et habitats.",
            points = 110
        ),
        JamesEducationalGame(
            id = "jeu_plantes",
            name = "Jeu de reconnaissance des plantes",
            category = "Sciences",
            icon = "🌿",
            rules = "Identifiez les arbres remarquables, fleurs tropicales et plantes médicinales traditionnelles.",
            points = 110
        ),
        JamesEducationalGame(
            id = "jeu_systeme_solaire",
            name = "Jeu du système solaire",
            category = "Sciences",
            icon = "🪐",
            rules = "Voyagez de Mercure à Neptune et découvrez les caractéristiques de chaque planète et lune.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_programmation",
            name = "Jeu de programmation",
            category = "Informatique",
            icon = "💻",
            rules = "Prédisez l'exécution d'algorithmes simples en Python, JavaScript et Kotlin.",
            points = 140
        ),
        JamesEducationalGame(
            id = "jeu_inventions",
            name = "Jeu de reconnaissance des inventions",
            category = "Histoire",
            icon = "💡",
            rules = "Associez les grandes inventions (imprimerie, vaccin, transistor, Internet) à leurs pionniers.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_personnalites",
            name = "Jeu des grandes personnalités historiques",
            category = "Histoire",
            icon = "⭐",
            rules = "Retrouvez les discours et hauts faits des héros nationaux haïtiens et leaders mondiaux.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_monuments",
            name = "Jeu de reconnaissance des monuments",
            category = "Culture & Savoir",
            icon = "🏰",
            rules = "Reconnaissez la Citadelle Laferrière, le Taj Mahal, la Tour Eiffel et les trésors de l'UNESCO.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_fractions",
            name = "Jeu de calcul de fractions",
            category = "Mathématiques",
            icon = "½",
            rules = "Additionnez, simplifiez et comparez les fractions numériques avec rapidité et exactitude.",
            points = 130
        ),
        JamesEducationalGame(
            id = "jeu_geometrie",
            name = "Jeu de géométrie",
            category = "Mathématiques",
            icon = "🔺",
            rules = "Appliquez le théorème de Pythagore, calculez des périmètres et aires de figures usuelles.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_physique",
            name = "Jeu de physique",
            category = "Sciences",
            icon = "⚛️",
            rules = "Démontrez votre maîtrise des circuits électriques, de la vitesse de la lumière et des forces.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_chimie",
            name = "Jeu de chimie",
            category = "Sciences",
            icon = "🧪",
            rules = "Manipulez le tableau périodique des éléments, les formules moléculaires et les réactions chimiques.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_mots_meles",
            name = "Jeu de mots mêlés",
            category = "Langues",
            icon = "🔠",
            rules = "Repérez les mots éducatifs dissimulés dans les grilles thématiques par déduction lexicale.",
            points = 130
        ),
        JamesEducationalGame(
            id = "jeu_mots_croises",
            name = "Jeu de mots croisés",
            category = "Langues",
            icon = "🔲",
            rules = "Complétez les définitions pédagogiques croisées pour former les concepts clés.",
            points = 130
        ),
        JamesEducationalGame(
            id = "jeu_classement",
            name = "Jeu de classement",
            category = "Logique & Réflexion",
            icon = "📊",
            rules = "Ordonnez les éléments selon leur taille, leur échelle numérique ou leur chronologie.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_vrai_ou_faux",
            name = "Jeu de vrai ou faux",
            category = "Culture & Savoir",
            icon = "✔️",
            rules = "Démêlez les mythes des vérités scientifiques et historiques en un temps record.",
            points = 110
        ),
        JamesEducationalGame(
            id = "jeu_questions_chronometrees",
            name = "Jeu de questions chronométrées",
            category = "Défis & Compétition",
            icon = "⏱️",
            rules = "Une course contre la montre : chaque seconde compte pour accumuler un score record !",
            points = 160
        ),
        JamesEducationalGame(
            id = "jeu_defis_educatifs",
            name = "Jeu de défis éducatifs",
            category = "Défis & Compétition",
            icon = "🏆",
            rules = "Relevez des défis interdisciplinaires exigeants pour tester l'étendue de vos savoirs.",
            points = 150
        ),
        JamesEducationalGame(
            id = "jeu_cartes_educatives",
            name = "Jeu de cartes éducatives",
            category = "Culture & Savoir",
            icon = "🃏",
            rules = "Révisez avec des fiches mémoires (flashcards) pour un ancrage cognitif durable.",
            points = 100,
            hasTimer = false
        ),
        JamesEducationalGame(
            id = "jeu_mots_images",
            name = "Jeu de correspondance entre mots et images",
            category = "Culture & Savoir",
            icon = "🖼️",
            rules = "Reliez les définitions littéraires ou scientifiques à leurs représentations visuelles.",
            points = 120
        ),
        JamesEducationalGame(
            id = "jeu_puzzles_geographiques",
            name = "Jeu de puzzles géographiques",
            category = "Géographie",
            icon = "🧩",
            rules = "Reconstituez les frontières et territoires insulaires des Caraïbes et des continents.",
            points = 130
        ),
        JamesEducationalGame(
            id = "jeu_defis_amis",
            name = "Jeu de défis entre amis",
            category = "Défis & Compétition",
            icon = "👥",
            rules = "Défiez vos proches ou contacts de James Chat dans un duel de connaissances passionnant !",
            points = 200
        )
    )
}
