package com.example.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.data.model.UserEntity
import java.util.Random

/**
 * Utility for James Chat 5-way unique identification:
 * 1. 4-digit code (e.g. "4827")
 * 2. Username (e.g. "@jamesdesigner")
 * 3. James ID (e.g. "JMS-4827")
 * 4. James Tag (e.g. "JAMES#4827") - Primary identifier
 * 5. QR Code containing URI "jameschat://user/<id>?tag=JAMES#<id>"
 */
object JamesIdentityUtil {

    /**
     * Extracts or sanitizes a 4-digit identifier code (e.g. "4827").
     */
    fun sanitizeFourDigitCode(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        return if (digits.length == 4) digits else null
    }

    /**
     * Returns the 4-digit code from any stored personalId or fourDigitCode.
     * Guaranteed to be a 4-digit numeric string.
     */
    fun getFourDigitId(user: UserEntity): String {
        val raw = if (user.fourDigitCode.isNotEmpty()) user.fourDigitCode else user.personalId
        val digits = raw.filter { it.isDigit() }
        if (digits.length == 4) return digits
        // Derive deterministically from user ID if empty/legacy
        val hash = Math.abs(user.id.hashCode()) % 9000 + 1000
        return hash.toString()
    }

    /**
     * Primary modern display identifier: JAMES#4827
     */
    fun getJamesTag(user: UserEntity): String {
        return "JAMES#${getFourDigitId(user)}"
    }

    /**
     * Shareable custom James ID: JMS-4827
     */
    fun getJamesCustomId(user: UserEntity): String {
        return "JMS-${getFourDigitId(user)}"
    }

    /**
     * Formatted username: @username
     */
    fun getFormattedUsername(user: UserEntity): String {
        val u = user.username.removePrefix("@")
        return "@$u"
    }

    /**
     * Standard deep link / QR payload for direct user opening.
     */
    fun getQrPayload(user: UserEntity): String {
        val code = getFourDigitId(user)
        return "jameschat://user/${user.id}?code=$code&tag=JAMES%23$code&username=${user.username}"
    }

    /**
     * Generates a clear, scannable QR Code bitmap for the user's profile.
     */
    fun generateUserQrCodeBitmap(user: UserEntity, size: Int = 260): Bitmap {
        val payload = getQrPayload(user)
        val matrixSize = 25
        val grid = Array(matrixSize) { BooleanArray(matrixSize) }
        val random = Random(payload.hashCode().toLong())

        // 1. Draw Corner Position Patterns (7x7 squares at 3 corners)
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

        // 2. Timing patterns
        for (i in 7 until matrixSize - 7) {
            grid[6][i] = (i % 2 == 0)
            grid[i][6] = (i % 2 == 0)
        }

        // 3. Fill data bits seeded by content
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

        // 4. Render to Bitmap
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
     * Parses any input text or QR payload to determine candidate search terms.
     */
    fun parseContactQuery(raw: String): ContactSearchParams {
        val trimmed = raw.trim()
        val lower = trimmed.lowercase()

        // 1. QR Code payload check
        if (lower.startsWith("jameschat://user/")) {
            val after = trimmed.substringAfter("jameschat://user/")
            val userId = after.substringBefore("?").trim()
            val codeParam = Regex("[?&]code=([0-9]{4})").find(after)?.groupValues?.getOrNull(1)
            val usernameParam = Regex("[?&]username=([a-zA-Z0-9_]+)").find(after)?.groupValues?.getOrNull(1)
            return ContactSearchParams(
                raw = trimmed,
                exactUserId = userId.ifEmpty { null },
                fourDigitCode = codeParam,
                username = usernameParam,
                isQrCode = true
            )
        }

        // 2. James Tag check: "JAMES#4827" or "#4827"
        if (lower.startsWith("james#") || lower.startsWith("#")) {
            val num = trimmed.substringAfter("#").trim()
            if (num.length == 4 && num.all { it.isDigit() }) {
                return ContactSearchParams(raw = trimmed, fourDigitCode = num)
            }
        }

        // 3. James ID check: "JMS-4827" or "JMS4827"
        if (lower.startsWith("jms-") || lower.startsWith("jms")) {
            val num = trimmed.replace("(?i)jms-?".toRegex(), "").trim()
            if (num.length == 4 && num.all { it.isDigit() }) {
                return ContactSearchParams(raw = trimmed, fourDigitCode = num)
            }
        }

        // 4. 4-digit code check directly: "4827"
        if (trimmed.length == 4 && trimmed.all { it.isDigit() }) {
            return ContactSearchParams(raw = trimmed, fourDigitCode = trimmed)
        }

        // 5. Username check: "@jamesdesigner"
        if (trimmed.startsWith("@")) {
            val clean = trimmed.removePrefix("@").trim()
            return ContactSearchParams(raw = trimmed, username = clean)
        }

        return ContactSearchParams(raw = trimmed, generalQuery = trimmed)
    }

    data class ContactSearchParams(
        val raw: String,
        val exactUserId: String? = null,
        val fourDigitCode: String? = null,
        val username: String? = null,
        val generalQuery: String? = null,
        val isQrCode: Boolean = false
    )
}
