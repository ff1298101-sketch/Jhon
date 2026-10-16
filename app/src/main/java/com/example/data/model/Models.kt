package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String,
    val isFullyTranslated: Boolean = false
) {
    FRANCAIS("fr", "Français", "🇫🇷", true),
    ENGLISH("en", "English", "🇺🇸", true),
    KREYOL("ht", "Kreyòl Ayisyen", "🇭🇹", true),
    ESPANOL("es", "Español", "🇪🇸", true),
    PORTUGUES("pt", "Português", "🇧🇷", true),
    DEUTSCH("de", "Deutsch", "🇩🇪", true),
    ITALIANO("it", "Italiano", "🇮🇹", true),
    RUSSIAN("ru", "Русский", "🇷🇺", true),
    CHINESE_SIMPLIFIED("zh", "简体中文", "🇨🇳", true),
    CHINESE_TRADITIONAL("zh-TW", "繁體中文", "🇹🇼", true),
    JAPANESE("ja", "日本語", "🇯🇵", true),
    KOREAN("ko", "한국어", "🇰🇷", true),
    ARABIC("ar", "العربية", "🇸🇦", true),
    HINDI("hi", "हिन्दी", "🇮🇳", true),
    BENGALI("bn", "বাংলা", "🇧🇩", false),
    TURKISH("tr", "Türkçe", "🇹🇷", false),
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳", false),
    INDONESIAN("id", "Bahasa Indonesia", "🇮🇩", false),
    POLISH("pl", "Polski", "🇵🇱", false),
    DUTCH("nl", "Nederlands", "🇳🇱", false),
    PERSIAN("fa", "فارسی", "🇮🇷", false),
    SWEDISH("sv", "Svenska", "🇸🇪", false),
    GREEK("el", "Ελληνικά", "🇬🇷", false),
    HEBREW("he", "עברית", "🇮🇱", false),
    THAI("th", "ไทย", "🇹🇭", false),
    CZECH("cs", "Čeština", "🇨🇿", false),
    ROMANIAN("ro", "Română", "🇷🇴", false),
    HUNGARIAN("hu", "Magyar", "🇭🇺", false),
    UKRAINIAN("uk", "Українська", "🇺🇦", false),
    DANISH("da", "Dansk", "🇩🇰", false),
    FINNISH("fi", "Suomi", "🇫🇮", false),
    NORWEGIAN("no", "Norsk", "🇳🇴", false),
    FILIPINO("tl", "Filipino", "🇵🇭", false),
    SWAHILI("sw", "Kiswahili", "🇰🇪", false),
    MALAY("ms", "Bahasa Melayu", "🇲🇾", false),
    YORUBA("yo", "Èdè Yorùbá", "🇳🇬", false),
    IGBO("ig", "Asụsụ Igbo", "🇳🇬", false),
    HAUSA("ha", "Harshen Hausa", "🇳🇬", false),
    AMHARIC("am", "አማርኛ", "🇪🇹", false),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "🇮🇳", false),
    TAMIL("ta", "தமிழ்", "🇮🇳", false),
    TELUGU("te", "తెలుగు", "🇮🇳", false),
    MARATHI("mr", "मराठी", "🇮🇳", false),
    URDU("ur", "اردو", "🇵🇰", false),
    GUJARATI("gu", "ગુજરાતી", "🇮🇳", false),
    MALAYALAM("ml", "മലയാളം", "🇮🇳", false),
    KANNADA("kn", "ಕನ್ನಡ", "🇮🇳", false),
    BURMESE("my", "မြန်မာဘာသာ", "🇲🇲", false),
    KHMER("km", "ភាសាខ្មែរ", "🇰🇭", false),
    SOMALI("so", "Af Soomaali", "🇸🇴", false)
}

enum class UserRole {
    USER,
    SUPPORT,
    MODERATOR,
    SENIOR_MODERATOR,
    ADMIN,
    SUPER_ADMIN,
    OWNER
}

enum class AdminPermission {
    VIEW_ADMIN_STATS,
    MANAGE_ROLES,
    MANAGE_V_BADGES,
    REVIEW_MODERATION_DOSSIERS,
    APPLY_WARNING,
    APPLY_CONTENT_REMOVAL,
    APPLY_SUSPENSION,
    APPLY_BAN,
    MANAGE_CHANNELS_GLOBAL,
    HANDLE_SUPPORT_TICKETS
}

enum class MessageType {
    TEXT,
    PHOTO,
    VIDEO,
    AUDIO,
    DOCUMENT,
    STICKER
}

enum class MessageDeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ
}

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED
}

enum class CallState {
    DIALING,
    RINGING,
    CONNECTED,
    ENDED
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

enum class StatusType {
    TEXT,
    PHOTO,
    VIDEO,
    MUSIC
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val username: String,
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val city: String = "",
    val country: String = "",
    val language: String = "fr",
    val bio: String = "",
    val avatarUrl: String = "",
    val profileVideoUrl: String = "",
    val showVideoOnProfile: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val isSuspended: Boolean = false,
    val suspendedUntil: Long = 0L,
    val isBanned: Boolean = false,
    val isVerified: Boolean = false,
    val verifiedAt: Long = 0L,
    val verifiedByAdminEmail: String = "",
    val verificationReason: String = "",
    val verificationBadgeStyle: String = "SEAL",
    val isTeamMember: Boolean = false,
    val warningCount: Int = 0,
    val restrictedUntil: Long = 0L,
    val role: String = UserRole.USER.name,
    val personalId: String = "",
    val fourDigitCode: String = "",
    val jamesTag: String = "",
    val jamesCustomId: String = "",
    val privacyLastSeen: String = "EVERYONE",
    val privacyProfilePhoto: String = "EVERYONE",
    val privacyWhoCanCall: String = "EVERYONE",
    val privacyWhoCanAddGroups: String = "EVERYONE",
    val appLockPin: String = "",
    val isBiometricEnabled: Boolean = false,
    val twoFactorPin: String = "",
    val profilePhotosJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val contactUserId: String,
    val customSound: String = "Default",
    val isBlocked: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val isGroup: Boolean = false,
    val title: String,
    val avatarUrl: String = "",
    val participantIds: String,
    val lastMessageSnippet: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val ephemeralDurationMs: Long = 0L,
    val isPinned: Boolean = false,
    val wallpaperKey: String = "default",
    val draftText: String = "",
    val pinnedMessageId: String? = null,
    val pinnedMessageText: String? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderUsername: String,
    val text: String,
    val type: String = MessageType.TEXT.name,
    val mediaUrl: String = "",
    val mediaDurationSeconds: Int = 0,
    val waveformData: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val isStarred: Boolean = false,
    val deletedForEveryone: Boolean = false,
    val forwardedFrom: String = "",
    val status: String = MessageDeliveryStatus.SENT.name,
    val stickerId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = 0L,
    val isPendingSync: Boolean = false
)

@Entity(tableName = "reactions")
data class ReactionEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val userId: String,
    val username: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarUrl: String = "",
    val presentationVideoUrl: String = "",
    val description: String = "",
    val category: String = "Groupes d'amis",
    val institution: String = "",
    val gradeLevel: String = "",
    val rules: String = "",
    val creatorId: String,
    val adminIds: String,
    val coAdminIds: String = "",
    val memberIds: String,
    val pendingMemberIds: String = "",
    val inviteCode: String,
    val onlyAdminsCanSend: Boolean = false,
    val requireApproval: Boolean = false,
    val isPublic: Boolean = true,
    val isMuted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String = "",
    val type: String = StatusType.TEXT.name,
    val content: String,
    val caption: String = "",
    val backgroundColorHex: String = "#1E3A8A",
    val musicTitle: String = "",
    val viewersCount: Int = 0,
    val viewersJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L
)

@Entity(tableName = "call_records")
data class CallRecordEntity(
    @PrimaryKey val id: String,
    val callerId: String,
    val callerName: String,
    val callerAvatar: String = "",
    val receiverId: String,
    val receiverName: String,
    val receiverAvatar: String = "",
    val isVideo: Boolean = false,
    val callType: String = CallType.OUTGOING.name,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val reporterUsername: String,
    val reportedUserId: String,
    val reportedUsername: String,
    val reason: String,
    val details: String = "",
    val status: String = "PENDING",
    val targetType: String = "USER",
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class GlobalSearchResult(
    val users: List<UserEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val channels: List<ChannelEntity> = emptyList()
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val deviceName: String,
    val deviceType: String,
    val ipAddress: String,
    val location: String,
    val lastActive: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey val id: String,
    val adminEmail: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sticker_favorites")
data class StickerFavoriteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val stickerId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sticker_recents")
data class StickerRecentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val stickerId: String,
    val lastUsedAt: Long = System.currentTimeMillis()
)

data class StickerItem(
    val id: String,
    val category: String,
    val name: String,
    val symbol: String,
    val keywords: List<String>
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String = "",
    val description: String = "",
    val presentationVideoUrl: String = "",
    val creatorId: String,
    val creatorEmail: String,
    val subscribersCount: Int = 0,
    val postsCount: Int = 0,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_posts")
data class ChannelPostEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String = "",
    val text: String,
    val type: String = MessageType.TEXT.name,
    val mediaUrl: String = "",
    val stickerId: String = "",
    val viewsCount: Int = 1,
    val likesCount: Int = 0,
    val reactionsJson: String = "{}",
    val commentsCount: Int = 0,
    val allowComments: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_subscriptions")
data class ChannelSubscriptionEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val userId: String,
    val notificationsEnabled: Boolean = true,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_comments")
data class ChannelCommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val userAvatar: String = "",
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "verified_badge_logs")
data class VerifiedBadgeLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val userPhoto: String = "",
    val action: String,
    val adminEmail: String,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "moderation_dossiers")
data class ModerationDossierEntity(
    @PrimaryKey val id: String,
    val contentId: String,
    val contentType: String,
    val authorId: String,
    val authorUsername: String,
    val contentPreview: String,
    val mediaUrl: String = "",
    val reportCount: Int = 1,
    val reason: String = "",
    val details: String = "",
    val status: String = "PENDING",
    val firstReportedAt: Long = System.currentTimeMillis(),
    val lastReportedAt: Long = System.currentTimeMillis(),
    val reviewedByAdmin: String = "",
    val decisionNotes: String = "",
    val sanctionType: String = "",
    val sanctionEndTimestamp: Long = 0L,
    val historyJson: String = "[]"
)

@Entity(tableName = "bot_knowledge")
data class BotKnowledgeEntity(
    @PrimaryKey val id: String,
    val category: String, // "Général", "Fonctionnalités", "Sécurité", "Règles", "Compte", "Support"
    val question: String,
    val alternativeQuestions: String = "", // Comma-separated alternative formulations
    val keywords: String = "", // Comma-separated keywords
    val answer: String,
    val language: String = "fr", // fr, ht, en, es
    val isPublished: Boolean = true,
    val createdByAdminEmail: String = "",
    val lastUpdatedByAdminEmail: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bot_reports")
data class BotReportEntity(
    @PrimaryKey val id: String,
    val userQuery: String,
    val botAnswer: String,
    val userFeedback: String,
    val reportedByUserId: String,
    val reportedByUsername: String,
    val status: String = "PENDING", // PENDING, RESOLVED, DISMISSED
    val reviewedByAdmin: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class VBadgeStyle(
    val code: String,
    val title: String,
    val description: String,
    val isPrimary: Boolean
) {
    CLASSIC("CLASSIC", "V Classique", "Cercle bleu avec coche blanche", false),
    SEAL("SEAL", "V Sceau", "Badge en forme de sceau officiel avec coche", true),
    ROYAL("ROYAL", "V Royal", "Bouclier bleu royal prestigieux avec coche", true),
    DIAMOND("DIAMOND", "V Diamant", "Forme diamant élégante avec coche", false),
    HEXAGON("HEXAGON", "V Hexagone", "Hexagone moderne technologique avec coche", false);

    companion object {
        fun fromCode(code: String): VBadgeStyle {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: SEAL
        }
    }
}

@Entity(tableName = "verification_requests")
data class VerificationRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val accountName: String,
    val username: String,
    val accountType: String, // "Créateur", "Personnalité publique", "Entreprise", "Communauté", "Autre"
    val reason: String,
    val additionalInfo: String = "",
    val preferredStyle: String = VBadgeStyle.SEAL.code, // SEAL or ROYAL
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val reviewedByAdminEmail: String = "",
    val rejectionReason: String = "",
    val assignedBadgeStyle: String = VBadgeStyle.SEAL.code,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L
)

@Entity(tableName = "verification_badges")
data class VerificationBadgeEntity(
    @PrimaryKey val userId: String,
    val badgeStyle: String = VBadgeStyle.SEAL.code,
    val verified: Boolean = true,
    val grantedBy: String = "",
    val reason: String = "",
    val grantedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
