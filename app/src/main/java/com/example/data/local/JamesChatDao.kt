package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdminLogEntity
import com.example.data.model.CallRecordEntity
import com.example.data.model.ChannelCommentEntity
import com.example.data.model.ChannelEntity
import com.example.data.model.ChannelPostEntity
import com.example.data.model.ChannelSubscriptionEntity
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.GroupEntity
import com.example.data.model.MessageEntity
import com.example.data.model.ModerationDossierEntity
import com.example.data.model.ReactionEntity
import com.example.data.model.ReportEntity
import com.example.data.model.SessionEntity
import com.example.data.model.StatusEntity
import com.example.data.model.StickerFavoriteEntity
import com.example.data.model.StickerRecentEntity
import com.example.data.model.UserEntity
import com.example.data.model.VerifiedBadgeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JamesChatDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE isBanned = 0")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE fourDigitCode = :code OR personalId = :code LIMIT 1")
    suspend fun getUserByFourDigitCode(code: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(jamesTag) = LOWER(:tag) LIMIT 1")
    suspend fun getUserByJamesTag(tag: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(jamesCustomId) = LOWER(:customId) LIMIT 1")
    suspend fun getUserByJamesCustomId(customId: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE fourDigitCode = :code OR personalId = :code")
    suspend fun countFourDigitCode(code: String): Int

    @Query("SELECT * FROM users WHERE (LOWER(username) LIKE '%' || LOWER(:query) || '%' OR LOWER(firstName) LIKE '%' || LOWER(:query) || '%' OR LOWER(lastName) LIKE '%' || LOWER(:query) || '%' OR fourDigitCode LIKE '%' || :query || '%' OR LOWER(jamesTag) LIKE '%' || LOWER(:query) || '%' OR LOWER(jamesCustomId) LIKE '%' || LOWER(:query) || '%') AND id != :excludeUserId AND isBanned = 0")
    suspend fun searchUsers(query: String, excludeUserId: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(:username)")
    suspend fun countUsername(username: String): Int

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, lastSeen: Long)

    @Query("UPDATE users SET isBanned = :isBanned WHERE id = :userId")
    suspend fun setBannedStatus(userId: String, isBanned: Boolean)

    @Query("UPDATE users SET isSuspended = :isSuspended WHERE id = :userId")
    suspend fun setSuspendedStatus(userId: String, isSuspended: Boolean)

    @Query("UPDATE users SET isVerified = :isVerified, verifiedAt = :verifiedAt, verifiedByAdminEmail = :adminEmail, verificationReason = :reason, verificationBadgeStyle = :badgeStyle WHERE id = :userId")
    suspend fun updateVerificationStatus(userId: String, isVerified: Boolean, verifiedAt: Long, adminEmail: String, reason: String, badgeStyle: String)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: String, role: String)

    @Query("UPDATE users SET isSuspended = :isSuspended, suspendedUntil = :suspendedUntil, isBanned = :isBanned, warningCount = :warningCount, restrictedUntil = :restrictedUntil WHERE id = :userId")
    suspend fun applyUserSanction(userId: String, isSuspended: Boolean, suspendedUntil: Long, isBanned: Boolean, warningCount: Int, restrictedUntil: Long)

    @Query("SELECT * FROM users WHERE isVerified = 1")
    fun getVerifiedUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isSuspended = 1 OR isBanned = 1")
    fun getSanctionedUsersFlow(): Flow<List<UserEntity>>

    // --- Contacts ---
    @Query("SELECT * FROM contacts WHERE ownerId = :ownerId")
    fun getContactsFlow(ownerId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE ownerId = :ownerId")
    fun getUserContactsFlow(ownerId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE ownerId = :ownerId")
    suspend fun getContacts(ownerId: String): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE ownerId = :ownerId AND contactUserId = :contactUserId LIMIT 1")
    suspend fun getContact(ownerId: String, contactUserId: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE ownerId = :ownerId AND contactUserId = :contactUserId")
    suspend fun deleteContact(ownerId: String, contactUserId: String)

    @Query("UPDATE contacts SET isBlocked = :isBlocked WHERE ownerId = :ownerId AND contactUserId = :contactUserId")
    suspend fun setContactBlocked(ownerId: String, contactUserId: String, isBlocked: Boolean)

    @Query("SELECT * FROM contacts WHERE ownerId = :ownerId AND isBlocked = 1")
    fun getBlockedContactsFlow(ownerId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE ownerId = :ownerId AND isBlocked = 1")
    fun getBlockedUsersFlow(ownerId: String): Flow<List<ContactEntity>>

    // --- Conversations ---
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllConversationsFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    fun getConversationByIdFlow(conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessageSnippet = :snippet, lastMessageTimestamp = :timestamp WHERE id = :conversationId")
    suspend fun updateConversationLastMessage(conversationId: String, snippet: String, timestamp: Long)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun resetConversationUnread(conversationId: String)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :conversationId")
    suspend fun incrementConversationUnread(conversationId: String)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :conversationId")
    suspend fun setConversationPinned(conversationId: String, isPinned: Boolean)

    @Query("UPDATE conversations SET ephemeralDurationMs = :durationMs WHERE id = :conversationId")
    suspend fun setEphemeralDuration(conversationId: String, durationMs: Long)

    @Query("UPDATE conversations SET wallpaperKey = :wallpaperKey WHERE id = :conversationId")
    suspend fun setConversationWallpaper(conversationId: String, wallpaperKey: String)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    // --- Messages ---
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isDeleted = 0 ORDER BY timestamp ASC")
    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isPinned = 1 AND isDeleted = 0 LIMIT 1")
    fun getPinnedMessageFlow(conversationId: String): Flow<MessageEntity?>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND LOWER(text) LIKE '%' || LOWER(:query) || '%' AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun searchMessages(conversationId: String, query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET text = :newText, isEdited = 1 WHERE id = :messageId")
    suspend fun editMessage(messageId: String, newText: String)

    @Query("UPDATE messages SET isDeleted = 1, text = 'Ce message a été supprimé' WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: String)

    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :messageId")
    suspend fun setMessagePinned(messageId: String, isPinned: Boolean)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("SELECT * FROM messages WHERE isPendingSync = 1")
    suspend fun getPendingSyncMessages(): List<MessageEntity>

    @Query("DELETE FROM messages WHERE expiresAt > 0 AND expiresAt < :currentTime")
    suspend fun purgeExpiredMessages(currentTime: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearConversationMessages(conversationId: String)

    // --- Reactions ---
    @Query("SELECT * FROM reactions")
    fun getAllReactionsFlow(): Flow<List<ReactionEntity>>

    @Query("SELECT * FROM reactions WHERE messageId = :messageId")
    fun getReactionsForMessageFlow(messageId: String): Flow<List<ReactionEntity>>

    @Query("SELECT * FROM reactions WHERE messageId = :messageId")
    suspend fun getReactionsForMessage(messageId: String): List<ReactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: ReactionEntity)

    @Query("DELETE FROM reactions WHERE messageId = :messageId AND userId = :userId")
    suspend fun removeReaction(messageId: String, userId: String)

    // --- Groups ---
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroupsFlow(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    fun getGroupByIdFlow(groupId: String): Flow<GroupEntity?>

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Query("SELECT * FROM groups WHERE inviteCode = :inviteCode LIMIT 1")
    suspend fun getGroupByInviteCode(inviteCode: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("SELECT * FROM groups WHERE isPublic = 1 ORDER BY createdAt DESC")
    fun getDiscoverableGroupsFlow(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE category = :category AND isPublic = 1 ORDER BY createdAt DESC")
    fun getGroupsByCategoryFlow(category: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE (LOWER(name) LIKE '%' || LOWER(:query) || '%' OR LOWER(description) LIKE '%' || LOWER(:query) || '%' OR LOWER(institution) LIKE '%' || LOWER(:query) || '%') AND isPublic = 1")
    fun searchDiscoverableGroupsFlow(query: String): Flow<List<GroupEntity>>

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    // --- Statuses (Stories) ---
    @Query("SELECT * FROM statuses WHERE expiresAt > :now ORDER BY createdAt DESC")
    fun getActiveStatusesFlow(now: Long = System.currentTimeMillis()): Flow<List<StatusEntity>>

    @Query("SELECT * FROM statuses WHERE userId = :userId AND expiresAt > :now ORDER BY createdAt DESC")
    fun getUserStatusesFlow(userId: String, now: Long = System.currentTimeMillis()): Flow<List<StatusEntity>>

    @Query("SELECT * FROM statuses WHERE id = :statusId LIMIT 1")
    fun getStatusByIdFlow(statusId: String): Flow<StatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: StatusEntity)

    @Query("UPDATE statuses SET viewersCount = viewersCount + 1, viewersJson = :viewersJson WHERE id = :statusId")
    suspend fun updateStatusViewers(statusId: String, viewersJson: String)

    @Query("DELETE FROM statuses WHERE id = :statusId")
    suspend fun deleteStatus(statusId: String)

    // --- Call Records ---
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getCallHistoryFlow(): Flow<List<CallRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(call: CallRecordEntity)

    @Query("DELETE FROM call_records")
    suspend fun clearCallHistory()

    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    // --- Sessions ---
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY isCurrent DESC, lastActive DESC")
    fun getUserSessionsFlow(userId: String): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM sessions WHERE userId = :userId AND isCurrent = 0")
    suspend fun terminateAllUserSessionsExceptCurrent(userId: String)

    // --- Admin Logs ---
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllAdminLogsFlow(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLogEntity)

    // --- Stickers ---
    @Query("SELECT * FROM sticker_favorites WHERE userId = :userId ORDER BY addedAt DESC")
    fun getStickerFavoritesFlow(userId: String): Flow<List<StickerFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickerFavorite(fav: StickerFavoriteEntity)

    @Query("DELETE FROM sticker_favorites WHERE userId = :userId AND stickerId = :stickerId")
    suspend fun deleteStickerFavorite(userId: String, stickerId: String)

    @Query("SELECT COUNT(*) FROM sticker_favorites WHERE userId = :userId AND stickerId = :stickerId")
    suspend fun isStickerFavorite(userId: String, stickerId: String): Int

    @Query("SELECT * FROM sticker_recents WHERE userId = :userId ORDER BY lastUsedAt DESC LIMIT 24")
    fun getStickerRecentsFlow(userId: String): Flow<List<StickerRecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickerRecent(recent: StickerRecentEntity)

    // --- Channels ---
    @Query("SELECT * FROM channels ORDER BY subscribersCount DESC, createdAt DESC")
    fun getAllChannelsFlow(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    fun getMyChannelsFlow(creatorId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :channelId LIMIT 1")
    fun getChannelByIdFlow(channelId: String): Flow<ChannelEntity?>

    @Query("SELECT * FROM channels WHERE id = :channelId LIMIT 1")
    suspend fun getChannelById(channelId: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getChannelByUsername(username: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels WHERE id = :channelId")
    suspend fun deleteChannel(channelId: String)

    @Query("UPDATE channels SET subscribersCount = :count WHERE id = :channelId")
    suspend fun updateChannelSubscribersCount(channelId: String, count: Int)

    @Query("UPDATE channels SET postsCount = :count WHERE id = :channelId")
    suspend fun updateChannelPostsCount(channelId: String, count: Int)

    // --- Channel Posts ---
    @Query("SELECT * FROM channel_posts WHERE channelId = :channelId ORDER BY createdAt DESC")
    fun getChannelPostsFlow(channelId: String): Flow<List<ChannelPostEntity>>

    @Query("SELECT * FROM channel_posts WHERE id = :postId LIMIT 1")
    suspend fun getChannelPostById(postId: String): ChannelPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelPost(post: ChannelPostEntity)

    @Update
    suspend fun updateChannelPost(post: ChannelPostEntity)

    @Query("DELETE FROM channel_posts WHERE id = :postId")
    suspend fun deleteChannelPost(postId: String)

    @Query("UPDATE channel_posts SET viewsCount = viewsCount + 1 WHERE id = :postId")
    suspend fun incrementPostViews(postId: String)

    @Query("UPDATE channel_posts SET reactionsJson = :reactionsJson WHERE id = :postId")
    suspend fun updatePostReactions(postId: String, reactionsJson: String)

    @Query("UPDATE channel_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementPostComments(postId: String)

    // --- Channel Subscriptions ---
    @Query("SELECT * FROM channel_subscriptions WHERE userId = :userId")
    fun getSubscribedChannelsFlow(userId: String): Flow<List<ChannelSubscriptionEntity>>

    @Query("SELECT * FROM channel_subscriptions WHERE channelId = :channelId AND userId = :userId LIMIT 1")
    suspend fun getSubscription(channelId: String, userId: String): ChannelSubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: ChannelSubscriptionEntity)

    @Query("DELETE FROM channel_subscriptions WHERE channelId = :channelId AND userId = :userId")
    suspend fun deleteSubscription(channelId: String, userId: String)

    @Query("SELECT COUNT(*) FROM channel_subscriptions WHERE channelId = :channelId")
    suspend fun countChannelSubscribers(channelId: String): Int

    @Query("UPDATE channel_subscriptions SET notificationsEnabled = :enabled WHERE channelId = :channelId AND userId = :userId")
    suspend fun updateSubscriptionNotifications(channelId: String, userId: String, enabled: Boolean)

    // --- Channel Comments ---
    @Query("SELECT * FROM channel_comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getChannelCommentsFlow(postId: String): Flow<List<ChannelCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelComment(comment: ChannelCommentEntity)

    @Query("DELETE FROM channel_comments WHERE id = :commentId")
    suspend fun deleteChannelComment(commentId: String)

    // --- Verified Badge Logs ---
    @Query("SELECT * FROM verified_badge_logs ORDER BY timestamp DESC")
    fun getAllVerifiedBadgeLogsFlow(): Flow<List<VerifiedBadgeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerifiedBadgeLog(log: VerifiedBadgeLogEntity)

    // --- Moderation Dossiers ---
    @Query("SELECT * FROM moderation_dossiers ORDER BY lastReportedAt DESC")
    fun getAllModerationDossiersFlow(): Flow<List<ModerationDossierEntity>>

    @Query("SELECT * FROM moderation_dossiers WHERE status IN ('PENDING', 'UNDER_REVIEW') ORDER BY reportCount DESC, lastReportedAt DESC")
    fun getActiveModerationDossiersFlow(): Flow<List<ModerationDossierEntity>>

    @Query("SELECT * FROM moderation_dossiers WHERE id = :dossierId LIMIT 1")
    suspend fun getDossierById(dossierId: String): ModerationDossierEntity?

    @Query("SELECT * FROM moderation_dossiers WHERE contentId = :contentId LIMIT 1")
    suspend fun getDossierByContentId(contentId: String): ModerationDossierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModerationDossier(dossier: ModerationDossierEntity)

    @Update
    suspend fun updateModerationDossier(dossier: ModerationDossierEntity)

    @Query("UPDATE moderation_dossiers SET status = :status, reviewedByAdmin = :admin, decisionNotes = :notes, sanctionType = :sanction, sanctionEndTimestamp = :sanctionEnd WHERE id = :dossierId")
    suspend fun updateDossierResolution(
        dossierId: String,
        status: String,
        admin: String,
        notes: String,
        sanction: String,
        sanctionEnd: Long
    )

    @Query("DELETE FROM moderation_dossiers WHERE id = :dossierId")
    suspend fun deleteDossier(dossierId: String)

    // --- James Chat V2 Queries ---
    @Query("SELECT * FROM messages WHERE isStarred = 1 AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getStarredMessagesFlow(): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isStarred = :isStarred WHERE id = :messageId")
    suspend fun setMessageStarred(messageId: String, isStarred: Boolean)

    @Query("UPDATE messages SET text = 'Ce message a été supprimé', isDeleted = 1, deletedForEveryone = 1 WHERE id = :messageId")
    suspend fun deleteMessageForEveryone(messageId: String)

    @Query("UPDATE messages SET text = 'Vous avez supprimé ce message', isDeleted = 1 WHERE id = :messageId")
    suspend fun deleteMessageForMe(messageId: String)

    @Query("SELECT * FROM messages WHERE LOWER(text) LIKE '%' || LOWER(:query) || '%' AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 50")
    suspend fun searchAllMessages(query: String): List<MessageEntity>

    @Query("UPDATE conversations SET draftText = :draft WHERE id = :conversationId")
    suspend fun updateConversationDraft(conversationId: String, draft: String)

    @Query("UPDATE conversations SET pinnedMessageId = :messageId, pinnedMessageText = :messageText WHERE id = :conversationId")
    suspend fun updateConversationPinnedMessage(conversationId: String, messageId: String?, messageText: String?)

    @Query("SELECT * FROM groups WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR LOWER(description) LIKE '%' || LOWER(:query) || '%'")
    suspend fun searchGroups(query: String): List<GroupEntity>

    @Query("SELECT * FROM channels WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' OR LOWER(description) LIKE '%' || LOWER(:query) || '%'")
    suspend fun searchChannels(query: String): List<ChannelEntity>

    @Query("UPDATE users SET privacyLastSeen = :lastSeen, privacyProfilePhoto = :photo, privacyWhoCanCall = :calls, privacyWhoCanAddGroups = :groups WHERE id = :userId")
    suspend fun updateUserPrivacySettings(userId: String, lastSeen: String, photo: String, calls: String, groups: String)

    @Query("UPDATE users SET appLockPin = :pin, isBiometricEnabled = :biometric, twoFactorPin = :twoFactor WHERE id = :userId")
    suspend fun updateUserSecurity(userId: String, pin: String, biometric: Boolean, twoFactor: String)

    @Query("UPDATE groups SET coAdminIds = :coAdmins WHERE id = :groupId")
    suspend fun updateGroupCoAdmins(groupId: String, coAdmins: String)

    @Query("UPDATE groups SET memberIds = :members, pendingMemberIds = :pending WHERE id = :groupId")
    suspend fun updateGroupMembersAndPending(groupId: String, members: String, pending: String)

    @Query("UPDATE groups SET onlyAdminsCanSend = :onlyAdmins, requireApproval = :approval, rules = :rules, description = :desc WHERE id = :groupId")
    suspend fun updateGroupSettings(groupId: String, onlyAdmins: Boolean, approval: Boolean, rules: String, desc: String)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    // --- James Bot Knowledge Base & Reports ---
    @Query("SELECT * FROM bot_knowledge ORDER BY updatedAt DESC")
    fun getAllBotKnowledgeFlow(): Flow<List<com.example.data.model.BotKnowledgeEntity>>

    @Query("SELECT * FROM bot_knowledge WHERE isPublished = 1 ORDER BY updatedAt DESC")
    suspend fun getPublishedBotKnowledge(): List<com.example.data.model.BotKnowledgeEntity>

    @Query("SELECT * FROM bot_knowledge WHERE id = :id LIMIT 1")
    suspend fun getBotKnowledgeById(id: String): com.example.data.model.BotKnowledgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBotKnowledge(entry: com.example.data.model.BotKnowledgeEntity)

    @Update
    suspend fun updateBotKnowledge(entry: com.example.data.model.BotKnowledgeEntity)

    @Query("DELETE FROM bot_knowledge WHERE id = :id")
    suspend fun deleteBotKnowledge(id: String)

    @Query("UPDATE bot_knowledge SET isPublished = :isPublished, updatedAt = :updatedAt, lastUpdatedByAdminEmail = :adminEmail WHERE id = :id")
    suspend fun toggleBotKnowledgePublished(id: String, isPublished: Boolean, updatedAt: Long, adminEmail: String)

    @Query("SELECT COUNT(*) FROM bot_knowledge")
    suspend fun countBotKnowledge(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBotReport(report: com.example.data.model.BotReportEntity)

    @Query("SELECT * FROM bot_reports ORDER BY timestamp DESC")
    fun getAllBotReportsFlow(): Flow<List<com.example.data.model.BotReportEntity>>

    @Query("UPDATE bot_reports SET status = :status, reviewedByAdmin = :adminEmail WHERE id = :id")
    suspend fun updateBotReportStatus(id: String, status: String, adminEmail: String)

    // --- Verification Requests & Badges ---
    @Query("SELECT * FROM verification_requests ORDER BY submittedAt DESC")
    fun getAllVerificationRequestsFlow(): Flow<List<com.example.data.model.VerificationRequestEntity>>

    @Query("SELECT * FROM verification_requests WHERE userId = :userId ORDER BY submittedAt DESC LIMIT 1")
    fun getLatestVerificationRequestFlow(userId: String): Flow<com.example.data.model.VerificationRequestEntity?>

    @Query("SELECT * FROM verification_requests WHERE userId = :userId ORDER BY submittedAt DESC LIMIT 1")
    suspend fun getLatestVerificationRequest(userId: String): com.example.data.model.VerificationRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequest(request: com.example.data.model.VerificationRequestEntity)

    @Update
    suspend fun updateVerificationRequest(request: com.example.data.model.VerificationRequestEntity)

    @Query("UPDATE verification_requests SET status = :status, reviewedByAdminEmail = :adminEmail, rejectionReason = :rejectionReason, assignedBadgeStyle = :badgeStyle, reviewedAt = :reviewedAt WHERE id = :requestId")
    suspend fun reviewVerificationRequest(
        requestId: String,
        status: String,
        adminEmail: String,
        rejectionReason: String,
        badgeStyle: String,
        reviewedAt: Long
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationBadge(badge: com.example.data.model.VerificationBadgeEntity)

    @Query("SELECT * FROM verification_badges WHERE userId = :userId LIMIT 1")
    suspend fun getVerificationBadgeByUserId(userId: String): com.example.data.model.VerificationBadgeEntity?

    @Query("SELECT * FROM verification_badges WHERE userId = :userId LIMIT 1")
    fun getVerificationBadgeFlow(userId: String): Flow<com.example.data.model.VerificationBadgeEntity?>

    @Query("DELETE FROM verification_badges WHERE userId = :userId")
    suspend fun deleteVerificationBadge(userId: String)
}
