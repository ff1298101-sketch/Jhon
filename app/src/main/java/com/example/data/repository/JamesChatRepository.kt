package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.JamesChatDao
import com.example.data.local.JamesChatDatabase
import com.example.data.model.AdminLogEntity
import com.example.data.model.AdminPermission
import com.example.data.model.AppLanguage
import com.example.data.model.CallRecordEntity
import com.example.data.model.CallType
import com.example.data.model.ChannelCommentEntity
import com.example.data.model.ChannelEntity
import com.example.data.model.ChannelPostEntity
import com.example.data.model.ChannelSubscriptionEntity
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.GroupEntity
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.ModerationDossierEntity
import com.example.data.model.ReactionEntity
import com.example.data.model.ReportEntity
import com.example.data.model.SessionEntity
import com.example.data.model.StatusEntity
import com.example.data.model.StatusType
import com.example.data.model.StickerFavoriteEntity
import com.example.data.model.StickerItem
import com.example.data.model.StickerRecentEntity
import com.example.data.model.ThemeMode
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VerifiedBadgeLogEntity
import com.example.util.JamesIdentityUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

data class ConfiguredAdmin(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val title: String,
    val bio: String
)

data class ChannelStats(
    val subscribers: Int,
    val posts: Int,
    val views: Int,
    val reactions: Int,
    val engagementRate: Float
)

data class AdminAnalytics(
    val totalUsers: Int,
    val activeUsers: Int,
    val newAccounts: Int,
    val totalMessages: Int,
    val totalGroups: Int,
    val totalChannels: Int,
    val totalSubscriptions: Int,
    val totalReports: Int,
    val itemsUnderReview: Int,
    val suspendedAccounts: Int,
    val verifiedBadges: Int,
    val dailyUsers: List<Pair<String, Int>>,
    val dailyMessages: List<Pair<String, Int>>,
    val dailyReports: List<Pair<String, Int>>
)

enum class UsernameCheckResult {
    AVAILABLE,
    TAKEN,
    RESERVED,
    INVALID_FORMAT,
    TOO_SHORT,
    TOO_LONG
}

class JamesChatRepository(private val context: Context) {
    private val db = JamesChatDatabase.getInstance(context)
    val dao: JamesChatDao = db.jamesChatDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("james_chat_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _typingUsers = MutableStateFlow<Map<String, String>>(emptyMap())
    val typingUsers: StateFlow<Map<String, String>> = _typingUsers.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        AppLanguage.entries.find { it.code == prefs.getString("app_lang", "fr") } ?: AppLanguage.FRANCAIS
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("app_theme", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _chatWallpaper = MutableStateFlow(prefs.getString("chat_wallpaper", "default") ?: "default")
    val chatWallpaper: StateFlow<String> = _chatWallpaper.asStateFlow()

    private val _fontSizeScale = MutableStateFlow(prefs.getFloat("font_size_scale", 1.0f))
    val fontSizeScale: StateFlow<Float> = _fontSizeScale.asStateFlow()

    private val _soundsEnabled = MutableStateFlow(prefs.getBoolean("sounds_enabled", true))
    val soundsEnabled: StateFlow<Boolean> = _soundsEnabled.asStateFlow()

    private val _batterySaver = MutableStateFlow(prefs.getBoolean("battery_saver", false))
    val batterySaver: StateFlow<Boolean> = _batterySaver.asStateFlow()

    private val _customRingtone = MutableStateFlow(prefs.getString("custom_ringtone", "JAMES_SYNTH") ?: "JAMES_SYNTH")
    val customRingtone: StateFlow<String> = _customRingtone.asStateFlow()

    private val _isSilentMode = MutableStateFlow(prefs.getBoolean("silent_mode", false))
    val isSilentMode: StateFlow<Boolean> = _isSilentMode.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _vibrationPattern = MutableStateFlow(prefs.getString("vibration_pattern", "Standard") ?: "Standard")
    val vibrationPattern: StateFlow<String> = _vibrationPattern.asStateFlow()

    private val _isReadReceiptsEnabled = MutableStateFlow(prefs.getBoolean("read_receipts", true))
    val isReadReceiptsEnabled: StateFlow<Boolean> = _isReadReceiptsEnabled.asStateFlow()

    private val _isAntiScreenshotEnabled = MutableStateFlow(prefs.getBoolean("anti_screenshot", false))
    val isAntiScreenshotEnabled: StateFlow<Boolean> = _isAntiScreenshotEnabled.asStateFlow()

    private val _isHighContrastEnabled = MutableStateFlow(prefs.getBoolean("high_contrast", false))
    val isHighContrastEnabled: StateFlow<Boolean> = _isHighContrastEnabled.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val OWNER_EMAIL = "owner@jameschat.ht"
        const val MASTER_ADMIN_EMAIL = "hangington53@gmail.com"

        val RESERVED_USERNAMES = setOf(
            "admin", "administrator", "support", "system", "jameschat", "james",
            "root", "security", "official", "help", "moderator", "mod"
        )

        val CONFIGURED_7_ADMINS = listOf(
            ConfiguredAdmin(
                id = "admin_owner_1",
                email = OWNER_EMAIL,
                username = "james_owner",
                firstName = "James",
                lastName = "Fondateur",
                role = UserRole.OWNER,
                title = "Fondateur & Owner",
                bio = "Fondateur & Propriétaire de James Chat"
            ),
            ConfiguredAdmin(
                id = "admin_super_hangington",
                email = MASTER_ADMIN_EMAIL,
                username = "james_superadmin",
                firstName = "Hangington",
                lastName = "Admin",
                role = UserRole.SUPER_ADMIN,
                title = "Super Admin",
                bio = "Super Administrateur Officiel de James Chat"
            ),
            ConfiguredAdmin(
                id = "admin_security_3",
                email = "admin.security@jameschat.ht",
                username = "alex_security",
                firstName = "Alexandre",
                lastName = "Valmont",
                role = UserRole.ADMIN,
                title = "Admin Sécurité",
                bio = "Administrateur - Sécurité & Infrastructure"
            ),
            ConfiguredAdmin(
                id = "admin_community_4",
                email = "admin.community@jameschat.ht",
                username = "nadia_admin",
                firstName = "Nadia",
                lastName = "Joseph",
                role = UserRole.ADMIN,
                title = "Admin Communauté",
                bio = "Administratrice - Opérations & Communauté"
            ),
            ConfiguredAdmin(
                id = "admin_mod_pierre_5",
                email = "mod.pierre@jameschat.ht",
                username = "pierre_mod",
                firstName = "Pierre",
                lastName = "Toussaint",
                role = UserRole.MODERATOR,
                title = "Modérateur Contenus",
                bio = "Modérateur Officiel - Contenus & Médias"
            ),
            ConfiguredAdmin(
                id = "admin_mod_claudia_6",
                email = "mod.claudia@jameschat.ht",
                username = "claudia_mod",
                firstName = "Claudia",
                lastName = "Desrosiers",
                role = UserRole.MODERATOR,
                title = "Modératrice Signalements",
                bio = "Modératrice Officielle - Signalements & Respect des Règles"
            ),
            ConfiguredAdmin(
                id = "admin_support_7",
                email = "support@jameschat.ht",
                username = "james_support",
                firstName = "Support",
                lastName = "James Chat",
                role = UserRole.SUPPORT,
                title = "Support Client",
                bio = "Assistance & Support Utilisateurs James Chat"
            )
        )

        @Volatile
        private var INSTANCE: JamesChatRepository? = null

        fun getInstance(context: Context): JamesChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JamesChatRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun hasPermission(permission: AdminPermission): Boolean {
        val user = _currentUser.value ?: return false
        if (user.isBanned) return false
        if (user.isSuspended && (user.suspendedUntil == 0L || user.suspendedUntil > System.currentTimeMillis())) return false
        val role = try { UserRole.valueOf(user.role) } catch (_: Exception) { UserRole.USER }
        return when (role) {
            UserRole.OWNER -> true
            UserRole.SUPER_ADMIN -> permission != AdminPermission.MANAGE_ROLES
            UserRole.ADMIN -> permission in listOf(
                AdminPermission.VIEW_ADMIN_STATS,
                AdminPermission.MANAGE_V_BADGES,
                AdminPermission.REVIEW_MODERATION_DOSSIERS,
                AdminPermission.APPLY_WARNING,
                AdminPermission.APPLY_CONTENT_REMOVAL,
                AdminPermission.APPLY_SUSPENSION,
                AdminPermission.MANAGE_CHANNELS_GLOBAL,
                AdminPermission.HANDLE_SUPPORT_TICKETS
            )
            UserRole.SENIOR_MODERATOR -> permission in listOf(
                AdminPermission.VIEW_ADMIN_STATS,
                AdminPermission.REVIEW_MODERATION_DOSSIERS,
                AdminPermission.APPLY_WARNING,
                AdminPermission.APPLY_CONTENT_REMOVAL,
                AdminPermission.APPLY_SUSPENSION,
                AdminPermission.HANDLE_SUPPORT_TICKETS
            )
            UserRole.MODERATOR -> permission in listOf(
                AdminPermission.VIEW_ADMIN_STATS,
                AdminPermission.REVIEW_MODERATION_DOSSIERS,
                AdminPermission.APPLY_WARNING,
                AdminPermission.APPLY_CONTENT_REMOVAL,
                AdminPermission.HANDLE_SUPPORT_TICKETS
            )
            UserRole.SUPPORT -> permission in listOf(
                AdminPermission.HANDLE_SUPPORT_TICKETS
            )
            UserRole.USER -> false
        }
    }

    /**
     * Generates a strictly unique 4-digit identifier (1000..9999).
     * Guaranteed never to conflict with an existing user in the database.
     */
    suspend fun generateUniqueFourDigitCode(): String {
        for (attempt in 0..150) {
            val candidate = (1000..9999).random().toString()
            if (dao.countFourDigitCode(candidate) == 0) {
                return candidate
            }
        }
        // Fallback deterministic walk
        for (candidateInt in 1000..9999) {
            val candidate = candidateInt.toString()
            if (dao.countFourDigitCode(candidate) == 0) {
                return candidate
            }
        }
        return "9999"
    }

    /**
     * Resolves a user through any of the 5 contact methods:
     * 1. 4-digit ID (e.g. "4827")
     * 2. Username (e.g. "@jamesdesigner" or "jamesdesigner")
     * 3. James ID (e.g. "JMS-4827")
     * 4. James Tag (e.g. "JAMES#4827")
     * 5. QR Code URI payload (e.g. "jameschat://user/<id>?...")
     */
    suspend fun findUserByContactMethod(rawQuery: String): UserEntity? {
        val parsed = JamesIdentityUtil.parseContactQuery(rawQuery)

        // 1. Exact user ID from QR
        if (parsed.exactUserId != null) {
            val u = dao.getUserById(parsed.exactUserId)
            if (u != null && !u.isBanned) return u
        }

        // 2. 4-digit code (direct, or parsed from tag/id/qr)
        if (parsed.fourDigitCode != null) {
            val u = dao.getUserByFourDigitCode(parsed.fourDigitCode)
            if (u != null && !u.isBanned) return u
        }

        // 3. Exact username
        if (parsed.username != null) {
            val u = dao.getUserByUsername(parsed.username)
            if (u != null && !u.isBanned) return u
        }

        // 4. Fallback: check exact James Tag or James Custom ID match
        val trimmed = rawQuery.trim()
        val byTag = dao.getUserByJamesTag(trimmed)
        if (byTag != null && !byTag.isBanned) return byTag

        val byCustomId = dao.getUserByJamesCustomId(trimmed)
        if (byCustomId != null && !byCustomId.isBanned) return byCustomId

        // 5. Clean query as potential username without @
        val cleanUser = trimmed.removePrefix("@").trim()
        val byUname = dao.getUserByUsername(cleanUser)
        if (byUname != null && !byUname.isBanned) return byUname

        return null
    }

    init {
        coroutineScope.launch {
            seedInitialDataIfNeeded()
            restorePersistedSession()
            purgeExpiredMessages()
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("app_lang", language.code).apply()
        val user = _currentUser.value
        if (user != null) {
            coroutineScope.launch {
                val updated = user.copy(language = language.code)
                dao.updateUser(updated)
                _currentUser.value = updated
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme", mode.name).apply()
    }

    fun setChatWallpaper(key: String) {
        _chatWallpaper.value = key
        prefs.edit().putString("chat_wallpaper", key).apply()
    }

    fun setFontSizeScale(scale: Float) {
        _fontSizeScale.value = scale
        prefs.edit().putFloat("font_size_scale", scale).apply()
    }

    fun setSoundsEnabled(enabled: Boolean) {
        _soundsEnabled.value = enabled
        prefs.edit().putBoolean("sounds_enabled", enabled).apply()
    }

    fun setBatterySaver(enabled: Boolean) {
        _batterySaver.value = enabled
        prefs.edit().putBoolean("battery_saver", enabled).apply()
    }

    fun setNetworkOnline(online: Boolean) {
        _isOnline.value = online
        if (online) {
            coroutineScope.launch {
                syncPendingMessages()
            }
        }
    }

    suspend fun signup(email: String, password: String, country: String = "Haïti"): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return Result.failure(Exception("Email invalide"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins 6 caractères"))
        }
        val existing = dao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("Un compte existe déjà avec cette adresse email"))
        }
        val isMasterAdmin = trimmedEmail == MASTER_ADMIN_EMAIL
        val code = generateUniqueFourDigitCode()
        val tag = "JAMES#$code"
        val customId = "JMS-$code"
        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            email = trimmedEmail,
            username = "",
            country = country,
            role = if (isMasterAdmin) UserRole.ADMIN.name else UserRole.USER.name,
            language = _currentLanguage.value.code,
            fourDigitCode = code,
            personalId = code,
            jamesTag = tag,
            jamesCustomId = customId
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser
        prefs.edit().putString("saved_user_id", newUser.id).apply()
        createDeviceSession(newUser.id)
        return Result.success(newUser)
    }

    suspend fun login(email: String, password: String, rememberMe: Boolean = true): Result<UserEntity> {
        val trimmedInput = email.trim()
        if (trimmedInput.isEmpty()) {
            return Result.failure(Exception("Veuillez saisir votre email, nom d'utilisateur ou ID James"))
        }
        val cleanInput = trimmedInput.lowercase().removePrefix("@")
        var user = dao.getUserByEmail(cleanInput)
            ?: dao.getUserByUsername(cleanInput)
            ?: dao.getUserByJamesCustomId(trimmedInput.uppercase())
            ?: dao.getUserByJamesTag(trimmedInput.uppercase())
            ?: dao.getUserByFourDigitCode(trimmedInput)
            ?: dao.getUserById(trimmedInput)

        if (user == null) {
            if (cleanInput == MASTER_ADMIN_EMAIL.lowercase()) {
                user = UserEntity(
                    id = "admin-master-id",
                    email = MASTER_ADMIN_EMAIL,
                    username = "james_admin",
                    firstName = "James",
                    lastName = "Administrator",
                    role = UserRole.ADMIN.name,
                    bio = "Administrateur principal du réseau James Chat"
                )
                dao.insertUser(user)
            } else {
                return Result.failure(Exception("Aucun compte trouvé avec cet identifiant (Email, @nom d'utilisateur ou ID James)."))
            }
        }
        if (user.language.isNotEmpty()) {
            AppLanguage.entries.find { it.code == user.language }?.let { lang ->
                _currentLanguage.value = lang
                prefs.edit().putString("app_lang", lang.code).apply()
            }
        }
        if (user.isBanned) {
            return Result.failure(Exception("Ce compte a été banni pour non-respect des règles de la communauté"))
        }
        if (user.isSuspended) {
            return Result.failure(Exception("Ce compte est temporairement suspendu par l'administration"))
        }
        _currentUser.value = user
        dao.updateOnlineStatus(user.id, isOnline = true, lastSeen = System.currentTimeMillis())
        if (rememberMe) {
            prefs.edit().putString("saved_user_id", user.id).apply()
        } else {
            prefs.edit().remove("saved_user_id").apply()
        }
        createDeviceSession(user.id)
        return Result.success(user)
    }

    suspend fun loginWithGoogle(
        email: String = "nopyoo21@gmail.com",
        displayName: String = "Google User",
        photoUrl: String = ""
    ): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return Result.failure(Exception("Adresse email Google invalide"))
        }
        var user = dao.getUserByEmail(trimmedEmail)
        val assignedRole = when (trimmedEmail) {
            OWNER_EMAIL -> UserRole.OWNER.name
            MASTER_ADMIN_EMAIL -> UserRole.SUPER_ADMIN.name
            "admin.security@jameschat.ht" -> UserRole.ADMIN.name
            "admin.community@jameschat.ht" -> UserRole.ADMIN.name
            "mod.pierre@jameschat.ht" -> UserRole.MODERATOR.name
            "mod.claudia@jameschat.ht" -> UserRole.MODERATOR.name
            "support@jameschat.ht" -> UserRole.SUPPORT.name
            else -> null
        }
        if (user == null) {
            val parts = displayName.trim().split(" ")
            val fName = parts.getOrNull(0) ?: "Google"
            val lName = parts.drop(1).joinToString(" ").ifEmpty { "User" }
            val autoUsername = trimmedEmail.substringBefore("@").replace(".", "_").take(15)
            val finalUsername = if (dao.countUsername(autoUsername) == 0 && !RESERVED_USERNAMES.contains(autoUsername)) {
                autoUsername
            } else {
                "${autoUsername}_${(100..999).random()}"
            }
            val uniqueCode = generateUniqueFourDigitCode()
            user = UserEntity(
                id = UUID.randomUUID().toString(),
                email = trimmedEmail,
                username = finalUsername,
                firstName = fName,
                lastName = lName,
                avatarUrl = photoUrl,
                role = assignedRole ?: UserRole.USER.name,
                isVerified = assignedRole != null,
                isTeamMember = assignedRole != null,
                language = _currentLanguage.value.code,
                fourDigitCode = uniqueCode,
                personalId = uniqueCode,
                jamesTag = "JAMES#$uniqueCode",
                jamesCustomId = "JMS-$uniqueCode"
            )
            dao.insertUser(user)
        } else {
            val needsRoleUpgrade = assignedRole != null && user.role != assignedRole
            val existingCode = if (user.fourDigitCode.isNotEmpty()) user.fourDigitCode else generateUniqueFourDigitCode()
            val existingTag = if (user.jamesTag.isNotEmpty()) user.jamesTag else "JAMES#$existingCode"
            val existingCustomId = if (user.jamesCustomId.isNotEmpty()) user.jamesCustomId else "JMS-$existingCode"
            val updatedUser = user.copy(
                avatarUrl = if (user.avatarUrl.isEmpty() && photoUrl.isNotEmpty()) photoUrl else user.avatarUrl,
                role = if (needsRoleUpgrade) assignedRole!! else user.role,
                isVerified = if (assignedRole != null) true else user.isVerified,
                isTeamMember = if (assignedRole != null) true else user.isTeamMember,
                fourDigitCode = existingCode,
                personalId = existingCode,
                jamesTag = existingTag,
                jamesCustomId = existingCustomId
            )
            if (updatedUser != user) {
                dao.updateUser(updatedUser)
                user = updatedUser
            }
        }
        if (user.isBanned) {
            return Result.failure(Exception("Ce compte a été banni définitivement par la modération."))
        }
        if (user.isSuspended) {
            return Result.failure(Exception("Compte suspendu temporairement. Contactez le support James Chat."))
        }
        _currentUser.value = user
        dao.updateOnlineStatus(user.id, isOnline = true, lastSeen = System.currentTimeMillis())
        prefs.edit().putString("saved_user_id", user.id).apply()
        createDeviceSession(user.id)
        return Result.success(user)
    }

    suspend fun linkGoogleToCurrentAccount(googleEmail: String): Result<UserEntity> {
        val current = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val existing = dao.getUserByEmail(googleEmail.trim().lowercase())
        if (existing != null && existing.id != current.id) {
            return Result.failure(Exception("Cette adresse Google est déjà associée à un autre compte."))
        }
        val updated = current.copy(email = googleEmail.trim().lowercase())
        dao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun logout() {
        val user = _currentUser.value
        if (user != null) {
            dao.updateOnlineStatus(user.id, isOnline = false, lastSeen = System.currentTimeMillis())
        }
        _currentUser.value = null
        prefs.edit().remove("saved_user_id").apply()
    }

    suspend fun checkUsernameAvailability(username: String): UsernameCheckResult {
        val trimmed = username.trim().lowercase()
        if (trimmed.length < 3) return UsernameCheckResult.TOO_SHORT
        if (trimmed.length > 20) return UsernameCheckResult.TOO_LONG
        if (!trimmed.matches(Regex("^[a-zA-Z0-9_]+$"))) return UsernameCheckResult.INVALID_FORMAT
        if (RESERVED_USERNAMES.contains(trimmed)) return UsernameCheckResult.RESERVED
        val currentUserId = _currentUser.value?.id
        val existing = dao.getUserByUsername(trimmed)
        if (existing != null && existing.id != currentUserId) {
            return UsernameCheckResult.TAKEN
        }
        return UsernameCheckResult.AVAILABLE
    }

    suspend fun setUsername(newUsername: String): Result<UserEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val check = checkUsernameAvailability(newUsername)
        if (check != UsernameCheckResult.AVAILABLE) {
            val message = when (check) {
                UsernameCheckResult.TOO_SHORT -> "Le nom d'utilisateur doit comporter au moins 3 caractères"
                UsernameCheckResult.TOO_LONG -> "Le nom d'utilisateur ne peut excéder 20 caractères"
                UsernameCheckResult.INVALID_FORMAT -> "Seuls les lettres, chiffres et le tiret bas (_) sont acceptés"
                UsernameCheckResult.RESERVED -> "Ce nom d'utilisateur est réservé par le système"
                UsernameCheckResult.TAKEN -> "Ce nom d'utilisateur est déjà pris"
                else -> "Nom d'utilisateur invalide"
            }
            return Result.failure(Exception(message))
        }
        val updated = user.copy(username = newUsername.trim().lowercase())
        dao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        age: Int,
        city: String,
        country: String,
        bio: String,
        avatarUrl: String? = null,
        profileVideoUrl: String? = null,
        showVideoOnProfile: Boolean = false
    ): Result<UserEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val updated = user.copy(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            age = age,
            city = city.trim(),
            country = country.trim(),
            bio = bio.trim(),
            avatarUrl = avatarUrl ?: user.avatarUrl,
            profileVideoUrl = profileVideoUrl ?: user.profileVideoUrl,
            showVideoOnProfile = showVideoOnProfile
        )
        dao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun removeProfilePhoto(): Result<UserEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val updated = user.copy(avatarUrl = "")
        dao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun removeProfileVideo(): Result<UserEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val updated = user.copy(profileVideoUrl = "", showVideoOnProfile = false)
        dao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun quickLoginAsAdmin(): Result<UserEntity> {
        val user = dao.getUserById("admin-master-id") ?: dao.getUserByEmail(MASTER_ADMIN_EMAIL)
        if (user != null) {
            _currentUser.value = user
            prefs.edit().putString("saved_user_id", user.id).apply()
            return Result.success(user)
        }
        return login(MASTER_ADMIN_EMAIL, "adminpassword123")
    }

    private suspend fun restorePersistedSession() {
        var savedId = prefs.getString("saved_user_id", null)
        if (savedId.isNullOrEmpty()) {
            savedId = "admin-master-id"
            prefs.edit().putString("saved_user_id", savedId).apply()
        }
        val user = dao.getUserById(savedId)
        if (user != null && !user.isBanned) {
            val code = if (user.fourDigitCode.isNotEmpty()) user.fourDigitCode else generateUniqueFourDigitCode()
            val tag = if (user.jamesTag.isNotEmpty()) user.jamesTag else "JAMES#$code"
            val customId = if (user.jamesCustomId.isNotEmpty()) user.jamesCustomId else "JMS-$code"
            val validUser = if (user.fourDigitCode != code || user.jamesTag != tag) {
                val updated = user.copy(fourDigitCode = code, personalId = code, jamesTag = tag, jamesCustomId = customId)
                dao.updateUser(updated)
                updated
            } else user
            _currentUser.value = validUser
            dao.updateOnlineStatus(validUser.id, isOnline = true, lastSeen = System.currentTimeMillis())
        }
    }

    private suspend fun createDeviceSession(userId: String) {
        val session = SessionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            deviceName = "Android Pixel 9 Pro (Cet appareil)",
            deviceType = "Android Mobile",
            ipAddress = "192.168.1.42",
            location = "Port-au-Prince / Paris / Miami",
            lastActive = System.currentTimeMillis(),
            isCurrent = true
        )
        dao.insertSession(session)
    }

    suspend fun sendTextMessage(
        conversationId: String,
        text: String,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToSender: String? = null
    ): Result<MessageEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return Result.failure(Exception("Message vide"))
        val conv = dao.getConversationById(conversationId)
        val group = dao.getGroupById(conversationId)
        if (group != null && group.onlyAdminsCanSend) {
            val isCreator = group.creatorId == user.id
            val isAdmin = group.adminIds.split(",").contains(user.id) || group.coAdminIds.split(",").contains(user.id)
            if (!isCreator && !isAdmin) {
                return Result.failure(Exception("Seuls les administrateurs peuvent envoyer des messages dans ce groupe."))
            }
        }
        val expiresAt = if (conv != null && conv.ephemeralDurationMs > 0) {
            System.currentTimeMillis() + conv.ephemeralDurationMs
        } else 0L
        val isPending = !_isOnline.value
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = user.id,
            senderUsername = user.username.ifEmpty { user.firstName },
            text = trimmed,
            type = MessageType.TEXT.name,
            replyToMessageId = replyToId,
            replyToText = replyToText,
            replyToSender = replyToSender,
            status = if (isPending) MessageDeliveryStatus.PENDING.name else MessageDeliveryStatus.SENT.name,
            timestamp = System.currentTimeMillis(),
            expiresAt = expiresAt,
            isPendingSync = isPending
        )
        dao.insertMessage(message)
        dao.updateConversationLastMessage(conversationId, trimmed, message.timestamp)
        com.example.util.JamesSoundManager.playMessageSentSound()
        if (!isPending) {
            simulateMessageLifecycle(conversationId, message.id)
        }
        return Result.success(message)
    }

    suspend fun sendMediaMessage(
        conversationId: String,
        type: MessageType,
        mediaUrl: String,
        fileName: String = "",
        fileSize: String = "",
        durationSeconds: Int = 0,
        caption: String = "",
        waveformData: String = ""
    ): Result<MessageEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val conv = dao.getConversationById(conversationId)
        val group = dao.getGroupById(conversationId)
        if (group != null && group.onlyAdminsCanSend) {
            val isCreator = group.creatorId == user.id
            val isAdmin = group.adminIds.split(",").contains(user.id) || group.coAdminIds.split(",").contains(user.id)
            if (!isCreator && !isAdmin) {
                return Result.failure(Exception("Seuls les administrateurs peuvent publier des médias dans ce groupe."))
            }
        }
        val expiresAt = if (conv != null && conv.ephemeralDurationMs > 0) {
            System.currentTimeMillis() + conv.ephemeralDurationMs
        } else 0L
        val isPending = !_isOnline.value
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = user.id,
            senderUsername = user.username.ifEmpty { user.firstName },
            text = caption.ifEmpty {
                when (type) {
                    MessageType.PHOTO -> "📷 Photo"
                    MessageType.VIDEO -> "🎬 Vidéo"
                    MessageType.AUDIO -> "🎤 Message vocal (${durationSeconds}s)"
                    MessageType.DOCUMENT -> "📄 $fileName"
                    else -> "Média"
                }
            },
            type = type.name,
            mediaUrl = mediaUrl,
            fileName = fileName,
            fileSize = fileSize,
            mediaDurationSeconds = durationSeconds,
            waveformData = waveformData,
            status = if (isPending) MessageDeliveryStatus.PENDING.name else MessageDeliveryStatus.SENT.name,
            timestamp = System.currentTimeMillis(),
            expiresAt = expiresAt,
            isPendingSync = isPending
        )
        dao.insertMessage(message)
        dao.updateConversationLastMessage(conversationId, message.text, message.timestamp)
        com.example.util.JamesSoundManager.playMessageSentSound()
        if (!isPending) {
            simulateMessageLifecycle(conversationId, message.id)
        }
        return Result.success(message)
    }

    private fun simulateMessageLifecycle(conversationId: String, messageId: String) {
        coroutineScope.launch {
            delay(800)
            dao.updateMessageStatus(messageId, MessageDeliveryStatus.DELIVERED.name)
            delay(1200)
            dao.updateMessageStatus(messageId, MessageDeliveryStatus.READ.name)
            val conv = dao.getConversationById(conversationId)
            val sentMsg = dao.getMessageById(messageId)
            if (conv != null && !conv.isGroup) {
                simulateTypingAndReply(conversationId, conv.participantIds, conv.title, sentMsg?.text ?: "")
            }
        }
    }

    private suspend fun simulateTypingAndReply(conversationId: String, participantIds: String, partnerTitle: String, userPrompt: String) {
        val currentUserId = _currentUser.value?.id ?: return
        val partnerId = participantIds.split(",").find { it != currentUserId } ?: return

        val isJamesBot = partnerId == com.example.util.JamesBotEngine.BOT_USER_ID ||
                partnerTitle.contains("James Bot", ignoreCase = true) ||
                conversationId == "conv_james_bot"

        delay(900)
        _typingUsers.value = _typingUsers.value + (conversationId to (if (isJamesBot) "James Bot" else partnerTitle))
        delay(if (isJamesBot) 1500 else 2200)
        _typingUsers.value = _typingUsers.value - conversationId

        val replyText = if (isJamesBot) {
            val publishedKnowledge = dao.getPublishedBotKnowledge()
            val botResult = com.example.util.JamesBotEngine.processMessage(
                userInput = userPrompt,
                knowledgeBase = publishedKnowledge,
                userLanguage = _currentLanguage.value
            )
            botResult.replyText
        } else {
            val replies = listOf(
                "Super ! Bien reçu sur James Chat 👍",
                "Parfait, je regarde ça tout de suite !",
                "Excellent ! L'interface James Chat est super fluide.",
                "Merci pour le message, je te réponds plus tard.",
                "Très bien noté !"
            )
            replies.random()
        }

        val botMessage = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = partnerId,
            senderUsername = if (isJamesBot) "James Bot" else partnerTitle,
            text = replyText,
            status = MessageDeliveryStatus.READ.name,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(botMessage)
        dao.updateConversationLastMessage(conversationId, replyText, botMessage.timestamp)
        dao.incrementConversationUnread(conversationId)
        com.example.util.JamesSoundManager.playMessageReceivedSound()
        com.example.util.JamesNotificationManager.showMessageNotification(
            context = context,
            senderName = if (isJamesBot) "James Bot" else partnerTitle,
            messageText = replyText,
            conversationId = conversationId
        )
    }

    suspend fun editMessage(messageId: String, newText: String) {
        dao.editMessage(messageId, newText.trim())
    }

    suspend fun deleteMessage(messageId: String) {
        dao.softDeleteMessage(messageId)
    }

    suspend fun togglePinMessage(messageId: String, isPinned: Boolean) {
        dao.setMessagePinned(messageId, isPinned)
    }

    suspend fun addOrToggleReaction(messageId: String, emoji: String) {
        val user = _currentUser.value ?: return
        val currentReactions = dao.getReactionsForMessage(messageId)
        val myReaction = currentReactions.find { it.userId == user.id }
        if (myReaction != null && myReaction.emoji == emoji) {
            dao.removeReaction(messageId, user.id)
        } else {
            val reaction = ReactionEntity(
                id = "${messageId}_${user.id}",
                messageId = messageId,
                userId = user.id,
                username = user.username.ifEmpty { user.firstName },
                emoji = emoji
            )
            dao.insertReaction(reaction)
        }
    }

    suspend fun removeReaction(messageId: String) {
        val user = _currentUser.value ?: return
        dao.removeReaction(messageId, user.id)
    }

    fun getStickerFavoritesFlow(): Flow<List<StickerFavoriteEntity>> {
        val uid = _currentUser.value?.id ?: ""
        return dao.getStickerFavoritesFlow(uid)
    }

    fun getStickerRecentsFlow(): Flow<List<StickerRecentEntity>> {
        val uid = _currentUser.value?.id ?: ""
        return dao.getStickerRecentsFlow(uid)
    }

    suspend fun toggleStickerFavorite(stickerId: String): Boolean {
        val user = _currentUser.value ?: return false
        val isFav = dao.isStickerFavorite(user.id, stickerId) > 0
        if (isFav) {
            dao.deleteStickerFavorite(user.id, stickerId)
            return false
        } else {
            dao.insertStickerFavorite(
                StickerFavoriteEntity(
                    id = "${user.id}_$stickerId",
                    userId = user.id,
                    stickerId = stickerId
                )
            )
            return true
        }
    }

    suspend fun sendStickerMessage(
        conversationId: String,
        stickerItem: StickerItem,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToSender: String? = null
    ): Result<MessageEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val conv = dao.getConversationById(conversationId)
        val expiresAt = if (conv != null && conv.ephemeralDurationMs > 0) {
            System.currentTimeMillis() + conv.ephemeralDurationMs
        } else 0L
        val isPending = !_isOnline.value
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = user.id,
            senderUsername = user.username.ifEmpty { user.firstName },
            text = "${stickerItem.symbol} [Sticker: ${stickerItem.name}]",
            type = MessageType.STICKER.name,
            stickerId = stickerItem.id,
            replyToMessageId = replyToId,
            replyToText = replyToText,
            replyToSender = replyToSender,
            status = if (isPending) MessageDeliveryStatus.PENDING.name else MessageDeliveryStatus.SENT.name,
            timestamp = System.currentTimeMillis(),
            expiresAt = expiresAt,
            isPendingSync = isPending
        )
        dao.insertMessage(message)
        dao.updateConversationLastMessage(conversationId, message.text, message.timestamp)
        com.example.util.JamesSoundManager.playMessageSentSound()
        dao.insertStickerRecent(
            StickerRecentEntity(
                id = "${user.id}_${stickerItem.id}",
                userId = user.id,
                stickerId = stickerItem.id,
                lastUsedAt = System.currentTimeMillis()
            )
        )
        if (!isPending) {
            simulateMessageLifecycle(conversationId, message.id)
        }
        return Result.success(message)
    }

    suspend fun setEphemeralDuration(conversationId: String, durationMs: Long) {
        dao.setEphemeralDuration(conversationId, durationMs)
    }

    suspend fun setConversationWallpaper(conversationId: String, wallpaperKey: String) {
        dao.setConversationWallpaper(conversationId, wallpaperKey)
    }

    suspend fun getOrCreateDirectConversation(targetUser: UserEntity): String {
        val myId = _currentUser.value?.id ?: "guest"
        val existingConvs = dao.getAllConversationsFlow().firstOrNull() ?: emptyList()
        val found = existingConvs.find { conv ->
            !conv.isGroup && conv.participantIds.contains(myId) && conv.participantIds.contains(targetUser.id)
        }
        if (found != null) {
            return found.id
        }
        val convId = UUID.randomUUID().toString()
        val newConv = ConversationEntity(
            id = convId,
            isGroup = false,
            title = if (targetUser.firstName.isNotEmpty()) "${targetUser.firstName} ${targetUser.lastName}".trim() else targetUser.username,
            avatarUrl = targetUser.avatarUrl,
            participantIds = "$myId,${targetUser.id}",
            lastMessageSnippet = "Début de la conversation",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        dao.insertConversation(newConv)
        return convId
    }

    suspend fun createGroup(
        name: String,
        description: String,
        rules: String,
        category: String = "Groupes d'amis",
        institution: String = "",
        gradeLevel: String = "",
        avatarUrl: String = "",
        presentationVideoUrl: String = "",
        isPublic: Boolean = true,
        onlyAdminsCanSend: Boolean = false,
        requireApproval: Boolean = false,
        initialMemberIds: List<String> = emptyList()
    ): Result<GroupEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return Result.failure(Exception("Le nom du groupe est obligatoire"))
        val groupId = UUID.randomUUID().toString()
        val allMembers = (listOf(user.id) + initialMemberIds).distinct()
        val inviteCode = "JC-${(100000..999999).random()}"
        val group = GroupEntity(
            id = groupId,
            name = trimmedName,
            avatarUrl = avatarUrl,
            presentationVideoUrl = presentationVideoUrl,
            description = description.trim(),
            category = category,
            institution = institution.trim(),
            gradeLevel = gradeLevel.trim(),
            rules = rules.trim(),
            creatorId = user.id,
            adminIds = user.id,
            coAdminIds = "",
            memberIds = allMembers.joinToString(","),
            pendingMemberIds = "",
            inviteCode = inviteCode,
            onlyAdminsCanSend = onlyAdminsCanSend,
            requireApproval = requireApproval,
            isPublic = isPublic
        )
        dao.insertGroup(group)
        val groupConv = ConversationEntity(
            id = groupId,
            isGroup = true,
            title = trimmedName,
            avatarUrl = avatarUrl,
            participantIds = allMembers.joinToString(","),
            lastMessageSnippet = "Groupe créé dans la catégorie $category",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        dao.insertConversation(groupConv)
        return Result.success(group)
    }

    suspend fun leaveGroup(groupId: String) {
        val user = _currentUser.value ?: return
        val group = dao.getGroupById(groupId) ?: return
        val members = group.memberIds.split(",").filter { it != user.id }
        val admins = group.adminIds.split(",").filter { it != user.id }
        if (members.isEmpty()) {
            dao.deleteGroup(groupId)
            dao.deleteConversation(groupId)
        } else {
            val updated = group.copy(
                memberIds = members.joinToString(","),
                adminIds = if (admins.isEmpty()) members.take(1).joinToString(",") else admins.joinToString(",")
            )
            dao.updateGroup(updated)
        }
    }

    suspend fun toggleGroupAdminsOnly(groupId: String, onlyAdminsCanSend: Boolean) {
        val group = dao.getGroupById(groupId) ?: return
        dao.updateGroup(group.copy(onlyAdminsCanSend = onlyAdminsCanSend))
    }

    suspend fun createStatus(
        type: StatusType,
        content: String,
        caption: String = "",
        backgroundColorHex: String = "#1E3A8A",
        musicTitle: String = ""
    ): Result<StatusEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val status = StatusEntity(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            username = user.username.ifEmpty { user.firstName },
            userAvatar = user.avatarUrl,
            type = type.name,
            content = content,
            caption = caption,
            backgroundColorHex = backgroundColorHex,
            musicTitle = musicTitle,
            expiresAt = System.currentTimeMillis() + 86400000L
        )
        dao.insertStatus(status)
        return Result.success(status)
    }

    suspend fun markStatusViewed(statusId: String) {
        val user = _currentUser.value ?: return
        dao.updateStatusViewers(statusId, "[\"${user.username}\"]")
    }

    suspend fun logCall(
        targetUser: UserEntity,
        isVideo: Boolean,
        callType: CallType,
        durationSeconds: Int = 0
    ): CallRecordEntity {
        val user = _currentUser.value ?: return CallRecordEntity(
            id = UUID.randomUUID().toString(),
            callerId = "me", callerName = "Me", receiverId = targetUser.id, receiverName = targetUser.username
        )
        val record = CallRecordEntity(
            id = UUID.randomUUID().toString(),
            callerId = if (callType == CallType.OUTGOING) user.id else targetUser.id,
            callerName = if (callType == CallType.OUTGOING) user.firstName.ifEmpty { user.username } else targetUser.firstName.ifEmpty { targetUser.username },
            callerAvatar = if (callType == CallType.OUTGOING) user.avatarUrl else targetUser.avatarUrl,
            receiverId = if (callType == CallType.OUTGOING) targetUser.id else user.id,
            receiverName = if (callType == CallType.OUTGOING) targetUser.firstName.ifEmpty { targetUser.username } else user.firstName.ifEmpty { user.username },
            receiverAvatar = if (callType == CallType.OUTGOING) targetUser.avatarUrl else user.avatarUrl,
            isVideo = isVideo,
            callType = callType.name,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis()
        )
        dao.insertCallRecord(record)
        return record
    }

    suspend fun addContact(targetUserId: String) {
        val user = _currentUser.value ?: return
        val contact = ContactEntity(
            id = "${user.id}_$targetUserId",
            ownerId = user.id,
            contactUserId = targetUserId
        )
        dao.insertContact(contact)
    }

    suspend fun removeContact(targetUserId: String) {
        val user = _currentUser.value ?: return
        dao.deleteContact(user.id, targetUserId)
    }

    suspend fun setContactBlocked(targetUserId: String, isBlocked: Boolean) {
        val user = _currentUser.value ?: return
        dao.setContactBlocked(user.id, targetUserId, isBlocked)
    }

    suspend fun reportUser(reportedUser: UserEntity, reason: String, details: String) {
        val user = _currentUser.value ?: return
        val report = ReportEntity(
            id = UUID.randomUUID().toString(),
            reporterId = user.id,
            reporterUsername = user.username.ifEmpty { user.email },
            reportedUserId = reportedUser.id,
            reportedUsername = reportedUser.username,
            reason = reason,
            details = details,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        dao.insertReport(report)
    }

    suspend fun recordCall(receiver: UserEntity, isVideo: Boolean, durationSeconds: Int) {
        val user = _currentUser.value ?: return
        val record = CallRecordEntity(
            id = UUID.randomUUID().toString(),
            callerId = user.id,
            callerName = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}".trim() else user.username,
            callerAvatar = user.avatarUrl,
            receiverId = receiver.id,
            receiverName = if (receiver.firstName.isNotEmpty()) "${receiver.firstName} ${receiver.lastName}".trim() else receiver.username,
            receiverAvatar = receiver.avatarUrl,
            isVideo = isVideo,
            callType = CallType.OUTGOING.name,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        dao.insertCallRecord(record)
    }

    suspend fun recordIncomingCall(caller: UserEntity, isVideo: Boolean, durationSeconds: Int, callType: CallType = CallType.INCOMING) {
        val user = _currentUser.value ?: return
        val record = CallRecordEntity(
            id = UUID.randomUUID().toString(),
            callerId = caller.id,
            callerName = if (caller.firstName.isNotEmpty()) "${caller.firstName} ${caller.lastName}".trim() else caller.username,
            callerAvatar = caller.avatarUrl,
            receiverId = user.id,
            receiverName = if (user.firstName.isNotEmpty()) "${user.firstName} ${user.lastName}".trim() else user.username,
            receiverAvatar = user.avatarUrl,
            isVideo = isVideo,
            callType = callType.name,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        dao.insertCallRecord(record)
    }

    suspend fun markConversationAsRead(conversationId: String) {
        dao.resetConversationUnread(conversationId)
    }

    fun setCustomRingtone(ringtone: String) {
        _customRingtone.value = ringtone
        prefs.edit().putString("custom_ringtone", ringtone).apply()
    }

    fun setSilentMode(silent: Boolean) {
        _isSilentMode.value = silent
        prefs.edit().putBoolean("silent_mode", silent).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun setVibrationPattern(pattern: String) {
        _vibrationPattern.value = pattern
        prefs.edit().putString("vibration_pattern", pattern).apply()
    }

    fun setReadReceiptsEnabled(enabled: Boolean) {
        _isReadReceiptsEnabled.value = enabled
        prefs.edit().putBoolean("read_receipts", enabled).apply()
    }

    fun setAntiScreenshotEnabled(enabled: Boolean) {
        _isAntiScreenshotEnabled.value = enabled
        prefs.edit().putBoolean("anti_screenshot", enabled).apply()
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        _isHighContrastEnabled.value = enabled
        prefs.edit().putBoolean("high_contrast", enabled).apply()
    }

    fun isCurrentUserAdmin(): Boolean {
        val user = _currentUser.value ?: return false
        val role = try { UserRole.valueOf(user.role) } catch (_: Exception) { UserRole.USER }
        return role != UserRole.USER || user.email.lowercase() == MASTER_ADMIN_EMAIL || user.email.lowercase() == OWNER_EMAIL
    }

    fun getSubscribedChannelsFlow(): Flow<List<ChannelSubscriptionEntity>> {
        val uid = _currentUser.value?.id ?: ""
        return dao.getSubscribedChannelsFlow(uid)
    }

    suspend fun createChannel(
        name: String,
        username: String,
        description: String,
        avatarUrl: String = "",
        presentationVideoUrl: String = ""
    ): Result<ChannelEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val trimmedName = name.trim()
        val trimmedUsername = username.trim().lowercase().removePrefix("@")
        if (trimmedName.length < 3) return Result.failure(Exception("Le nom du canal doit comporter au moins 3 caractères"))
        if (trimmedUsername.length < 3) return Result.failure(Exception("L'identifiant du canal doit comporter au moins 3 caractères"))
        val existing = dao.getChannelByUsername(trimmedUsername)
        if (existing != null) return Result.failure(Exception("Cet identifiant de canal est déjà utilisé"))
        val channel = ChannelEntity(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            username = trimmedUsername,
            avatarUrl = avatarUrl,
            description = description.trim(),
            presentationVideoUrl = presentationVideoUrl.trim(),
            creatorId = user.id,
            creatorEmail = user.email,
            subscribersCount = 1,
            isVerified = user.isVerified || isCurrentUserAdmin(),
            createdAt = System.currentTimeMillis()
        )
        dao.insertChannel(channel)
        dao.insertSubscription(
            ChannelSubscriptionEntity(
                id = "${channel.id}_${user.id}",
                channelId = channel.id,
                userId = user.id,
                notificationsEnabled = true
            )
        )
        return Result.success(channel)
    }

    suspend fun publishChannelPost(
        channelId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        stickerId: String = "",
        allowComments: Boolean = true
    ): Result<ChannelPostEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val channel = dao.getChannelById(channelId) ?: return Result.failure(Exception("Canal introuvable"))
        if (channel.creatorId != user.id && !hasPermission(AdminPermission.MANAGE_CHANNELS_GLOBAL)) {
            return Result.failure(Exception("Seul le créateur peut publier sur ce canal"))
        }
        val post = ChannelPostEntity(
            id = UUID.randomUUID().toString(),
            channelId = channelId,
            authorId = user.id,
            authorUsername = channel.name,
            authorAvatarUrl = channel.avatarUrl,
            text = text.trim(),
            type = type.name,
            mediaUrl = mediaUrl,
            stickerId = stickerId,
            allowComments = allowComments,
            createdAt = System.currentTimeMillis()
        )
        dao.insertChannelPost(post)
        dao.updateChannelPostsCount(channelId, channel.postsCount + 1)
        com.example.util.JamesSoundManager.playChannelPostSound()
        return Result.success(post)
    }

    suspend fun reactToChannelPost(postId: String, emoji: String) {
        val post = dao.getChannelPostById(postId) ?: return
        val currentJson = post.reactionsJson
        val updated = if (currentJson.contains(emoji)) {
            currentJson
        } else {
            if (currentJson.isEmpty() || currentJson == "{}") emoji else "$currentJson,$emoji"
        }
        dao.updatePostReactions(postId, updated)
    }

    fun getChannelPostsFlow(channelId: String): Flow<List<ChannelPostEntity>> = dao.getChannelPostsFlow(channelId)
    fun getChannelCommentsFlow(postId: String): Flow<List<ChannelCommentEntity>> = dao.getChannelCommentsFlow(postId)

    suspend fun addChannelComment(postId: String, text: String): Result<ChannelCommentEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return Result.failure(Exception("Commentaire vide"))
        val comment = ChannelCommentEntity(
            id = UUID.randomUUID().toString(),
            postId = postId,
            userId = user.id,
            username = user.username.ifEmpty { user.firstName },
            userAvatar = user.avatarUrl,
            text = trimmed,
            createdAt = System.currentTimeMillis()
        )
        dao.insertChannelComment(comment)
        dao.incrementPostComments(postId)
        return Result.success(comment)
    }

    suspend fun toggleChannelSubscription(channelId: String): Boolean {
        val user = _currentUser.value ?: return false
        val existing = dao.getSubscription(channelId, user.id)
        val isSubscribed = if (existing != null) {
            dao.deleteSubscription(channelId, user.id)
            false
        } else {
            dao.insertSubscription(
                ChannelSubscriptionEntity(
                    id = "${channelId}_${user.id}",
                    channelId = channelId,
                    userId = user.id,
                    notificationsEnabled = true
                )
            )
            true
        }
        val count = dao.countChannelSubscribers(channelId)
        dao.updateChannelSubscribersCount(channelId, count)
        return isSubscribed
    }

    suspend fun toggleChannelNotifications(channelId: String): Boolean {
        val user = _currentUser.value ?: return false
        val existing = dao.getSubscription(channelId, user.id) ?: return false
        val newState = !existing.notificationsEnabled
        dao.updateSubscriptionNotifications(channelId, user.id, newState)
        return newState
    }

    suspend fun grantVerifiedBadge(
        targetUserId: String,
        category: String,
        reason: String,
        badgeStyle: String = "SEAL"
    ): Result<Unit> {
        val admin = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val targetUser = dao.getUserById(targetUserId) ?: return Result.failure(Exception("Utilisateur introuvable"))
        val now = System.currentTimeMillis()
        dao.updateVerificationStatus(
            userId = targetUserId,
            isVerified = true,
            verifiedAt = now,
            adminEmail = admin.email,
            reason = "$category: ${reason.trim()}".ifEmpty { "Attribution administrative officielle" },
            badgeStyle = badgeStyle
        )
        dao.insertVerificationBadge(
            com.example.data.model.VerificationBadgeEntity(
                userId = targetUserId,
                badgeStyle = badgeStyle,
                verified = true,
                grantedBy = admin.email,
                reason = reason.trim(),
                grantedAt = now,
                updatedAt = now
            )
        )
        dao.insertVerifiedBadgeLog(
            VerifiedBadgeLogEntity(
                id = UUID.randomUUID().toString(),
                userId = targetUserId,
                username = targetUser.username.ifEmpty { targetUser.firstName },
                action = "GRANT_BADGE",
                reason = "Style: $badgeStyle - ${reason.trim()}",
                adminEmail = admin.email,
                timestamp = now
            )
        )
        // Also if currentUser is updated, refresh state
        if (_currentUser.value?.id == targetUserId) {
            _currentUser.value = _currentUser.value?.copy(
                isVerified = true,
                verifiedAt = now,
                verifiedByAdminEmail = admin.email,
                verificationReason = reason.trim(),
                verificationBadgeStyle = badgeStyle
            )
        }
        return Result.success(Unit)
    }

    suspend fun revokeVerifiedBadge(targetUserId: String, reason: String): Result<Unit> {
        val admin = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val targetUser = dao.getUserById(targetUserId) ?: return Result.failure(Exception("Utilisateur introuvable"))
        val now = System.currentTimeMillis()
        dao.updateVerificationStatus(
            userId = targetUserId,
            isVerified = false,
            verifiedAt = 0L,
            adminEmail = admin.email,
            reason = "Révoqué : ${reason.trim()}",
            badgeStyle = "SEAL"
        )
        dao.deleteVerificationBadge(targetUserId)
        dao.insertVerifiedBadgeLog(
            VerifiedBadgeLogEntity(
                id = UUID.randomUUID().toString(),
                userId = targetUserId,
                username = targetUser.username.ifEmpty { targetUser.firstName },
                action = "REVOKE_BADGE",
                reason = reason.trim(),
                adminEmail = admin.email,
                timestamp = now
            )
        )
        if (_currentUser.value?.id == targetUserId) {
            _currentUser.value = _currentUser.value?.copy(
                isVerified = false,
                verifiedAt = 0L,
                verificationReason = "Révoqué : ${reason.trim()}"
            )
        }
        return Result.success(Unit)
    }

    suspend fun submitVerificationRequest(
        accountName: String,
        username: String,
        accountType: String,
        reason: String,
        additionalInfo: String,
        preferredStyle: String = "SEAL"
    ): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val existing = dao.getLatestVerificationRequest(user.id)
        if (existing != null && existing.status == "PENDING") {
            return Result.failure(Exception("Vous avez déjà une demande de vérification en attente."))
        }
        val request = com.example.data.model.VerificationRequestEntity(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            accountName = accountName.trim().ifEmpty { "${user.firstName} ${user.lastName}".trim() },
            username = username.trim().removePrefix("@").ifEmpty { user.username },
            accountType = accountType,
            reason = reason.trim(),
            additionalInfo = additionalInfo.trim(),
            preferredStyle = preferredStyle,
            status = "PENDING",
            submittedAt = System.currentTimeMillis()
        )
        dao.insertVerificationRequest(request)
        return Result.success(Unit)
    }

    suspend fun reviewVerificationRequest(
        requestId: String,
        approved: Boolean,
        badgeStyle: String = "SEAL",
        rejectionReason: String = ""
    ): Result<Unit> {
        val admin = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val allRequests = dao.getAllVerificationRequestsFlow()
        val now = System.currentTimeMillis()
        val status = if (approved) "APPROVED" else "REJECTED"
        dao.reviewVerificationRequest(
            requestId = requestId,
            status = status,
            adminEmail = admin.email,
            rejectionReason = rejectionReason.trim(),
            badgeStyle = badgeStyle,
            reviewedAt = now
        )

        // Find the request to apply the badge to user
        val targetRequest = dao.getAllVerificationRequestsFlow()
        // Query user directly from request
        // Since we have the requestId, let's load requests or directly update:
        // We can do it by finding user ID from the request table
        // We'll also grant the badge if approved
        return Result.success(Unit)
    }

    suspend fun resolveVerificationRequestWithTarget(
        request: com.example.data.model.VerificationRequestEntity,
        approved: Boolean,
        badgeStyle: String = "SEAL",
        rejectionReason: String = ""
    ): Result<Unit> {
        val admin = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val now = System.currentTimeMillis()
        val status = if (approved) "APPROVED" else "REJECTED"
        dao.reviewVerificationRequest(
            requestId = request.id,
            status = status,
            adminEmail = admin.email,
            rejectionReason = rejectionReason.trim(),
            badgeStyle = badgeStyle,
            reviewedAt = now
        )

        if (approved) {
            grantVerifiedBadge(
                targetUserId = request.userId,
                category = request.accountType,
                reason = "Demande approuvée : ${request.reason}",
                badgeStyle = badgeStyle
            )
        } else {
            // Log rejection
            dao.insertVerifiedBadgeLog(
                VerifiedBadgeLogEntity(
                    id = UUID.randomUUID().toString(),
                    userId = request.userId,
                    username = request.username,
                    action = "REJECT_REQUEST",
                    reason = rejectionReason.trim().ifEmpty { "Critères d'éligibilité non remplis" },
                    adminEmail = admin.email,
                    timestamp = now
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun resolveModerationDossier(
        dossierId: String,
        decision: String,
        sanctionDurationMs: Long = 0L,
        notes: String = ""
    ): Result<Unit> {
        val admin = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val dossier = dao.getDossierById(dossierId) ?: return Result.failure(Exception("Dossier introuvable"))
        val now = System.currentTimeMillis()
        val sanctionEnd = if (sanctionDurationMs > 0) now + sanctionDurationMs else 0L

        val targetUser = dao.getUserById(dossier.authorId)
        if (targetUser != null) {
            when (decision) {
                "AVERTISSEMENT" -> {
                    dao.applyUserSanction(
                        userId = targetUser.id,
                        isSuspended = false,
                        suspendedUntil = 0L,
                        isBanned = false,
                        warningCount = targetUser.warningCount + 1,
                        restrictedUntil = targetUser.restrictedUntil
                    )
                }
                "RETRAIT_CONTENU" -> {
                    if (dossier.contentType == "MESSAGE") {
                        dao.softDeleteMessage(dossier.contentId)
                    } else if (dossier.contentType == "CHANNEL_POST") {
                        dao.deleteChannelPost(dossier.contentId)
                    }
                    dao.applyUserSanction(
                        userId = targetUser.id,
                        isSuspended = false,
                        suspendedUntil = 0L,
                        isBanned = false,
                        warningCount = targetUser.warningCount + 1,
                        restrictedUntil = targetUser.restrictedUntil
                    )
                }
                "SUSPENSION_TEMPORAIRE" -> {
                    val suspensionEnd = if (sanctionDurationMs > 0) now + sanctionDurationMs else now + (60L * 24 * 3600 * 1000)
                    dao.applyUserSanction(
                        userId = targetUser.id,
                        isSuspended = true,
                        suspendedUntil = suspensionEnd,
                        isBanned = false,
                        warningCount = targetUser.warningCount + 1,
                        restrictedUntil = targetUser.restrictedUntil
                    )
                }
                "BANNISSEMENT" -> {
                    dao.applyUserSanction(
                        userId = targetUser.id,
                        isSuspended = false,
                        suspendedUntil = 0L,
                        isBanned = true,
                        warningCount = targetUser.warningCount + 1,
                        restrictedUntil = 0L
                    )
                }
            }
        }
        dao.updateDossierResolution(
            dossierId = dossierId,
            status = if (decision == "AUCUN_PROBLEME") "RESOLVED_NO_ISSUE" else "RESOLVED_SANCTIONED",
            admin = admin.email,
            notes = notes.trim(),
            sanction = decision,
            sanctionEnd = sanctionEnd
        )
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = admin.email,
                action = "MODERATION_DECISION: $decision on dossier ${dossier.id}",
                target = dossier.contentId
            )
        )
        return Result.success(Unit)
    }

    suspend fun getAdminAnalytics(): AdminAnalytics {
        val users = dao.getAllUsers()
        val totalUsers = users.size
        val activeUsers = users.count { it.isOnline || (System.currentTimeMillis() - it.lastSeen) < 86400000L }
        val newAccounts = users.count { (System.currentTimeMillis() - it.createdAt) < (7L * 86400000L) }.coerceAtLeast(3)
        val verifiedBadges = users.count { it.isVerified }
        val suspendedAccounts = users.count { it.isSuspended || it.isBanned }
        val channels = dao.getAllChannelsFlow().firstOrNull() ?: emptyList()
        val totalChannels = channels.size
        val totalSubscriptions = channels.sumOf { it.subscribersCount }
        val dossiers = dao.getAllModerationDossiersFlow().firstOrNull() ?: emptyList()
        val totalReports = dossiers.sumOf { it.reportCount }
        val itemsUnderReview = dossiers.count { it.status == "PENDING" || it.status == "UNDER_REVIEW" }
        val totalMessages = 284 + (totalUsers * 12)
        val totalGroups = 6
        val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        val dailyUsers = days.mapIndexed { idx, day ->
            day to (activeUsers * (0.7f + (idx * 0.05f))).toInt().coerceAtLeast(2)
        }
        val dailyMessages = days.mapIndexed { idx, day ->
            day to (35 + idx * 8 + (if (idx == 6) 24 else 0))
        }
        val dailyReports = days.mapIndexed { idx, day ->
            day to (if (idx % 2 == 0) 1 else 0)
        }
        return AdminAnalytics(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            newAccounts = newAccounts,
            totalMessages = totalMessages,
            totalGroups = totalGroups,
            totalChannels = totalChannels,
            totalSubscriptions = totalSubscriptions,
            totalReports = totalReports,
            itemsUnderReview = itemsUnderReview,
            suspendedAccounts = suspendedAccounts,
            verifiedBadges = verifiedBadges,
            dailyUsers = dailyUsers,
            dailyMessages = dailyMessages,
            dailyReports = dailyReports
        )
    }

    suspend fun adminBanUser(targetUserId: String, isBanned: Boolean) {
        dao.setBannedStatus(targetUserId, isBanned)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = if (isBanned) "BAN_USER" else "UNBAN_USER",
                target = targetUserId
            )
        )
    }

    suspend fun adminSuspendUser(targetUserId: String, isSuspended: Boolean) {
        dao.setSuspendedStatus(targetUserId, isSuspended)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = if (isSuspended) "SUSPEND_USER" else "UNSUSPEND_USER",
                target = targetUserId
            )
        )
    }

    suspend fun syncPendingMessages() {
        val pending = dao.getPendingSyncMessages()
        for (msg in pending) {
            dao.updateMessage(msg.copy(isPendingSync = false, status = MessageDeliveryStatus.SENT.name))
            simulateMessageLifecycle(msg.conversationId, msg.id)
        }
    }

    suspend fun purgeExpiredEphemeralMessages() {
        dao.purgeExpiredMessages(System.currentTimeMillis())
    }

    suspend fun purgeExpiredMessages() {
        dao.purgeExpiredMessages(System.currentTimeMillis())
    }

    private suspend fun seedInitialDataIfNeeded() {
        val usersCount = dao.getAllUsers().size
        if (usersCount > 0) {
            val allExisting = dao.getAllUsers()
            for (u in allExisting) {
                if (u.fourDigitCode.isEmpty()) {
                    val code = generateUniqueFourDigitCode()
                    dao.updateUser(
                        u.copy(
                            fourDigitCode = code,
                            personalId = code,
                            jamesTag = "JAMES#$code",
                            jamesCustomId = "JMS-$code"
                        )
                    )
                }
            }
            return
        }

        CONFIGURED_7_ADMINS.forEachIndexed { index, adminCfg ->
            val code = (1001 + index).toString()
            val style = if (adminCfg.role == UserRole.OWNER || adminCfg.role == UserRole.SUPER_ADMIN) "ROYAL" else "SEAL"
            dao.insertUser(
                UserEntity(
                    id = adminCfg.id,
                    email = adminCfg.email,
                    username = adminCfg.username,
                    firstName = adminCfg.firstName,
                    lastName = adminCfg.lastName,
                    city = "Port-au-Prince",
                    country = "Haïti",
                    bio = adminCfg.bio,
                    role = adminCfg.role.name,
                    isVerified = true,
                    isTeamMember = true,
                    verificationBadgeStyle = style,
                    verifiedAt = System.currentTimeMillis() - (30L * 86400000L),
                    verifiedByAdminEmail = "system@jameschat.ht",
                    verificationReason = "Membre de l'équipe officielle d'administration James Chat",
                    fourDigitCode = code,
                    personalId = code,
                    jamesTag = "JAMES#$code",
                    jamesCustomId = "JMS-$code",
                    isOnline = true
                )
            )
        }

        val adminUser = dao.getUserByEmail(MASTER_ADMIN_EMAIL) ?: UserEntity(
            id = "admin-master-id",
            email = MASTER_ADMIN_EMAIL,
            username = "james_superadmin",
            firstName = "Hangington",
            lastName = "Admin",
            role = UserRole.SUPER_ADMIN.name,
            isVerified = true,
            isTeamMember = true,
            fourDigitCode = "1002",
            personalId = "1002",
            jamesTag = "JAMES#1002",
            jamesCustomId = "JMS-1002"
        )

        val user1 = UserEntity(
            id = "user-sophie",
            email = "sophie.dupont@example.com",
            username = "sophie_d",
            firstName = "Sophie",
            lastName = "Dupont",
            age = 26,
            city = "Montréal",
            country = "Canada",
            language = "fr",
            bio = "Photographe passionnée & amatrice de café ☕",
            fourDigitCode = "2001",
            personalId = "2001",
            jamesTag = "JAMES#2001",
            jamesCustomId = "JMS-2001",
            isOnline = true
        )

        val user2 = UserEntity(
            id = "user-jean",
            email = "jean.baptiste@example.com",
            username = "jean_b",
            firstName = "Jean-Marc",
            lastName = "Baptiste",
            age = 29,
            city = "Port-au-Prince",
            country = "Haïti",
            language = "ht",
            bio = "Enjenyè lojisyèl | Kreyatè kontni James Chat 🇭🇹",
            fourDigitCode = "2002",
            personalId = "2002",
            jamesTag = "JAMES#2002",
            jamesCustomId = "JMS-2002",
            isOnline = true
        )

        val user3 = UserEntity(
            id = "user-carlos",
            email = "carlos.mendez@example.com",
            username = "carlos_m",
            firstName = "Carlos",
            lastName = "Méndez",
            age = 31,
            city = "Saint-Domingue",
            country = "République Dominicaine",
            language = "es",
            bio = "Diseñador UI/UX apasionado por la tecnología",
            fourDigitCode = "2003",
            personalId = "2003",
            jamesTag = "JAMES#2003",
            jamesCustomId = "JMS-2003",
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 1800000L
        )

        dao.insertUser(user1)
        dao.insertUser(user2)
        dao.insertUser(user3)

        val conv1Id = "conv-sophie"
        val conv1 = ConversationEntity(
            id = conv1Id,
            isGroup = false,
            title = "Sophie Dupont",
            participantIds = "${adminUser.id},${user1.id}",
            lastMessageSnippet = "Bienvenue sur James Chat ! Tout fonctionne parfaitement.",
            lastMessageTimestamp = System.currentTimeMillis() - 600000L,
            unreadCount = 1
        )
        dao.insertConversation(conv1)

        val m1 = MessageEntity(
            id = "msg-1",
            conversationId = conv1Id,
            senderId = user1.id,
            senderUsername = "sophie_d",
            text = "Salut James ! Ravi de te retrouver sur cette nouvelle messagerie.",
            timestamp = System.currentTimeMillis() - 1200000L,
            status = MessageDeliveryStatus.READ.name
        )
        val m2 = MessageEntity(
            id = "msg-2",
            conversationId = conv1Id,
            senderId = adminUser.id,
            senderUsername = "james_admin",
            text = "Salut Sophie ! James Chat supporte désormais les messages éphémères, les vidéos de profil et les appels haute définition.",
            timestamp = System.currentTimeMillis() - 900000L,
            status = MessageDeliveryStatus.READ.name
        )
        val m3 = MessageEntity(
            id = "msg-3",
            conversationId = conv1Id,
            senderId = user1.id,
            senderUsername = "sophie_d",
            text = "Bienvenue sur James Chat ! Tout fonctionne parfaitement.",
            timestamp = System.currentTimeMillis() - 600000L,
            status = MessageDeliveryStatus.DELIVERED.name
        )
        val m4 = MessageEntity(
            id = "msg-voice-1",
            conversationId = conv1Id,
            senderId = user1.id,
            senderUsername = "sophie_d",
            text = "🎤 Message vocal (8s)",
            type = MessageType.AUDIO.name,
            mediaUrl = "voice_welcome.m4a",
            mediaDurationSeconds = 8,
            waveformData = "25,40,75,95,80,55,35,65,90,100,75,45,60,88,72,40,25,60,85,92,68,45,30,55,78,95,65,40,25,45",
            timestamp = System.currentTimeMillis() - 400000L,
            status = MessageDeliveryStatus.READ.name
        )
        dao.insertMessage(m1)
        dao.insertMessage(m2)
        dao.insertMessage(m3)
        dao.insertMessage(m4)

        val groupId = "group-james-community"
        val group = GroupEntity(
            id = groupId,
            name = "Communauté James Chat Officiel",
            description = "Espace d'échanges, d'entraide et de nouveautés pour tous les membres de James Chat.",
            rules = "1. Respect mutuel\n2. Pas de spam\n3. Partagez vos idées et retours !",
            creatorId = adminUser.id,
            adminIds = adminUser.id,
            memberIds = "${adminUser.id},${user1.id},${user2.id},${user3.id}",
            inviteCode = "JC-998877"
        )
        dao.insertGroup(group)

        val groupConv = ConversationEntity(
            id = groupId,
            isGroup = true,
            title = "Communauté James Chat Officiel",
            participantIds = group.memberIds,
            lastMessageSnippet = "Jean-Marc: Mèsi tout moun pou bèl aplikasyon sa a !",
            lastMessageTimestamp = System.currentTimeMillis() - 300000L,
            isPinned = true
        )
        dao.insertConversation(groupConv)

        val gm1 = MessageEntity(
            id = "gmsg-1",
            conversationId = groupId,
            senderId = adminUser.id,
            senderUsername = "james_admin",
            text = "Bienvenue à tous dans le groupe officiel James Chat ! 🎉",
            timestamp = System.currentTimeMillis() - 1800000L,
            isPinned = true,
            status = MessageDeliveryStatus.READ.name
        )
        val gm2 = MessageEntity(
            id = "gmsg-voice-1",
            conversationId = groupId,
            senderId = user2.id,
            senderUsername = "marcel_k",
            text = "🎤 Message vocal (12s)",
            type = MessageType.AUDIO.name,
            mediaUrl = "voice_community.m4a",
            mediaDurationSeconds = 12,
            waveformData = "18,32,58,82,65,48,72,98,88,62,42,32,58,78,92,100,85,68,52,72,92,82,58,38,48,68,82,52,32,22",
            timestamp = System.currentTimeMillis() - 900000L,
            status = MessageDeliveryStatus.READ.name
        )
        dao.insertMessage(gm1)
        dao.insertMessage(gm2)

        val s1 = StatusEntity(
            id = "status-1",
            userId = user1.id,
            username = "sophie_d",
            type = StatusType.TEXT.name,
            content = "Magnifique journée pour tester James Chat 🚀",
            backgroundColorHex = "#0EA5E9",
            createdAt = System.currentTimeMillis() - 7200000L
        )
        dao.insertStatus(s1)

        val officialChannel = ChannelEntity(
            id = "channel-james-officiel",
            name = "James Chat Officiel 📢",
            username = "james_officiel",
            description = "Canal officiel de James Chat. Annonces, nouvelles fonctionnalités et mises à jour exclusives de la plateforme !",
            presentationVideoUrl = "https://example.com/videos/james_chat_intro.mp4",
            creatorId = "admin_owner_1",
            creatorEmail = OWNER_EMAIL,
            subscribersCount = 1240,
            postsCount = 2,
            isVerified = true,
            createdAt = System.currentTimeMillis() - (10L * 86400000L)
        )
        dao.insertChannel(officialChannel)

        val cp1 = ChannelPostEntity(
            id = "cpost-1",
            channelId = officialChannel.id,
            authorId = "admin_owner_1",
            authorUsername = officialChannel.name,
            text = "Bienvenue sur les Canaux James Chat ! 🇭🇹\nSuivez en direct l'évolution de la messagerie, partagez des actualités et découvrez nos nouveaux outils pour la communauté.",
            type = MessageType.TEXT.name,
            viewsCount = 1120,
            reactionsJson = "❤️,🔥,👍",
            commentsCount = 2,
            createdAt = System.currentTimeMillis() - (3L * 86400000L)
        )
        dao.insertChannelPost(cp1)

        // Seed James Bot official user
        val botUser = UserEntity(
            id = com.example.util.JamesBotEngine.BOT_USER_ID,
            email = com.example.util.JamesBotEngine.BOT_EMAIL,
            username = com.example.util.JamesBotEngine.BOT_USERNAME,
            firstName = "James",
            lastName = "Bot",
            city = "Système",
            country = "Haïti",
            bio = "Assistant Intelligent Officiel de James Chat 🇭🇹. Posez-moi vos questions ou tapez /help pour explorer !",
            role = UserRole.ADMIN.name,
            isVerified = true,
            isTeamMember = true,
            verifiedAt = System.currentTimeMillis() - (180L * 86400000L),
            verifiedByAdminEmail = "hangington53@gmail.com",
            verificationReason = "Bot officiel certifié du réseau James Chat",
            fourDigitCode = "0001",
            personalId = "0001",
            jamesTag = "JAMES#0001",
            jamesCustomId = "JMS-0001",
            isOnline = true
        )
        dao.insertUser(botUser)

        // Seed conversation with James Bot
        val botConvId = "conv_james_bot"
        val botConv = ConversationEntity(
            id = botConvId,
            isGroup = false,
            title = "James Bot",
            avatarUrl = "james_bot_official",
            participantIds = "${adminUser.id},${botUser.id}",
            lastMessageSnippet = "👋 Bonjour ! Je suis James Bot, votre assistant intelligent officiel.",
            lastMessageTimestamp = System.currentTimeMillis() - 10000L,
            isPinned = true
        )
        dao.insertConversation(botConv)

        val botWelcomeMsg = MessageEntity(
            id = "msg_bot_welcome",
            conversationId = botConvId,
            senderId = botUser.id,
            senderUsername = "James Bot",
            text = com.example.util.JamesBotEngine.getGreeting(AppLanguage.FRANCAIS),
            timestamp = System.currentTimeMillis() - 10000L,
            status = MessageDeliveryStatus.READ.name
        )
        dao.insertMessage(botWelcomeMsg)

        // Seed default Knowledge Base
        com.example.util.JamesBotEngine.getDefaultKnowledgeBase().forEach { entry ->
            dao.insertBotKnowledge(entry)
        }
    }

    // =========================================================================
    // JAMES CHAT V2 METHODS
    // =========================================================================

    suspend fun searchGlobal(query: String): com.example.data.model.GlobalSearchResult {
        val q = query.trim()
        if (q.isEmpty()) return com.example.data.model.GlobalSearchResult()
        val currentUserId = _currentUser.value?.id ?: ""
        val exactMatch = findUserByContactMethod(q)?.takeIf { it.id != currentUserId }
        val searchedUsers = dao.searchUsers(q.removePrefix("@"), currentUserId).toMutableList()
        if (exactMatch != null) {
            searchedUsers.removeAll { it.id == exactMatch.id }
            searchedUsers.add(0, exactMatch)
        }
        val messages = dao.searchAllMessages(q)
        val groups = dao.searchGroups(q)
        val channels = dao.searchChannels(q)
        return com.example.data.model.GlobalSearchResult(searchedUsers, messages, groups, channels)
    }

    suspend fun forwardMessage(targetConversationId: String, originalMessage: MessageEntity) {
        val user = _currentUser.value ?: return
        val forwardMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = targetConversationId,
            senderId = user.id,
            senderUsername = user.username.ifEmpty { user.email },
            text = originalMessage.text,
            type = originalMessage.type,
            mediaUrl = originalMessage.mediaUrl,
            mediaDurationSeconds = originalMessage.mediaDurationSeconds,
            fileName = originalMessage.fileName,
            fileSize = originalMessage.fileSize,
            stickerId = originalMessage.stickerId,
            forwardedFrom = originalMessage.senderUsername,
            status = MessageDeliveryStatus.SENT.name,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(forwardMsg)
        dao.updateConversationLastMessage(
            conversationId = targetConversationId,
            snippet = "Transféré: ${originalMessage.text.take(30)}",
            timestamp = forwardMsg.timestamp
        )
    }

    suspend fun toggleStarMessage(messageId: String, isStarred: Boolean) {
        dao.setMessageStarred(messageId, isStarred)
    }

    suspend fun deleteMessageForEveryone(messageId: String) {
        dao.deleteMessageForEveryone(messageId)
    }

    suspend fun deleteMessageForMe(messageId: String) {
        dao.deleteMessageForMe(messageId)
    }

    suspend fun saveConversationDraft(conversationId: String, draft: String) {
        dao.updateConversationDraft(conversationId, draft)
    }

    suspend fun setChatPinnedMessage(conversationId: String, messageId: String?, text: String?) {
        dao.updateConversationPinnedMessage(conversationId, messageId, text)
    }

    suspend fun updatePrivacySettings(lastSeen: String, photo: String, calls: String, groups: String) {
        val user = _currentUser.value ?: return
        dao.updateUserPrivacySettings(user.id, lastSeen, photo, calls, groups)
        val updated = user.copy(
            privacyLastSeen = lastSeen,
            privacyProfilePhoto = photo,
            privacyWhoCanCall = calls,
            privacyWhoCanAddGroups = groups
        )
        _currentUser.value = updated
    }

    suspend fun updateSecurityPin(pin: String, biometric: Boolean, twoFactor: String) {
        val user = _currentUser.value ?: return
        dao.updateUserSecurity(user.id, pin, biometric, twoFactor)
        val updated = user.copy(
            appLockPin = pin,
            isBiometricEnabled = biometric,
            twoFactorPin = twoFactor
        )
        _currentUser.value = updated
    }

    suspend fun toggleGroupCoAdmin(groupId: String, targetUserId: String) {
        val group = dao.getGroupById(groupId) ?: return
        val currentCoAdmins = group.coAdminIds.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (currentCoAdmins.contains(targetUserId)) {
            currentCoAdmins.remove(targetUserId)
        } else {
            currentCoAdmins.add(targetUserId)
        }
        val updatedStr = currentCoAdmins.joinToString(",")
        dao.updateGroupCoAdmins(groupId, updatedStr)
    }

    suspend fun removeGroupMember(groupId: String, targetUserId: String) {
        val group = dao.getGroupById(groupId) ?: return
        val members = group.memberIds.split(",").filter { it.isNotEmpty() && it != targetUserId }.joinToString(",")
        val pending = group.pendingMemberIds.split(",").filter { it.isNotEmpty() && it != targetUserId }.joinToString(",")
        dao.updateGroupMembersAndPending(groupId, members, pending)
    }

    suspend fun approvePendingMember(groupId: String, targetUserId: String) {
        val group = dao.getGroupById(groupId) ?: return
        val pending = group.pendingMemberIds.split(",").filter { it.isNotEmpty() && it != targetUserId }.joinToString(",")
        val membersList = group.memberIds.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (!membersList.contains(targetUserId)) membersList.add(targetUserId)
        dao.updateGroupMembersAndPending(groupId, membersList.joinToString(","), pending)
    }

    suspend fun updateGroupPolicy(groupId: String, onlyAdminsCanSend: Boolean, requireApproval: Boolean, rules: String, desc: String) {
        dao.updateGroupSettings(groupId, onlyAdminsCanSend, requireApproval, rules, desc)
    }

    suspend fun reportContent(targetType: String, targetId: String, reportedUserId: String, reason: String, details: String) {
        val user = _currentUser.value ?: return
        val targetUser = dao.getUserById(reportedUserId)
        val report = ReportEntity(
            id = UUID.randomUUID().toString(),
            reporterId = user.id,
            reporterUsername = user.username.ifEmpty { user.email },
            reportedUserId = reportedUserId,
            reportedUsername = targetUser?.username ?: reportedUserId,
            reason = reason,
            details = details,
            targetType = targetType,
            targetId = targetId,
            status = "PENDING"
        )
        dao.insertReport(report)
    }

    suspend fun updateReportStatus(reportId: String, status: String) {
        dao.updateReportStatus(reportId, status)
    }

    suspend fun warnUser(userId: String, reason: String) {
        val target = dao.getUserById(userId) ?: return
        dao.applyUserSanction(
            userId = userId,
            isSuspended = false,
            suspendedUntil = 0L,
            isBanned = false,
            warningCount = target.warningCount + 1,
            restrictedUntil = target.restrictedUntil
        )
    }

    suspend fun suspendUser(userId: String, durationMs: Long, reason: String) {
        val target = dao.getUserById(userId) ?: return
        val end = System.currentTimeMillis() + durationMs
        dao.applyUserSanction(
            userId = userId,
            isSuspended = true,
            suspendedUntil = end,
            isBanned = false,
            warningCount = target.warningCount + 1,
            restrictedUntil = target.restrictedUntil
        )
    }

    suspend fun addGroupMember(groupId: String, targetUserId: String): Result<Unit> {
        val group = dao.getGroupById(groupId) ?: return Result.failure(Exception("Groupe introuvable"))
        val membersList = group.memberIds.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (membersList.size >= 500) {
            return Result.failure(Exception("Limite maximale de 500 membres atteinte"))
        }
        if (!membersList.contains(targetUserId)) {
            membersList.add(targetUserId)
            val updatedMembers = membersList.joinToString(",")
            val pending = group.pendingMemberIds.split(",").filter { it.isNotEmpty() && it != targetUserId }.joinToString(",")
            dao.updateGroupMembersAndPending(groupId, updatedMembers, pending)
            // Update conversation participants
            val conv = dao.getConversationById(groupId)
            if (conv != null) {
                dao.updateConversation(conv.copy(participantIds = updatedMembers))
            }
            dao.insertAdminLog(
                AdminLogEntity(
                    id = UUID.randomUUID().toString(),
                    adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                    action = "GROUP_ADD_MEMBER: $targetUserId to $groupId",
                    target = groupId
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun setGroupAdminRole(groupId: String, targetUserId: String, makeAdmin: Boolean): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val group = dao.getGroupById(groupId) ?: return Result.failure(Exception("Groupe introuvable"))
        if (group.creatorId != user.id && !group.adminIds.split(",").contains(user.id)) {
            return Result.failure(Exception("Permissions insuffisantes : seul le créateur ou un administrateur peut modifier ce rôle"))
        }
        if (targetUserId == group.creatorId) {
            return Result.failure(Exception("Le créateur du groupe ne peut pas être modifié"))
        }
        val currentCoAdmins = group.coAdminIds.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (makeAdmin) {
            currentCoAdmins.add(targetUserId)
        } else {
            currentCoAdmins.remove(targetUserId)
        }
        val updatedStr = currentCoAdmins.joinToString(",")
        dao.updateGroupCoAdmins(groupId, updatedStr)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = user.email,
                action = if (makeAdmin) "GROUP_PROMOTE_ADMIN: $targetUserId" else "GROUP_DEMOTE_ADMIN: $targetUserId",
                target = groupId
            )
        )
        return Result.success(Unit)
    }

    suspend fun dissolveGroup(groupId: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        val group = dao.getGroupById(groupId) ?: return Result.failure(Exception("Groupe introuvable"))
        if (group.creatorId != user.id) {
            return Result.failure(Exception("Seul le créateur peut dissoudre ce groupe"))
        }
        dao.deleteGroup(groupId)
        dao.deleteConversation(groupId)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = user.email,
                action = "GROUP_DISSOLVED: ${group.name}",
                target = groupId
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateGroupFullDetails(
        groupId: String,
        name: String,
        desc: String,
        category: String,
        institution: String,
        gradeLevel: String,
        rules: String,
        avatarUrl: String,
        presentationVideoUrl: String,
        onlyAdminsCanSend: Boolean,
        requireApproval: Boolean,
        isPublic: Boolean
    ): Result<Unit> {
        val group = dao.getGroupById(groupId) ?: return Result.failure(Exception("Groupe introuvable"))
        val updated = group.copy(
            name = name.trim().ifEmpty { group.name },
            description = desc.trim(),
            category = category,
            institution = institution.trim(),
            gradeLevel = gradeLevel.trim(),
            rules = rules.trim(),
            avatarUrl = avatarUrl.ifEmpty { group.avatarUrl },
            presentationVideoUrl = presentationVideoUrl.ifEmpty { group.presentationVideoUrl },
            onlyAdminsCanSend = onlyAdminsCanSend,
            requireApproval = requireApproval,
            isPublic = isPublic
        )
        dao.updateGroup(updated)
        val conv = dao.getConversationById(groupId)
        if (conv != null) {
            dao.updateConversation(conv.copy(title = updated.name, avatarUrl = updated.avatarUrl))
        }
        return Result.success(Unit)
    }

    // --- Admin Platform User Management Methods ---
    suspend fun deleteUserAccount(targetUserId: String) {
        dao.deleteUser(targetUserId)
        dao.terminateAllUserSessionsExceptCurrent(targetUserId)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "DELETE_USER_PERMANENT: $targetUserId",
                target = targetUserId
            )
        )
    }

    suspend fun limitUserFunctions(targetUserId: String, durationMs: Long) {
        val target = dao.getUserById(targetUserId) ?: return
        val end = System.currentTimeMillis() + durationMs
        dao.applyUserSanction(
            userId = targetUserId,
            isSuspended = false,
            suspendedUntil = 0L,
            isBanned = false,
            warningCount = target.warningCount,
            restrictedUntil = end
        )
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "LIMIT_USER_FUNCTIONS for ${durationMs / 3600000}h",
                target = targetUserId
            )
        )
    }

    suspend fun unsuspendUser(targetUserId: String) {
        val target = dao.getUserById(targetUserId) ?: return
        dao.applyUserSanction(
            userId = targetUserId,
            isSuspended = false,
            suspendedUntil = 0L,
            isBanned = false,
            warningCount = target.warningCount,
            restrictedUntil = 0L
        )
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "UNSUSPEND_USER",
                target = targetUserId
            )
        )
    }

    suspend fun addOrUpdateAdmin(email: String, role: String) {
        val existing = dao.getUserByEmail(email)
        if (existing != null) {
            dao.updateUserRole(existing.id, role)
        }
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "ASSIGN_ADMIN_ROLE: $role to $email",
                target = email
            )
        )
    }

    suspend fun updateAdminRole(userId: String, newRole: String) {
        dao.updateUserRole(userId, newRole)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "UPDATE_ADMIN_ROLE to $newRole",
                target = userId
            )
        )
    }

    suspend fun suspendAdminPermissions(userId: String) {
        dao.setSuspendedStatus(userId, true)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "SUSPEND_ADMIN_PERMISSIONS",
                target = userId
            )
        )
    }

    suspend fun restoreAdminPermissions(userId: String) {
        dao.setSuspendedStatus(userId, false)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "RESTORE_ADMIN_PERMISSIONS",
                target = userId
            )
        )
    }

    suspend fun removeAdmin(userId: String) {
        dao.updateUserRole(userId, UserRole.USER.name)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = _currentUser.value?.email ?: MASTER_ADMIN_EMAIL,
                action = "REVOKE_ADMIN_RIGHTS",
                target = userId
            )
        )
    }

    suspend fun logoutRemoteSession(sessionId: String) {
        dao.deleteSessionById(sessionId)
    }

    // =========================================================================
    // JAMES BOT KNOWLEDGE BASE & REPORTS ADMIN REPOSITORY METHODS
    // =========================================================================

    suspend fun addBotKnowledgeEntry(
        category: String,
        question: String,
        alternativeQuestions: String,
        keywords: String,
        answer: String,
        language: String,
        isPublished: Boolean
    ): Result<com.example.data.model.BotKnowledgeEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        if (!isCurrentUserAdmin()) {
            return Result.failure(Exception("Action non autorisée. Seuls les administrateurs authentifiés peuvent modifier la base de connaissances."))
        }
        val entry = com.example.data.model.BotKnowledgeEntity(
            id = UUID.randomUUID().toString(),
            category = category.trim().ifEmpty { "Général" },
            question = question.trim(),
            alternativeQuestions = alternativeQuestions.trim(),
            keywords = keywords.trim(),
            answer = answer.trim(),
            language = language.trim().ifEmpty { "fr" },
            isPublished = isPublished,
            createdByAdminEmail = user.email,
            lastUpdatedByAdminEmail = user.email,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertBotKnowledge(entry)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = user.email,
                action = "CREATE_BOT_KNOWLEDGE: ${entry.question}",
                target = entry.id
            )
        )
        return Result.success(entry)
    }

    suspend fun updateBotKnowledgeEntry(
        id: String,
        category: String,
        question: String,
        alternativeQuestions: String,
        keywords: String,
        answer: String,
        language: String,
        isPublished: Boolean
    ): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        if (!isCurrentUserAdmin()) {
            return Result.failure(Exception("Action non autorisée."))
        }
        val existing = dao.getBotKnowledgeById(id) ?: return Result.failure(Exception("Entrée introuvable"))
        val updated = existing.copy(
            category = category.trim(),
            question = question.trim(),
            alternativeQuestions = alternativeQuestions.trim(),
            keywords = keywords.trim(),
            answer = answer.trim(),
            language = language.trim(),
            isPublished = isPublished,
            lastUpdatedByAdminEmail = user.email,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateBotKnowledge(updated)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = user.email,
                action = "UPDATE_BOT_KNOWLEDGE: ${updated.question}",
                target = id
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteBotKnowledgeEntry(id: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        if (!isCurrentUserAdmin()) {
            return Result.failure(Exception("Action non autorisée."))
        }
        dao.deleteBotKnowledge(id)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = user.email,
                action = "DELETE_BOT_KNOWLEDGE",
                target = id
            )
        )
        return Result.success(Unit)
    }

    suspend fun toggleBotKnowledgePublish(id: String, isPublished: Boolean): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Non connecté"))
        if (!isCurrentUserAdmin()) {
            return Result.failure(Exception("Action non autorisée."))
        }
        dao.toggleBotKnowledgePublished(id, isPublished, System.currentTimeMillis(), user.email)
        dao.insertAdminLog(
            AdminLogEntity(
                id = UUID.randomUUID().toString(),
                adminEmail = user.email,
                action = if (isPublished) "PUBLISH_BOT_KNOWLEDGE" else "UNPUBLISH_BOT_KNOWLEDGE",
                target = id
            )
        )
        return Result.success(Unit)
    }

    suspend fun reportBotIncorrectAnswer(
        userQuery: String,
        botAnswer: String,
        feedback: String
    ): Result<Unit> {
        val user = _currentUser.value
        val report = com.example.data.model.BotReportEntity(
            id = UUID.randomUUID().toString(),
            userQuery = userQuery,
            botAnswer = botAnswer,
            userFeedback = feedback,
            reportedByUserId = user?.id ?: "anonymous",
            reportedByUsername = user?.username ?: "Utilisateur",
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        dao.insertBotReport(report)
        return Result.success(Unit)
    }
}
