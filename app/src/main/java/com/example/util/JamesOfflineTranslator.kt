package com.example.util

/**
 * Motè tradiksyon lokal ak gratis pou James Chat (100% offline, zero API peye).
 * Tradui mesaj tèks nan konvèsasyon yo san koute lajan, san voye done sou sèvè deyò.
 */
object JamesOfflineTranslator {

    data class TranslationResult(
        val originalText: String,
        val translatedText: String,
        val sourceLanguage: String,
        val targetLanguage: String,
        val isOfflineEngine: Boolean = true
    )

    // Diksyonè fraz ak mo kle ki pi komen nan diskisyon mesajri (chat, salitasyon, emosyon, keksyon, travay, lavi chak jou)
    private val phraseDictionary: Map<String, Map<String, String>> = mapOf(
        "bonjour" to mapOf(
            "fr" to "Bonjour", "en" to "Hello", "ht" to "Bonjou", "es" to "Hola",
            "pt" to "Olá", "de" to "Guten Tag", "it" to "Buongiorno", "ru" to "Здравствуйте",
            "zh" to "你好", "ja" to "こんにちは", "ko" to "안녕하세요", "ar" to "مرحبا", "hi" to "नमस्ते"
        ),
        "hello" to mapOf(
            "fr" to "Bonjour", "en" to "Hello", "ht" to "Bonjou", "es" to "Hola",
            "pt" to "Olá", "de" to "Hallo", "it" to "Ciao", "ru" to "Привет",
            "zh" to "你好", "ja" to "こんにちは", "ko" to "안녕하세요", "ar" to "مرحبا", "hi" to "नमस्ते"
        ),
        "bonjou" to mapOf(
            "fr" to "Bonjour", "en" to "Hello", "ht" to "Bonjou", "es" to "Hola",
            "pt" to "Olá", "de" to "Hallo", "it" to "Ciao", "ru" to "Привет",
            "zh" to "你好", "ja" to "こんにちは", "ko" to "안녕하세요", "ar" to "مرحبا", "hi" to "नमस्ते"
        ),
        "bonswa" to mapOf(
            "fr" to "Bonsoir", "en" to "Good evening", "ht" to "Bonswa", "es" to "Buenas noches",
            "pt" to "Boa noite", "de" to "Guten Abend", "it" to "Buonasera", "ru" to "Добрый вечер",
            "zh" to "晚上好", "ja" to "こんばんは", "ko" to "좋은 저녁입니다", "ar" to "مساء الخير"
        ),
        "bonsoir" to mapOf(
            "fr" to "Bonsoir", "en" to "Good evening", "ht" to "Bonswa", "es" to "Buenas noches",
            "pt" to "Boa noite", "de" to "Guten Abend", "it" to "Buonasera", "ru" to "Добрый вечер",
            "zh" to "晚上好", "ja" to "こんばんは", "ko" to "좋은 저녁입니다", "ar" to "مساء الخير"
        ),
        "kijan ou ye" to mapOf(
            "fr" to "Comment vas-tu ?", "en" to "How are you?", "ht" to "Kijan ou ye ?", "es" to "¿Cómo estás?",
            "pt" to "Como você está?", "de" to "Wie geht es dir?", "it" to "Come stai?", "ru" to "Как дела?",
            "zh" to "你好吗？", "ja" to "お元気ですか？", "ko" to "어떻게 지내세요?", "ar" to "كيف حالك؟"
        ),
        "comment vas-tu" to mapOf(
            "fr" to "Comment vas-tu ?", "en" to "How are you?", "ht" to "Kijan ou ye ?", "es" to "¿Cómo estás?",
            "pt" to "Como você está?", "de" to "Wie geht es dir?", "it" to "Come stai?", "ru" to "Как дела?",
            "zh" to "你好吗？", "ja" to "お元気ですか？", "ko" to "어떻게 지내세요?", "ar" to "كيف حالك؟"
        ),
        "how are you" to mapOf(
            "fr" to "Comment vas-tu ?", "en" to "How are you?", "ht" to "Kijan ou ye ?", "es" to "¿Cómo estás?",
            "pt" to "Como você está?", "de" to "Wie geht es dir?", "it" to "Come stai?", "ru" to "Как дела?",
            "zh" to "你好吗？", "ja" to "お元気ですか？", "ko" to "어떻게 지내세요?", "ar" to "كيف حالك؟"
        ),
        "mwen byen" to mapOf(
            "fr" to "Je vais bien", "en" to "I am doing well", "ht" to "Mwen byen", "es" to "Estoy bien",
            "pt" to "Estou bem", "de" to "Mir geht es gut", "it" to "Sto bene", "ru" to "Я в порядке",
            "zh" to "我很好", "ja" to "元気です", "ko" to "잘 지내고 있습니다", "ar" to "أنا بخير"
        ),
        "je vais bien" to mapOf(
            "fr" to "Je vais bien", "en" to "I am doing well", "ht" to "Mwen byen", "es" to "Estoy bien",
            "pt" to "Estou bem", "de" to "Mir geht es gut", "it" to "Sto bene", "ru" to "Я в порядке",
            "zh" to "我很好", "ja" to "元気です", "ko" to "잘 지내고 있습니다", "ar" to "أنا بخير"
        ),
        "i am good" to mapOf(
            "fr" to "Je vais bien", "en" to "I am good", "ht" to "Mwen byen", "es" to "Estoy bien",
            "pt" to "Estou bem", "de" to "Mir geht es gut", "it" to "Sto bene", "ru" to "Я в порядке",
            "zh" to "我很好", "ja" to "元気です", "ko" to "잘 지내고 있습니다", "ar" to "أنا بخير"
        ),
        "merci" to mapOf(
            "fr" to "Merci", "en" to "Thank you", "ht" to "Mèsi", "es" to "Gracias",
            "pt" to "Obrigado", "de" to "Danke", "it" to "Grazie", "ru" to "Спасибо",
            "zh" to "谢谢", "ja" to "ありがとう", "ko" to "감사합니다", "ar" to "شكراً", "hi" to "धन्यवाद"
        ),
        "mesi" to mapOf(
            "fr" to "Merci", "en" to "Thank you", "ht" to "Mèsi", "es" to "Gracias",
            "pt" to "Obrigado", "de" to "Danke", "it" to "Grazie", "ru" to "Спасибо",
            "zh" to "谢谢", "ja" to "ありがとう", "ko" to "감사합니다", "ar" to "شكراً", "hi" to "धन्यवाद"
        ),
        "thank you" to mapOf(
            "fr" to "Merci", "en" to "Thank you", "ht" to "Mèsi", "es" to "Gracias",
            "pt" to "Obrigado", "de" to "Danke", "it" to "Grazie", "ru" to "Спасибо",
            "zh" to "谢谢", "ja" to "ありがとう", "ko" to "감사합니다", "ar" to "شكراً", "hi" to "धन्यवाद"
        ),
        "de rien" to mapOf(
            "fr" to "De rien", "en" to "You're welcome", "ht" to "Pa gen pwoblèm", "es" to "De nada",
            "pt" to "De nada", "de" to "Bitte schön", "it" to "Prego", "ru" to "Пожалуйста",
            "zh" to "不客气", "ja" to "どういたしまして", "ko" to "천만에요", "ar" to "عفواً"
        ),
        "you are welcome" to mapOf(
            "fr" to "De rien", "en" to "You're welcome", "ht" to "Pa gen pwoblèm", "es" to "De nada",
            "pt" to "De nada", "de" to "Bitte schön", "it" to "Prego", "ru" to "Пожалуйста",
            "zh" to "不客气", "ja" to "どういたしまして", "ko" to "천만에요", "ar" to "عفواً"
        ),
        "oui" to mapOf(
            "fr" to "Oui", "en" to "Yes", "ht" to "Wi", "es" to "Sí",
            "pt" to "Sim", "de" to "Ja", "it" to "Sì", "ru" to "Да",
            "zh" to "是的", "ja" to "はい", "ko" to "네", "ar" to "نعم"
        ),
        "non" to mapOf(
            "fr" to "Non", "en" to "No", "ht" to "Non", "es" to "No",
            "pt" to "Não", "de" to "Nein", "it" to "No", "ru" to "Нет",
            "zh" to "不", "ja" to "いいえ", "ko" to "아니요", "ar" to "لا"
        ),
        "wi" to mapOf(
            "fr" to "Oui", "en" to "Yes", "ht" to "Wi", "es" to "Sí",
            "pt" to "Sim", "de" to "Ja", "it" to "Sì", "ru" to "Да",
            "zh" to "是的", "ja" to "はい", "ko" to "네", "ar" to "نعم"
        ),
        "a bientot" to mapOf(
            "fr" to "À bientôt", "en" to "See you soon", "ht" to "N a wè talè", "es" to "Hasta pronto",
            "pt" to "Até breve", "de" to "Bis bald", "it" to "A presto", "ru" to "До скорого",
            "zh" to "回头见", "ja" to "またね", "ko" to "곧 봐요", "ar" to "أراك قريباً"
        ),
        "au revoir" to mapOf(
            "fr" to "Au revoir", "en" to "Goodbye", "ht" to "Orevwa", "es" to "Adiós",
            "pt" to "Adeus", "de" to "Auf Wiedersehen", "it" to "Arrivederci", "ru" to "До свидания",
            "zh" to "再见", "ja" to "さようなら", "ko" to "안녕히 계세요", "ar" to "مع السلامة"
        ),
        "d'accord" to mapOf(
            "fr" to "D'accord", "en" to "Okay / Agreed", "ht" to "Dakò", "es" to "De acuerdo",
            "pt" to "De acordo", "de" to "Einverstanden", "it" to "D'accordo", "ru" to "Хорошо / Согласен",
            "zh" to "好的", "ja" to "了解しました", "ko" to "알겠습니다", "ar" to "حسناً"
        ),
        "dako" to mapOf(
            "fr" to "D'accord", "en" to "Okay / Agreed", "ht" to "Dakò", "es" to "De acuerdo",
            "pt" to "De acordo", "de" to "Einverstanden", "it" to "D'accordo", "ru" to "Хорошо / Согласен",
            "zh" to "好的", "ja" to "了解しました", "ko" to "알겠습니다", "ar" to "حسناً"
        ),
        "sa fe lontan" to mapOf(
            "fr" to "Ça fait longtemps !", "en" to "Long time no see!", "ht" to "Sa fè lontan !", "es" to "¡Cuánto tiempo!",
            "pt" to "Quanto tempo!", "de" to "Lange nicht gesehen!", "it" to "Da quanto tempo!", "ru" to "Давно не виделись!",
            "zh" to "好久不见！", "ja" to "久しぶり！", "ko" to "오랜만이에요!", "ar" to "لم نلتقِ منذ مدة!"
        ),
        "je t'aime" to mapOf(
            "fr" to "Je t'aime", "en" to "I love you", "ht" to "Mwen renmen w", "es" to "Te quiero",
            "pt" to "Eu te amo", "de" to "Ich liebe dich", "it" to "Ti amo", "ru" to "Я люблю тебя",
            "zh" to "我爱你", "ja" to "愛してる", "ko" to "사랑해요", "ar" to "أحبك"
        ),
        "mwen renmen w" to mapOf(
            "fr" to "Je t'aime", "en" to "I love you", "ht" to "Mwen renmen w", "es" to "Te quiero",
            "pt" to "Eu te amo", "de" to "Ich liebe dich", "it" to "Ti amo", "ru" to "Я люблю тебя",
            "zh" to "我爱你", "ja" to "愛してる", "ko" to "사랑해요", "ar" to "أحبك"
        ),
        "bonne nuit" to mapOf(
            "fr" to "Bonne nuit", "en" to "Good night", "ht" to "Bòn nwit", "es" to "Buenas noches",
            "pt" to "Boa noite", "de" to "Gute Nacht", "it" to "Buonanotte", "ru" to "Спокойной ночи",
            "zh" to "晚安", "ja" to "おやすみなさい", "ko" to "안녕히 주무세요", "ar" to "تصبح على خير"
        ),
        "bon courage" to mapOf(
            "fr" to "Bon courage", "en" to "Good luck / Keep it up", "ht" to "Bon kouraj", "es" to "Mucho ánimo",
            "pt" to "Bom trabalho", "de" to "Viel Erfolg", "it" to "Buona fortuna", "ru" to "Удачи",
            "zh" to "加油", "ja" to "頑張って", "ko" to "힘내세요", "ar" to "بالتوفيق"
        )
    )

    // Diksyonè mo pa mo pou ranplasman entèlijan
    private val wordMap: Map<String, Map<String, String>> = mapOf(
        "wi" to mapOf("fr" to "oui", "en" to "yes", "es" to "sí", "ht" to "wi", "pt" to "sim"),
        "non" to mapOf("fr" to "non", "en" to "no", "es" to "no", "ht" to "non", "pt" to "não"),
        "mwen" to mapOf("fr" to "moi / je", "en" to "I / me", "es" to "yo", "ht" to "mwen", "pt" to "eu"),
        "ou" to mapOf("fr" to "toi / tu", "en" to "you", "es" to "tú", "ht" to "ou", "pt" to "você"),
        "li" to mapOf("fr" to "il / elle", "en" to "he / she", "es" to "él / ella", "ht" to "li", "pt" to "ele / ela"),
        "nou" to mapOf("fr" to "nous", "en" to "we / us", "es" to "nosotros", "ht" to "nou", "pt" to "nós"),
        "yo" to mapOf("fr" to "ils / eux", "en" to "they", "es" to "ellos", "ht" to "yo", "pt" to "eles"),
        "bon" to mapOf("fr" to "bon", "en" to "good", "es" to "bueno", "ht" to "bon", "pt" to "bom"),
        "byen" to mapOf("fr" to "bien", "en" to "well", "es" to "bien", "ht" to "byen", "pt" to "bem"),
        "mesi" to mapOf("fr" to "merci", "en" to "thanks", "es" to "gracias", "ht" to "mèsi", "pt" to "obrigado"),
        "mèsi" to mapOf("fr" to "merci", "en" to "thanks", "es" to "gracias", "ht" to "mèsi", "pt" to "obrigado"),
        "zanmi" to mapOf("fr" to "ami", "en" to "friend", "es" to "amigo", "ht" to "zanmi", "pt" to "amigo"),
        "travay" to mapOf("fr" to "travail", "en" to "work", "es" to "trabajo", "ht" to "travay", "pt" to "trabalho"),
        "kay" to mapOf("fr" to "maison", "en" to "home", "es" to "casa", "ht" to "kay", "pt" to "casa"),
        "jodi" to mapOf("fr" to "aujourd'hui", "en" to "today", "es" to "hoy", "ht" to "jodi a", "pt" to "hoje"),
        "demen" to mapOf("fr" to "demain", "en" to "tomorrow", "es" to "mañana", "ht" to "demen", "pt" to "amanhã"),
        "yes" to mapOf("fr" to "oui", "en" to "yes", "es" to "sí", "ht" to "wi", "pt" to "sim"),
        "no" to mapOf("fr" to "non", "en" to "no", "es" to "no", "ht" to "non", "pt" to "não"),
        "thanks" to mapOf("fr" to "merci", "en" to "thanks", "es" to "gracias", "ht" to "mèsi", "pt" to "obrigado"),
        "friend" to mapOf("fr" to "ami", "en" to "friend", "es" to "amigo", "ht" to "zanmi", "pt" to "amigo"),
        "ami" to mapOf("fr" to "ami", "en" to "friend", "es" to "amigo", "ht" to "zanmi", "pt" to "amigo"),
        "aujourd'hui" to mapOf("fr" to "aujourd'hui", "en" to "today", "es" to "hoy", "ht" to "jodi a", "pt" to "hoje")
    )

    fun translate(text: String, targetLangCode: String): TranslationResult {
        val clean = text.trim()
        if (clean.isEmpty()) {
            return TranslationResult(text, text, "auto", targetLangCode)
        }

        val normalized = clean.lowercase()
            .replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .trim()

        // 1. Tcheke si se yon fraz konplè nan diksyonè a
        phraseDictionary[normalized]?.let { transMap ->
            val trans = transMap[targetLangCode] ?: transMap["en"] ?: transMap["fr"]
            if (trans != null) {
                // Kenbe pwen eksklamasyon oswa keksyon orijinal
                val suffix = when {
                    clean.endsWith("?") -> " ?"
                    clean.endsWith("!") -> " !"
                    clean.endsWith(".") -> "."
                    else -> ""
                }
                return TranslationResult(
                    originalText = text,
                    translatedText = trans + suffix,
                    sourceLanguage = "auto",
                    targetLanguage = targetLangCode
                )
            }
        }

        // 2. Chèche pati nan fraz la (sub-phrase matching)
        for ((phraseKey, transMap) in phraseDictionary) {
            if (normalized.contains(phraseKey)) {
                val replacement = transMap[targetLangCode] ?: transMap["en"] ?: transMap["fr"]
                if (replacement != null) {
                    val translated = clean.replace(Regex("(?i)\\b$phraseKey\\b"), replacement)
                    if (translated != clean) {
                        return TranslationResult(
                            originalText = text,
                            translatedText = translated,
                            sourceLanguage = "auto",
                            targetLanguage = targetLangCode
                        )
                    }
                }
            }
        }

        // 3. Tradiksyon mo pa mo pou mo kle li rekonèt
        val words = clean.split(Regex("\\s+"))
        var changed = false
        val translatedWords = words.map { w ->
            val punct = w.filter { !it.isLetterOrDigit() }
            val baseWord = w.filter { it.isLetterOrDigit() }.lowercase()
            val match = wordMap[baseWord]?.get(targetLangCode)
            if (match != null) {
                changed = true
                if (w.firstOrNull()?.isUpperCase() == true) match.replaceFirstChar { it.uppercase() } + punct
                else match + punct
            } else {
                w
            }
        }

        val resultString = if (changed) translatedWords.joinToString(" ") else clean

        return TranslationResult(
            originalText = text,
            translatedText = resultString,
            sourceLanguage = "auto",
            targetLanguage = targetLangCode
        )
    }
}
