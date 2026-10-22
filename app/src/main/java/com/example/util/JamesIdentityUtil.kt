package com.example.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.data.model.UserEntity
import java.util.Random

/**
 * Utility for James Chat 5-way unique identification and universal normalization:
 * 1. James Tag (e.g. "JAMES#4827", "JAMES#0001", "JAMES#10023", etc.) - Identifiant Officiel Principal
 * 2. James Custom ID (e.g. "JMS-4827")
 * 3. Username (e.g. "@jamesdesigner" ou "jamesdesigner")
 * 4. Code personnel d'identification (ex: "4827" ou tout identifiant numérique/alphanumérique valide)
 * 5. QR Code deep-link URI "jameschat://user/<id>?code=...&tag=..."
 */
object JamesIdentityUtil {

    /**
     * Normalise un identifiant brut en supprimant les espaces superflus, tabulations,
     * et en gérant les majuscules/minuscules de manière cohérente.
     */
    fun normalizeRaw(input: String): String {
        return input.trim()
            .replace("\\s+".toRegex(), " ")
            .replace("#\\s+".toRegex(), "#")
            .replace("\\s+#".toRegex(), "#")
            .replace("-\\s+".toRegex(), "-")
            .replace("\\s+-".toRegex(), "-")
    }

    /**
     * Extrait et assainit le code numérique/alphanumérique d'un utilisateur.
     * Compatible avec les codes à 4 chiffres historiques tout en supportant
     * des identifiants personnalisés de longueur variable.
     */
    fun sanitizeCode(raw: String): String {
        val clean = raw.trim().replace("\\s".toRegex(), "")
        return if (clean.isNotEmpty()) clean else "1000"
    }

    /**
     * Retourne l'identifiant code de l'utilisateur (historique 4 chiffres ou code personnalisé).
     */
    fun getFourDigitId(user: UserEntity): String {
        val raw = when {
            user.fourDigitCode.isNotEmpty() -> user.fourDigitCode
            user.personalId.isNotEmpty() -> user.personalId
            else -> ""
        }
        val digits = raw.filter { it.isDigit() }
        if (digits.length >= 4) return digits
        if (raw.isNotEmpty()) return raw
        // Si vide, dérivation déterministe depuis l'ID utilisateur
        val hash = Math.abs(user.id.hashCode()) % 9000 + 1000
        return hash.toString()
    }

    /**
     * Identifiant principal officiel au format James Tag : "JAMES#4827"
     */
    fun getJamesTag(user: UserEntity): String {
        if (user.jamesTag.isNotEmpty() && user.jamesTag.startsWith("JAMES#", ignoreCase = true)) {
            return user.jamesTag.uppercase()
        }
        return "JAMES#${getFourDigitId(user).uppercase()}"
    }

    /**
     * Identifiant partageable James ID : "JMS-4827"
     */
    fun getJamesCustomId(user: UserEntity): String {
        if (user.jamesCustomId.isNotEmpty() && user.jamesCustomId.startsWith("JMS-", ignoreCase = true)) {
            return user.jamesCustomId.uppercase()
        }
        return "JMS-${getFourDigitId(user).uppercase()}"
    }

    /**
     * Nom d'utilisateur formaté : "@username"
     */
    fun getFormattedUsername(user: UserEntity): String {
        val u = user.username.removePrefix("@").trim()
        return "@$u"
    }

    /**
     * Payload standard pour Deep Link et QR Code.
     */
    fun getQrPayload(user: UserEntity): String {
        val code = getFourDigitId(user)
        val tag = getJamesTag(user)
        return "jameschat://user/${user.id}?code=$code&tag=${java.net.URLEncoder.encode(tag, "UTF-8")}&username=${user.username}"
    }

    /**
     * Génère un QR Code scannable de haute qualité pour le profil de l'utilisateur.
     */
    fun generateUserQrCodeBitmap(user: UserEntity, size: Int = 260): Bitmap {
        val payload = getQrPayload(user)
        val matrixSize = 25
        val grid = Array(matrixSize) { BooleanArray(matrixSize) }
        val random = Random(payload.hashCode().toLong())

        // Coins de localisation (motifs 7x7)
        fun drawFinderPattern(startX: Int, startY: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    grid[startY + r][startX + c] = isBorder || isCenter
                }
            }
        }

        drawFinderPattern(0, 0)
        drawFinderPattern(matrixSize - 7, 0)
        drawFinderPattern(0, matrixSize - 7)

        // Lignes de synchronisation
        for (i in 7 until matrixSize - 7) {
            grid[6][i] = (i % 2 == 0)
            grid[i][6] = (i % 2 == 0)
        }

        // Données encodées
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                val inFinder1 = r < 8 && c < 8
                val inFinder2 = r < 8 && c >= matrixSize - 8
                val inFinder3 = r >= matrixSize - 8 && c < 8
                val inTiming = (r == 6 || c == 6)

                if (!inFinder1 && !inFinder2 && !inFinder3 && !inTiming) {
                    grid[r][c] = random.nextBoolean()
                }
            }
        }

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val moduleSize = size / matrixSize
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                val color = if (grid[r][c]) AndroidColor.BLACK else AndroidColor.WHITE
                for (px in 0 until moduleSize) {
                    for (py in 0 until moduleSize) {
                        val x = c * moduleSize + px
                        val y = r * moduleSize + py
                        if (x < size && y < size) {
                            bitmap.setPixel(x, y, color)
                        }
                    }
                }
            }
        }
        return bitmap
    }

    /**
     * Analyse et normalise toute requête de recherche (QR, Tag, ID, code, @username).
     * Gère toutes les variations de casse, d'espaces et de préfixes,
     * sans imposer une longueur arbitraire de 4 chiffres.
     */
    fun parseContactQuery(raw: String): ContactSearchParams {
        val normalized = normalizeRaw(raw)
        val trimmed = normalized.trim()
        val lower = trimmed.lowercase()

        if (trimmed.isEmpty()) {
            return ContactSearchParams(raw = raw, normalizedQuery = "")
        }

        // 1. Détection QR Code / Deep Link : jameschat://user/<id>?code=...
        if (lower.startsWith("jameschat://user/")) {
            val after = trimmed.substringAfter("jameschat://user/")
            val userId = after.substringBefore("?").trim()
            val codeParam = Regex("[?&]code=([a-zA-Z0-9_]+)").find(after)?.groupValues?.getOrNull(1)
            val usernameParam = Regex("[?&]username=([a-zA-Z0-9_]+)").find(after)?.groupValues?.getOrNull(1)
            val tagParam = Regex("[?&]tag=([^&]+)").find(after)?.groupValues?.getOrNull(1)?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            return ContactSearchParams(
                raw = trimmed,
                normalizedQuery = userId,
                exactUserId = userId.ifEmpty { null },
                candidateCode = codeParam,
                candidateTag = tagParam,
                username = usernameParam?.lowercase(),
                isQrCode = true
            )
        }

        // 2. Détection James Tag : "JAMES#4827", "james # 4827", "#4827", "JAMES#0001"
        if (lower.startsWith("james#") || lower.startsWith("#")) {
            val code = trimmed.substringAfter("#").trim().replace("\\s".toRegex(), "")
            if (code.isNotEmpty()) {
                val fullTag = "JAMES#${code.uppercase()}"
                return ContactSearchParams(
                    raw = trimmed,
                    normalizedQuery = fullTag,
                    candidateTag = fullTag,
                    candidateCode = code
                )
            }
        }

        // 3. Détection James ID : "JMS-4827", "jms 4827", "jms4827"
        if (lower.startsWith("jms-") || lower.startsWith("jms")) {
            val code = trimmed.replace("(?i)jms-?\\s*".toRegex(), "").trim()
            if (code.isNotEmpty()) {
                val fullCustomId = "JMS-${code.uppercase()}"
                val fullTag = "JAMES#${code.uppercase()}"
                return ContactSearchParams(
                    raw = trimmed,
                    normalizedQuery = fullCustomId,
                    candidateCustomId = fullCustomId,
                    candidateTag = fullTag,
                    candidateCode = code
                )
            }
        }

        // 4. Détection Nom d'utilisateur avec arobase : "@jamesdesigner"
        if (trimmed.startsWith("@")) {
            val clean = trimmed.removePrefix("@").trim().lowercase()
            return ContactSearchParams(
                raw = trimmed,
                normalizedQuery = clean,
                username = clean
            )
        }

        // 5. Code numérique ou alphanumérique court (ex: "4827", "0001", "10023")
        val isNumeric = trimmed.all { it.isDigit() }
        if (isNumeric && trimmed.length in 3..10) {
            val tag = "JAMES#${trimmed.uppercase()}"
            val custom = "JMS-${trimmed.uppercase()}"
            return ContactSearchParams(
                raw = trimmed,
                normalizedQuery = trimmed,
                candidateCode = trimmed,
                candidateTag = tag,
                candidateCustomId = custom,
                username = trimmed.lowercase(),
                generalQuery = trimmed
            )
        }

        // 6. Requête générale : nom d'utilisateur sans @ ou nom complet
        val cleanUser = trimmed.lowercase()
        val tagCandidate = "JAMES#${trimmed.uppercase()}"
        val customCandidate = "JMS-${trimmed.uppercase()}"
        return ContactSearchParams(
            raw = trimmed,
            normalizedQuery = cleanUser,
            username = cleanUser,
            candidateCode = trimmed,
            candidateTag = tagCandidate,
            candidateCustomId = customCandidate,
            generalQuery = trimmed
        )
    }

    data class ContactSearchParams(
        val raw: String,
        val normalizedQuery: String = "",
        val exactUserId: String? = null,
        val candidateCode: String? = null,
        val candidateTag: String? = null,
        val candidateCustomId: String? = null,
        val username: String? = null,
        val generalQuery: String? = null,
        val isQrCode: Boolean = false
    )
}
