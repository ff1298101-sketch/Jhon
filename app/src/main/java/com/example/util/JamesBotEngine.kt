package com.example.util

import com.example.data.model.AppLanguage
import com.example.data.model.BotKnowledgeEntity
import java.text.Normalizer
import java.util.Locale

/**
 * Moteur de réponse déterministe et intelligent pour James Bot :
 * 1. Détection des commandes (/help, /rules, /games, /support, /profile)
 * 2. Détection d'intentions de productivité (/rewrite, /formal, /casual, /correct, etc.)
 * 3. Recherche dans la base de connaissances validée par les administrateurs (Firestore / Room)
 * 4. Tolérance aux fautes d'orthographe, variations linguistiques et formulations alternatives
 * 5. Support multilingue natif (Français, Kreyòl Ayisyen, Anglais, Espagnol)
 * 6. Aucune API payante requise : 100% autonome, rapide, sécurisé
 */
object JamesBotEngine {

    const val BOT_USER_ID = "james_bot_official"
    const val BOT_USERNAME = "james_bot"
    const val BOT_DISPLAY_NAME = "James Bot"
    const val BOT_EMAIL = "bot@jameschat.ht"

    data class BotResponseResult(
        val replyText: String,
        val matchedKnowledgeId: String? = null,
        val canCopy: Boolean = true,
        val suggestedQuestions: List<String> = emptyList()
    )

    /**
     * Traite un message utilisateur adressé à James Bot.
     */
    fun processMessage(
        userInput: String,
        knowledgeBase: List<BotKnowledgeEntity>,
        userLanguage: AppLanguage
    ): BotResponseResult {
        val raw = userInput.trim()
        if (raw.isEmpty()) {
            return BotResponseResult(
                replyText = getGreeting(userLanguage),
                suggestedQuestions = getQuickSuggestions(userLanguage)
            )
        }

        // 1. Commandes directes
        if (raw.startsWith("/")) {
            val parts = raw.split(" ", limit = 2)
            val cmd = parts[0].lowercase(Locale.ROOT)
            val arg = if (parts.size > 1) parts[1].trim() else ""
            return handleCommand(cmd, arg, userLanguage)
        }

        val normalizedInput = normalizeText(raw)

        // Détection naturelle de demande de jeux / mini-jeux / /games
        if (normalizedInput.contains("game") || normalizedInput.contains("jwet") ||
            normalizedInput.contains("jwe") || normalizedInput.contains("jeux") ||
            normalizedInput.contains("jouer") || normalizedInput.contains("devinette")
        ) {
            return handleCommand("/games", "", userLanguage)
        }

        // 2. Recherche intelligente dans la base de connaissances validée
        val bestMatch = findBestMatchInKnowledge(normalizedInput, knowledgeBase, userLanguage)

        if (bestMatch != null) {
            return BotResponseResult(
                replyText = bestMatch.answer,
                matchedKnowledgeId = bestMatch.id,
                canCopy = true,
                suggestedQuestions = getRelatedSuggestions(bestMatch.category, userLanguage)
            )
        }

        // 3. Outils textuels intégrés (reformulation, correction basique si mentionné)
        if (isTextAssistanceRequest(normalizedInput)) {
            return handleTextAssistance(raw, userLanguage)
        }

        // 4. Si aucune réponse validée n'existe : ne JAMAIS inventer, expliquer courtoisement et proposer le support
        return fallbackResponse(userLanguage)
    }

    private fun handleCommand(cmd: String, arg: String, lang: AppLanguage): BotResponseResult {
        return when (cmd) {
            "/help", "/aide", "/ede" -> {
                val text = when (lang) {
                    AppLanguage.KREYOL ->
                        "🤖 **KÒMAND JAMES BOT DISPONIB** :\n\n" +
                        "• `/help` : Afiche èd ak kòmand yo\n" +
                        "• `/rules` : Règleman kominote James Chat\n" +
                        "• `/games` : Mini-jwèt ak defi nan chat la\n" +
                        "• `/support` : Kontakte asistans ofisyèl\n" +
                        "• `/profile` : Wè pwofil ak kapasite James Bot\n" +
                        "• `/reformuler [tèks]` : Refòmile yon fraz an fason pwofesyonèl\n" +
                        "• `/korije [tèks]` : Korije epi amelyore yon mesaj\n\n" +
                        "💡 Ou ka poze m nenpòt kesyon sou kijan James Chat fonksyone !"
                    AppLanguage.ENGLISH ->
                        "🤖 **JAMES BOT COMMANDS**:\n\n" +
                        "• `/help`: Display available commands\n" +
                        "• `/rules`: James Chat community rules\n" +
                        "• `/games`: Mini-games and trivia\n" +
                        "• `/support`: Contact human support team\n" +
                        "• `/profile`: View James Bot identity & status\n" +
                        "• `/rewrite [text]`: Reformulate sentence politely\n" +
                        "• `/correct [text]`: Proofread and refine a message\n\n" +
                        "💡 Ask me any question about James Chat features!"
                    AppLanguage.ESPANOL ->
                        "🤖 **COMANDOS DE JAMES BOT**:\n\n" +
                        "• `/help`: Mostrar ayuda y comandos\n" +
                        "• `/rules`: Normas y reglas de la comunidad\n" +
                        "• `/games`: Mini-juegos y retos divertidos\n" +
                        "• `/support`: Contactar al soporte oficial\n" +
                        "• `/profile`: Ver perfil oficial de James Bot\n" +
                        "• `/reformular [texto]`: Reformular mensaje profesionalmente\n\n" +
                        "💡 ¡Pregúntame cualquier duda sobre el funcionamiento de James Chat!"
                    else ->
                        "🤖 **COMMANDES OFFICIELLES DE JAMES BOT** :\n\n" +
                        "• `/help` : Afficher la liste des commandes et l'aide\n" +
                        "• `/rules` : Règlement officiel et consignes de sécurité\n" +
                        "• `/games` : Mini-jeux, devinettes et quiz dans le chat\n" +
                        "• `/support` : Joindre l'assistance et les administrateurs\n" +
                        "• `/profile` : Consulter la fiche d'identité de James Bot\n" +
                        "• `/reformuler [texte]` : Reformuler un message avec élégance\n" +
                        "• `/corriger [texte]` : Améliorer et corriger un texte\n\n" +
                        "💡 Vous pouvez aussi simplement me poser une question naturelle !"
                }
                BotResponseResult(replyText = text, suggestedQuestions = listOf("/rules", "/games", "/support", "/profile"))
            }

            "/rules", "/regles", "/regleman" -> {
                val text = when (lang) {
                    AppLanguage.KREYOL ->
                        "🛡️ **RÈG KONFÒMITE AK SEKIRITE JAMES CHAT** :\n\n" +
                        "1. **Respè mityèl** : Pa gen jouman, menas oswa arasman.\n" +
                        "2. **Zewo Spam** : Entèdi voye mesaj piblisite san otorizasyon.\n" +
                        "3. **Pwoteksyon vi prive** : Pa janm pataje enfòmasyon sekrè yon lòt moun.\n" +
                        "4. **Otantisite** : Pa imite administratè oswa lòt manm.\n" +
                        "5. **Sekirite kontni** : Tout kontni ilegal mennen nan bannisman imedyat.\n\n" +
                        "⚖️ Administrateur modération yo ap aplike sanksyon pou pwoteje kominote a."
                    AppLanguage.ENGLISH ->
                        "🛡️ **JAMES CHAT COMMUNITY RULES**:\n\n" +
                        "1. **Mutual Respect**: No harassment, hate speech, or abuse.\n" +
                        "2. **Zero Spam**: No unsolicited commercial broadcasting.\n" +
                        "3. **Privacy**: Never share other users' private data without consent.\n" +
                        "4. **No Impersonation**: Impersonating staff or others is forbidden.\n" +
                        "5. **Content Safety**: Illegal or violent content triggers an immediate ban.\n\n" +
                        "⚖️ Moderation admins strictly enforce these guidelines."
                    AppLanguage.ESPANOL ->
                        "🛡️ **NORMAS DE COMUNIDAD JAMES CHAT**:\n\n" +
                        "1. **Respeto mutuo**: Cero tolerancia al acoso o insultos.\n" +
                        "2. **Sin spam**: Prohibido enviar publicidad masiva.\n" +
                        "3. **Privacidad**: Protege tus datos y los de los demás.\n" +
                        "4. **No suplantación**: Prohibido hacerse pasar por administradores.\n" +
                        "5. **Seguridad**: El contenido ilegal conlleva suspensión permanente."
                    else ->
                        "🛡️ **RÈGLEMENT DE LA COMMUNAUTÉ JAMES CHAT** :\n\n" +
                        "1. **Respect absolu** : Pas d'insultes, harcèlement ou menaces.\n" +
                        "2. **Zéro Spam** : La diffusion massive non sollicitée est proscrite.\n" +
                        "3. **Confidentialité** : Ne partagez jamais les informations privées d'autrui.\n" +
                        "4. **Usurpation interdite** : Ne prétendez jamais être un administrateur officiel.\n" +
                        "5. **Contenus sécurisés** : Tout média illicite entraîne un bannissement irrévocable.\n\n" +
                        "⚖️ Les administrateurs de modération (Lilmontero444, escanorliondelorgueil9) veillent au respect de ces règles."
                }
                BotResponseResult(replyText = text, suggestedQuestions = listOf("/help", "/support", "Comment signaler un utilisateur ?"))
            }

            "/support", "/asistans" -> {
                val text = when (lang) {
                    AppLanguage.KREYOL ->
                        "📞 **ASISTANS & SIPÒ JAMES CHAT** :\n\n" +
                        "• **Imèl Sipò Ofisyèl** : support@jameschat.ht\n" +
                        "• **Administrasyon & Sekirite** : admin@jameschat.ht\n\n" +
                        "📝 Si w rankontre yon pwoblèm oswa w vle rapòte yon abi, ou ka itilize bouton 'Signaler 🚨' dirèkteman nan meni mesaj la oswa pwofil la."
                    AppLanguage.ENGLISH ->
                        "📞 **JAMES CHAT OFFICIAL SUPPORT**:\n\n" +
                        "• **Support Email**: support@jameschat.ht\n" +
                        "• **Administration & Security**: admin@jameschat.ht\n\n" +
                        "📝 You can also report violations directly via the 'Signaler 🚨' option."
                    else ->
                        "📞 **ASSISTANCE OFFICIELLE JAMES CHAT** :\n\n" +
                        "• **Email Support** : support@jameschat.ht\n" +
                        "• **Administration & Sécurité** : admin@jameschat.ht\n\n" +
                        "📝 Vous pouvez également utiliser le bouton 'Signaler 🚨' sur n'importe quel message ou profil suspect pour un examen immédiat."
                }
                BotResponseResult(replyText = text, suggestedQuestions = listOf("/rules", "/profile", "Comment obtenir le V Badge ?"))
            }

            "/games", "/jeux", "/jwet", "/jwèt", "jwèt", "jwet", "game", "games" -> {
                val gamesList = when (lang) {
                    AppLanguage.KREYOL -> listOf(
                        "🎮 **Devinèt James Bot** :\n« Mwen pèmèt ou koute vwa ak yon bèl ond sonò anime, chanje 50 lang diferan, epi chifre tout mesaj ou yo an sekirite. Ki moun mwen ye ? »\n👉 *Repons : Se James Chat !* 🇭🇹",
                        "🎯 **Defi Vitès & Idantite** :\nKi James Tag inik ou ye sou James Chat ? Ale nan pwofil ou, gade kòd 4 chif ou a (egz: JAMES#4827) epi pataje l ak zanmi w pou yo jwenn ou imedyatman !",
                        "🧠 **Kwiz Kominote** :\nKijan ou ka ajoute yon zanmi sou James Chat ?\n👉 *Repons : Avèk James Tag li, nom d'utilisateur li, kòd 4 chif li oswa scan QR Code li !*",
                        "🎲 **Mini-Jwèt Chif Sekrè** :\nChwazi yon chif ant 1 ak 10. Si w jwenn chif chans James Bot la (7), ou merite yon gwo bravo pou lespri w !",
                        "🧩 **Jwèt Kilti Kreyòl** :\n« Dèyè mòn gen... ? »\n👉 *Repons : Mòn ! Yon bèl pwovèb ayisyen ki montre kouraj ak pèseverans !* 💪"
                    )
                    AppLanguage.ENGLISH -> listOf(
                        "🎮 **James Bot Riddle**:\n\"I show animated sound waves when you speak, support 50 languages and protect your chats with end-to-end security. What am I?\"\n👉 *Answer: James Chat!*",
                        "🎯 **Tag Speed Challenge**:\nWhat is your unique 4-digit James Tag? Check your profile (e.g. JAMES#1234) and share your personal QR Code with friends!",
                        "🧠 **Security Trivia**:\nHow do you connect securely with friends on James Chat?\n👉 *Answer: Using their unique James Tag, 4-digit code, @username or QR Code!*",
                        "🎲 **Lucky Number Game**:\nPick a number between 1 and 10. Can you guess James Bot's lucky digit? (It's 7!)"
                    )
                    AppLanguage.ESPANOL -> listOf(
                        "🎮 **Adivinanza de James Bot**:\n«Muestro ondas de sonido animadas cuando hablas, tengo 50 idiomas y cifrado seguro. ¿Quién soy?»\n👉 *Respuesta: ¡James Chat!*",
                        "🎯 **Reto de Identidad**:\n¿Cuál es tu James Tag de 4 dígitos? Búscalo en tu perfil y comparte tu código QR.",
                        "🎲 **Juego del Número Secreto**:\nElige un número del 1 al 10. ¡El número de la suerte de James Bot es el 7!"
                    )
                    else -> listOf(
                        "🎮 **Devinette James Bot** :\n« Je permets d'écouter la voix avec une onde sonore animée en temps réel, de basculer entre 50 langues et de chiffrer les échanges. Qui suis-je ? »\n👉 *Réponse : James Chat !*",
                        "🎯 **Défi Vitesse & Identifiant** :\nQuel est votre identifiant Tag unique sur James Chat ? Allez dans votre profil et partagez votre code à 4 chiffres (ex: JAMES#4827) !",
                        "🧠 **Quiz Sécurité & Gouvernance** :\nComment ajouter un contact de manière sécurisée sur James Chat ?\n👉 *Réponse : Grâce au James Tag unique, au nom d'utilisateur, au code à 4 chiffres ou au scan de QR Code !*",
                        "🎲 **Mini-Jeu du Chiffre Mystère** :\nChoisissez un chiffre entre 1 et 10. Si vous trouvez le chiffre secret de James Bot (7), vous remportez la médaille de perspicacité !"
                    )
                }
                BotResponseResult(replyText = gamesList.random(), suggestedQuestions = listOf("/games", "/help", "/profile", "/rules"))
            }

            "/profile", "/profil" -> {
                val text = when (lang) {
                    AppLanguage.KREYOL ->
                        "🤖 **PWOFIL OFISYÈL JAMES BOT**\n\n" +
                        "• **Non** : James Bot\n" +
                        "• **Wòl** : Asistan Entelektyèl & Gid Ofisyèl James Chat\n" +
                        "• **Idantifyan** : @james_bot • JMS-0001\n" +
                        "• **Statut** : Aktif 24h/24, 7j/7\n" +
                        "• **Sekirite** : 100% Repons verifye pa Administrateur yo\n" +
                        "• **Lang sipòte** : Kreyòl Ayisyen, Français, English, Español"
                    else ->
                        "🤖 **PROFIL OFFICIEL DE JAMES BOT**\n\n" +
                        "• **Nom** : James Bot\n" +
                        "• **Rôle** : Assistant Intelligent & Guide Officiel James Chat\n" +
                        "• **Identifiant** : @james_bot • JMS-0001 • JAMES#0001\n" +
                        "• **Statut** : En ligne 24h/24, 7j/7 • Certifié Officiel\n" +
                        "• **Couleurs** : Bleu (#2563EB) & Violet (#7C3AED)\n" +
                        "• **Moteur** : Base de connaissances certifiée + Moteur déterministe local\n" +
                        "• **Sécurité** : Règles serveur strictes vérifiées par les administrateurs"
                }
                BotResponseResult(replyText = text, suggestedQuestions = listOf("/help", "/rules", "/support"))
            }

            "/reformuler", "/rewrite", "/reformule" -> {
                if (arg.isEmpty()) {
                    BotResponseResult(
                        replyText = "Veuillez fournir le texte à reformuler. Exemple : `/reformuler Je veux savoir quand commence la réunion`",
                        canCopy = false
                    )
                } else {
                    val formal = "✨ **Proposition Formelle & Professionnelle** :\n« Madame, Monsieur, je souhaiterais obtenir des précisions concernant : $arg. En vous remerciant d'avance. »\n\n💬 **Proposition Conviviale & Courtoise** :\n« Bonjour ! Je me permets de revenir vers vous à propos de : $arg. Merci beaucoup ! »"
                    BotResponseResult(replyText = formal, canCopy = true)
                }
            }

            "/corriger", "/correct", "/korije" -> {
                if (arg.isEmpty()) {
                    BotResponseResult(
                        replyText = "Veuillez saisir le texte à corriger. Exemple : `/corriger Salut cva bien`",
                        canCopy = false
                    )
                } else {
                    val corrected = cleanAndCorrectText(arg)
                    val text = "✍️ **Texte Corrigé & Soigné** :\n« $corrected »\n\n💡 *Astuce : Vous pouvez utiliser le bouton 'Copier la réponse' pour l'insérer directement dans votre discussion.*"
                    BotResponseResult(replyText = text, canCopy = true)
                }
            }

            else -> {
                BotResponseResult(
                    replyText = "Commande inconnue : `$cmd`. Tapez `/help` pour consulter les commandes valides.",
                    suggestedQuestions = listOf("/help", "/rules", "/support")
                )
            }
        }
    }

    private fun findBestMatchInKnowledge(
        normalizedQuery: String,
        knowledgeBase: List<BotKnowledgeEntity>,
        userLang: AppLanguage
    ): BotKnowledgeEntity? {
        val published = knowledgeBase.filter { it.isPublished }
        if (published.isEmpty()) return null

        val queryWords = normalizedQuery.split(" ", "-", "'").filter { it.length > 2 }.toSet()

        var bestScore = 0
        var bestItem: BotKnowledgeEntity? = null

        for (item in published) {
            var score = 0
            val normQ = normalizeText(item.question)

            // 1. Égalité exacte ou inclusion totale
            if (normQ == normalizedQuery) score += 100
            else if (normQ.contains(normalizedQuery) || normalizedQuery.contains(normQ)) score += 60

            // 2. Questions alternatives
            val alternatives = item.alternativeQuestions.split(",").map { normalizeText(it.trim()) }
            for (alt in alternatives) {
                if (alt.isNotEmpty()) {
                    if (alt == normalizedQuery) score += 95
                    else if (alt.contains(normalizedQuery) || normalizedQuery.contains(alt)) score += 55
                }
            }

            // 3. Mots-clés
            val keywords = item.keywords.split(",").map { normalizeText(it.trim()) }
            for (kw in keywords) {
                if (kw.isNotEmpty() && normalizedQuery.contains(kw)) {
                    score += 25
                }
            }

            // 4. Chevauchement de mots
            val itemWords = (normQ + " " + item.keywords).split(" ", "-", "'").filter { it.length > 2 }.toSet()
            val commonWords = queryWords.intersect(itemWords)
            score += commonWords.size * 10

            // 5. Bonus de langue concordante
            if (item.language.equals(userLang.code, ignoreCase = true)) {
                score += 5
            }

            if (score > bestScore && score >= 20) {
                bestScore = score
                bestItem = item
            }
        }

        return bestItem
    }

    private fun isTextAssistanceRequest(query: String): Boolean {
        val triggers = listOf("reformule", "corrige", "redige", "rediger", "ecris", "traduis", "fais moi un message", "propose une reponse")
        return triggers.any { query.contains(it) }
    }

    private fun handleTextAssistance(rawInput: String, lang: AppLanguage): BotResponseResult {
        val reply = "✍️ **Assistance Rédactionnelle James Bot** :\n\n" +
                "Voici des modèles prêts à l'emploi selon votre besoin :\n\n" +
                "1. **Professionnel & Courtois** :\n« Bonjour, je me permets de vous contacter afin de faire le point. Restant à votre disposition pour tout échange. »\n\n" +
                "2. **Amical & Chaleureux** :\n« Salut ! J'espère que tu vas bien. Fais-moi signe dès que tu es disponible pour discuter ! »\n\n" +
                "3. **Confirmation rapide** :\n« Bien reçu, merci beaucoup ! Je reviens vers vous dès que possible. »"
        return BotResponseResult(replyText = reply, canCopy = true, suggestedQuestions = listOf("/help", "Comment utiliser les messages vocaux ?"))
    }

    private fun cleanAndCorrectText(input: String): String {
        var res = input.trim()
        val commonFixes = mapOf(
            "slt" to "salut",
            "cva" to "ça va",
            "ca va" to "ça va",
            "bjr" to "bonjour",
            "bsr" to "bonsoir",
            "stpj" to "s'il te plaît",
            "stp" to "s'il te plaît",
            "svp" to "s'il vous plaît",
            "pkoi" to "pourquoi",
            "pk" to "pourquoi",
            "nn" to "non",
            "ui" to "oui",
            "bcp" to "beaucoup",
            "mrci" to "merci",
            "mrc" to "merci",
            "daccord" to "d'accord",
            "ki" to "qui",
            "koi" to "quoi"
        )
        val tokens = res.split(" ")
        val correctedTokens = tokens.map { token ->
            val clean = token.lowercase(Locale.ROOT).replace(Regex("[^a-zA-Z]"), "")
            commonFixes[clean] ?: token
        }
        res = correctedTokens.joinToString(" ")
        if (res.isNotEmpty()) {
            res = res.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            if (!res.endsWith(".") && !res.endsWith("!") && !res.endsWith("?")) {
                res += "."
            }
        }
        return res
    }

    private fun fallbackResponse(lang: AppLanguage): BotResponseResult {
        val text = when (lang) {
            AppLanguage.KREYOL ->
                "🤖 Padon, mwen pa gen yon repons ofisyèl ki valide pou kesyon sa a kounye a.\n\n" +
                "Pou sekirite w, mwen pa janm envante enfòmasyon ki pa verifye. Ou ka kontakte administratè nou yo dirèkteman via `/support` oswa voye yon imèl bay `support@jameschat.ht`."
            AppLanguage.ENGLISH ->
                "🤖 I don't have a verified answer for this question yet.\n\n" +
                "For your safety and accuracy, James Bot never fabricates unverified features. Please contact our official support team using `/support` or email `support@jameschat.ht`."
            AppLanguage.ESPANOL ->
                "🤖 No dispongo de una respuesta verificada para esta consulta en este momento.\n\n" +
                "Por seguridad y precisión, nunca invento respuestas. Puedes contactar al soporte oficial mediante `/support` o escribiendo a `support@jameschat.ht`."
            else ->
                "🤖 La réponse à cette question n'est pas encore disponible dans la base de connaissances certifiée.\n\n" +
                "Par souci de fiabilité, James Bot n'invente jamais d'informations non vérifiées. Vous pouvez contacter notre assistance officielle via la commande `/support` ou écrire à `support@jameschat.ht`."
        }
        return BotResponseResult(
            replyText = text,
            canCopy = false,
            suggestedQuestions = listOf("/help", "/rules", "/support", "Comment obtenir le V Badge ?")
        )
    }

    fun getGreeting(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.KREYOL ->
                "👋 Bonjou ! Mwen se **James Bot**, asistan entèlijan ofisyèl James Chat.\n\nKijan mwen ka ede w jodi a ? Ou ka poze m kesyon sou aplikasyon an oswa tape `/help` pou wè tout kòmand yo."
            AppLanguage.ENGLISH ->
                "👋 Hello! I am **James Bot**, the official intelligent assistant for James Chat.\n\nHow can I help you today? Ask any question about features or type `/help` to see commands."
            AppLanguage.ESPANOL ->
                "👋 ¡Hola! Soy **James Bot**, el asistente inteligente oficial de James Chat.\n\n¿En qué puedo ayudarte hoy? Pregúntame sobre cualquier función o escribe `/help`."
            else ->
                "👋 Bonjour ! Je suis **James Bot**, l'assistant intelligent officiel de James Chat.\n\nComment puis-je vous aider aujourd'hui ? Posez-moi une question sur le fonctionnement ou tapez `/help` pour découvrir les commandes."
        }
    }

    fun getQuickSuggestions(lang: AppLanguage): List<String> {
        return when (lang) {
            AppLanguage.KREYOL -> listOf(
                "/games",
                "/help",
                "/rules",
                "Kijan pou m jwenn V Badge ?",
                "Kijan pou m anrejistre yon vokal ?",
                "/support"
            )
            AppLanguage.ENGLISH -> listOf(
                "/games",
                "/help",
                "/rules",
                "How to get verified V Badge?",
                "How to record voice message?",
                "/support"
            )
            else -> listOf(
                "/games",
                "/help",
                "/rules",
                "Comment obtenir le V Badge ?",
                "Comment écouter un message vocal ?",
                "/support"
            )
        }
    }

    private fun getRelatedSuggestions(category: String, lang: AppLanguage): List<String> {
        return listOf("/help", "/rules", "/support")
    }

    private fun normalizeText(input: String): String {
        val withoutAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return withoutAccents.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Données par défaut pour initialiser la base de connaissances lors du premier lancement.
     */
    fun getDefaultKnowledgeBase(): List<BotKnowledgeEntity> {
        val now = System.currentTimeMillis()
        val admin = "admin@jameschat.ht"
        return listOf(
            BotKnowledgeEntity(
                id = "kb_v_badge",
                category = "Badges & Vérification",
                question = "Comment obtenir le V Badge de vérification ?",
                alternativeQuestions = "Comment être certifié, Avoir le badge bleu, Devenir vérifié, Badge V officiel, Certification James Chat",
                keywords = "badge, vérifié, certification, v badge, officiel, certifier",
                answer = "Le V Badge officiel de James Chat est attribué par l'équipe de modération officielle et la gouvernance James Chat. Pour en faire la demande, votre profil doit être complet (photo, nom, bio) et respecter rigoureusement le règlement de la communauté. Contactez l'assistance via `/support`.",
                language = "fr",
                isPublished = true,
                createdByAdminEmail = admin,
                createdAt = now,
                updatedAt = now
            ),
            BotKnowledgeEntity(
                id = "kb_voice_waveform",
                category = "Messages Vocaux",
                question = "Comment fonctionne l'onde sonore animée des messages vocaux ?",
                alternativeQuestions = "Message vocal, Onde sonore, Écouter audio, Waveform WhatsApp, Barre de lecture audio",
                keywords = "vocal, audio, onde, sonore, waveform, micro, lecture, écouter, play",
                answer = "Lorsque vous écoutez un message vocal sur James Chat et appuyez sur ▶️, une onde sonore animée WhatsApp s'affiche en temps réel. Les barres oscillent avec la musique de votre voix, la partie écoutée change de couleur pour indiquer la progression, et vous pouvez faire glisser le curseur n'importe où. Un seul message vocal ne joue à la fois pour un confort optimal !",
                language = "fr",
                isPublished = true,
                createdByAdminEmail = admin,
                createdAt = now,
                updatedAt = now
            ),
            BotKnowledgeEntity(
                id = "kb_50_languages",
                category = "International",
                question = "Quelles sont les langues disponibles sur James Chat ?",
                alternativeQuestions = "Changer de langue, 50 langues, Traduction, Kreyòl Ayisyen, Espagnol, Anglais",
                keywords = "langue, tradui, traduction, 50 langues, kreyol, francais, anglais, spanish",
                answer = "James Chat propose 50 langues internationales, dont le Kreyòl Ayisyen, le Français, l'Anglais, l'Espagnol, le Portugais, le Chinois, l'Arabe, l'Hindi et bien d'autres. Vous pouvez changer la langue à tout moment depuis le menu du drapeau en haut ou dans Paramètres > Langue de l'application. Un moteur de traduction local gratuit est également intégré !",
                language = "fr",
                isPublished = true,
                createdByAdminEmail = admin,
                createdAt = now,
                updatedAt = now
            ),
            BotKnowledgeEntity(
                id = "kb_contact_identifiers",
                category = "Contacts & ID",
                question = "Comment ajouter un contact avec son identifiant ou QR Code ?",
                alternativeQuestions = "Ajouter ami, Trouver quelqu'un, Tag James, Code à 4 chiffres, JMS ID, Scanner QR",
                keywords = "contact, ajouter, id, tag, 4 chiffres, qrcode, jms, username",
                answer = "James Chat propose 5 méthodes uniques pour trouver un contact :\n1. Le James Tag universel (ex: JAMES#4827)\n2. Le nom d'utilisateur @username\n3. L'identifiant James Custom ID (ex: JMS-4827)\n4. Le code personnel à 4 chiffres (ex: 4827)\n5. Le scan instantané de QR Code dans l'onglet Contacts.",
                language = "fr",
                isPublished = true,
                createdByAdminEmail = admin,
                createdAt = now,
                updatedAt = now
            ),
            BotKnowledgeEntity(
                id = "kb_ephemeral",
                category = "Confidentialité",
                question = "Comment activer les messages éphémères ?",
                alternativeQuestions = "Messages qui disparaissent, Autodestruction, Minuteur message, Disappearing messages",
                keywords = "éphémère, disparaitre, minuteur, 24h, 7 jours, secret, confidentiel",
                answer = "Dans n'importe quelle discussion, touchez le menu des options (les 3 points en haut à droite) puis sélectionnez 'Messages éphémères'. Vous pouvez choisir entre 1 heure, 24 heures ou 7 jours. Les nouveaux messages envoyés disparaîtront automatiquement une fois le délai écoulé.",
                language = "fr",
                isPublished = true,
                createdByAdminEmail = admin,
                createdAt = now,
                updatedAt = now
            ),
            BotKnowledgeEntity(
                id = "kb_admin_roles",
                category = "Administration",
                question = "Qui sont les propriétaires et administrateurs de James Chat ?",
                alternativeQuestions = "Qui dirige James Chat, Fondateur, Propriétaire, Admins officiels, Staff James Chat",
                keywords = "proprietaire, owner, admin, support, direction",
                answer = "James Chat est sous l'autorité souveraine de son équipe de direction (admin@jameschat.ht et support@jameschat.ht). Toutes les opérations administratives sont strictement auditées et protégées côté serveur.",
                language = "fr",
                isPublished = true,
                createdByAdminEmail = admin,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
