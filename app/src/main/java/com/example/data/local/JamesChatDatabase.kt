package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        ReactionEntity::class,
        GroupEntity::class,
        StatusEntity::class,
        CallRecordEntity::class,
        ReportEntity::class,
        SessionEntity::class,
        AdminLogEntity::class,
        StickerFavoriteEntity::class,
        StickerRecentEntity::class,
        ChannelEntity::class,
        ChannelPostEntity::class,
        ChannelSubscriptionEntity::class,
        ChannelCommentEntity::class,
        VerifiedBadgeLogEntity::class,
        ModerationDossierEntity::class,
        com.example.data.model.BotKnowledgeEntity::class,
        com.example.data.model.BotReportEntity::class,
        com.example.data.model.VerificationRequestEntity::class,
        com.example.data.model.VerificationBadgeEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class JamesChatDatabase : RoomDatabase() {
    abstract fun jamesChatDao(): JamesChatDao

    companion object {
        @Volatile
        private var INSTANCE: JamesChatDatabase? = null

        fun getInstance(context: Context): JamesChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JamesChatDatabase::class.java,
                    "james_chat.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
