package com.example.util

data class JamesCountry(
    val code: String,
    val name: String,
    val nativeName: String,
    val dialCode: String,
    val flag: String
)

object JamesCountryUtil {
    val countries: List<JamesCountry> = listOf(
        JamesCountry("HT", "Haïti", "Ayiti", "+509", "🇭🇹"),
        JamesCountry("FR", "France", "France", "+33", "🇫🇷"),
        JamesCountry("US", "États-Unis", "United States", "+1", "🇺🇸"),
        JamesCountry("CA", "Canada", "Canada", "+1", "🇨🇦"),
        JamesCountry("DO", "République Dominicaine", "República Dominicana", "+1-809", "🇩🇴"),
        JamesCountry("ES", "Espagne", "España", "+34", "🇪🇸"),
        JamesCountry("BR", "Brésil", "Brasil", "+55", "🇧🇷"),
        JamesCountry("GB", "Royaume-Uni", "United Kingdom", "+44", "🇬🇧"),
        JamesCountry("DE", "Allemagne", "Deutschland", "+49", "🇩🇪"),
        JamesCountry("IT", "Italie", "Italia", "+39", "🇮🇹"),
        JamesCountry("PT", "Portugal", "Portugal", "+351", "🇵🇹"),
        JamesCountry("MX", "Mexique", "México", "+52", "🇲🇽"),
        JamesCountry("CO", "Colombie", "Colombia", "+57", "🇨🇴"),
        JamesCountry("AR", "Argentine", "Argentina", "+54", "🇦🇷"),
        JamesCountry("CL", "Chili", "Chile", "+56", "🇨🇱"),
        JamesCountry("PE", "Pérou", "Perú", "+51", "🇵🇪"),
        JamesCountry("BE", "Belgique", "België", "+32", "🇧🇪"),
        JamesCountry("CH", "Suisse", "Schweiz", "+41", "🇨🇭"),
        JamesCountry("SN", "Sénégal", "Sénégal", "+221", "🇸🇳"),
        JamesCountry("CI", "Côte d'Ivoire", "Côte d'Ivoire", "+225", "🇨🇮"),
        JamesCountry("CM", "Cameroun", "Cameroun", "+237", "🇨🇲"),
        JamesCountry("CD", "RD Congo", "RD Congo", "+243", "🇨🇩"),
        JamesCountry("NG", "Nigeria", "Nigeria", "+234", "🇳🇬"),
        JamesCountry("GH", "Ghana", "Ghana", "+233", "🇬🇭"),
        JamesCountry("KE", "Kenya", "Kenya", "+254", "🇰🇪"),
        JamesCountry("ZA", "Afrique du Sud", "South Africa", "+27", "🇿🇦"),
        JamesCountry("MA", "Maroc", "المغرب", "+212", "🇲🇦"),
        JamesCountry("DZ", "Algérie", "الجزائر", "+213", "🇩🇿"),
        JamesCountry("TN", "Tunisie", "تونس", "+216", "🇹🇳"),
        JamesCountry("EG", "Égypte", "مصر", "+20", "🇪🇬"),
        JamesCountry("SA", "Arabie Saoudite", "السعودية", "+966", "🇸🇦"),
        JamesCountry("AE", "Émirats Arabes Unis", "الإمارات", "+971", "🇦🇪"),
        JamesCountry("CN", "Chine", "中国", "+86", "🇨🇳"),
        JamesCountry("JP", "Japon", "日本", "+81", "🇯🇵"),
        JamesCountry("KR", "Corée du Sud", "대한민국", "+82", "🇰🇷"),
        JamesCountry("IN", "Inde", "भारत", "+91", "🇮🇳"),
        JamesCountry("PK", "Pakistan", "پاکستان", "+92", "🇵🇰"),
        JamesCountry("BD", "Bangladesh", "বাংলাদেশ", "+880", "🇧🇩"),
        JamesCountry("ID", "Indonésie", "Indonesia", "+62", "🇮🇩"),
        JamesCountry("PH", "Philippines", "Pilipinas", "+63", "🇵🇭"),
        JamesCountry("VN", "Vietnam", "Việt Nam", "+84", "🇻🇳"),
        JamesCountry("TH", "Thaïlande", "ประเทศไทย", "+66", "🇹🇭"),
        JamesCountry("TR", "Turquie", "Türkiye", "+90", "🇹🇷"),
        JamesCountry("RU", "Russie", "Россия", "+7", "🇷🇺"),
        JamesCountry("UA", "Ukraine", "Україна", "+380", "🇺🇦"),
        JamesCountry("PL", "Pologne", "Polska", "+48", "🇵🇱"),
        JamesCountry("NL", "Pays-Bas", "Nederland", "+31", "🇳🇱"),
        JamesCountry("SE", "Suède", "Sverige", "+46", "🇸🇪"),
        JamesCountry("NO", "Norvège", "Norge", "+47", "🇳🇴"),
        JamesCountry("GR", "Grèce", "Ελλάδα", "+30", "🇬🇷")
    )

    fun findByCode(code: String): JamesCountry {
        return countries.find { it.code.equals(code, ignoreCase = true) }
            ?: countries.first()
    }

    fun findByDialCode(dial: String): JamesCountry? {
        return countries.find { it.dialCode == dial }
    }
}
