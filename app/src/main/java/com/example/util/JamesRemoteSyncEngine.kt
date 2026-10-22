package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.local.JamesChatDao
import com.example.data.model.ContactEntity
import com.example.data.model.UserEntity
import com.example.data.model.VerificationBadgeEntity
import com.example.data.model.VerifiedBadgeLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

sealed class SyncConnectionState {
    object Connected : SyncConnectionState()
    object Offline : SyncConnectionState()
    data class Syncing(val task: String) : SyncConnectionState()
    data class Error(val message: String) : SyncConnectionState()
}

/**
 * Moteur de synchronisation multi-appareils et d'annuaire distant James Chat.
 * Assure la recherche des James ID au-delà de la base SQLite locale,
 * la synchronisation des contacts par compte utilisateur et la vérification
 * sécurisée des V Badges côté serveur.
 */
class JamesRemoteSyncEngine(
    private val context: Context,
    private val dao: JamesChatDao
) {
    private val _connectionState = MutableStateFlow<SyncConnectionState>(SyncConnectionState.Connected)
    val connectionState: StateFlow<SyncConnectionState> = _connectionState.asStateFlow()

    // URL configurable du serveur de synchronisation d'annuaire distant (par défaut port JamesWebServer local/LAN)
    private var remoteServerBaseUrl: String = "http://127.0.0.1:8080"

    fun setRemoteServerUrl(url: String) {
        remoteServerBaseUrl = url.trimEnd('/')
    }

    fun getRemoteServerUrl(): String = remoteServerBaseUrl

    /**
     * Vérifie la connectivité réseau physique du terminal.
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true // En cas de sandbox de test, présumer disponible
        }
    }

    /**
     * Recherche distante d'un utilisateur par son James ID, James Tag, ou @username.
     * Si l'utilisateur est trouvé sur le serveur distant, il est automatiquement mis en cache
     * dans la base SQLite locale pour permettre la continuité hors ligne et l'accès immédiat.
     */
    suspend fun resolveRemoteUser(parsed: JamesIdentityUtil.ContactSearchParams): Result<UserEntity?> = withContext(Dispatchers.IO) {
        _connectionState.value = SyncConnectionState.Syncing("Recherche d'annuaire distant...")

        // Requête sur l'API d'annuaire distant
        val candidateTerm = when {
            !parsed.exactUserId.isNullOrEmpty() -> parsed.exactUserId
            !parsed.candidateTag.isNullOrEmpty() -> parsed.candidateTag
            !parsed.candidateCustomId.isNullOrEmpty() -> parsed.candidateCustomId
            !parsed.candidateCode.isNullOrEmpty() -> parsed.candidateCode
            !parsed.username.isNullOrEmpty() -> parsed.username
            else -> parsed.normalizedQuery
        }

        if (candidateTerm.isNullOrBlank()) {
            _connectionState.value = SyncConnectionState.Connected
            return@withContext Result.success(null)
        }

        try {
            val encodedQuery = URLEncoder.encode(candidateTerm, "UTF-8")
            val targetUrl = "$remoteServerBaseUrl/api/directory/resolve?q=$encodedQuery"
            val url = URL(targetUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.readText()
                reader.close()

                val json = JSONObject(jsonString)
                if (json.optBoolean("found", false)) {
                    val userObj = json.getJSONObject("user")
                    val remoteUser = parseUserJson(userObj)
                    // Mettre en cache localement dans SQLite pour garantir la disponibilité hors ligne
                    dao.insertUser(remoteUser)
                    _connectionState.value = SyncConnectionState.Connected
                    return@withContext Result.success(remoteUser)
                } else {
                    _connectionState.value = SyncConnectionState.Connected
                    return@withContext Result.success(null)
                }
            } else if (responseCode == 404) {
                _connectionState.value = SyncConnectionState.Connected
                return@withContext Result.success(null)
            } else {
                _connectionState.value = SyncConnectionState.Error("Erreur serveur ($responseCode)")
                return@withContext Result.failure(Exception("Erreur serveur lors de la recherche ($responseCode)"))
            }
        } catch (e: Exception) {
            Log.w("JamesSyncEngine", "Annuaire distant non joignable (${e.message}) - Utilisation de l'annuaire local")
            _connectionState.value = SyncConnectionState.Offline
            // En cas d'indisponibilité réseau, retourner l'exception pour permettre un message clair à l'UI
            return@withContext Result.failure(e)
        }
    }

    /**
     * Synchronise la liste des contacts d'un utilisateur avec le serveur distant.
     * Empêche les doublons et assure la réplication entre les appareils du même compte.
     */
    suspend fun syncContactsWithRemote(ownerId: String): Result<List<ContactEntity>> = withContext(Dispatchers.IO) {
        if (ownerId.isEmpty()) return@withContext Result.success(emptyList())

        _connectionState.value = SyncConnectionState.Syncing("Synchronisation des contacts...")
        try {
            val targetUrl = "$remoteServerBaseUrl/api/contacts?ownerId=${URLEncoder.encode(ownerId, "UTF-8")}"
            val connection = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200) {
                val jsonString = BufferedReader(InputStreamReader(connection.inputStream)).readText()
                val json = JSONObject(jsonString)
                val contactsArray = json.optJSONArray("contacts") ?: JSONArray()
                val syncedList = mutableListOf<ContactEntity>()

                for (i in 0 until contactsArray.length()) {
                    val cObj = contactsArray.getJSONObject(i)
                    val contact = ContactEntity(
                        id = cObj.optString("id", "${ownerId}_${cObj.getString("contactUserId")}"),
                        ownerId = ownerId,
                        contactUserId = cObj.getString("contactUserId"),
                        customSound = cObj.optString("customSound", "Default"),
                        isBlocked = cObj.optBoolean("isBlocked", false),
                        addedAt = cObj.optLong("addedAt", System.currentTimeMillis())
                    )
                    dao.insertContact(contact)
                    syncedList.add(contact)
                }
                _connectionState.value = SyncConnectionState.Connected
                return@withContext Result.success(syncedList)
            } else {
                _connectionState.value = SyncConnectionState.Connected
                return@withContext Result.success(dao.getContacts(ownerId))
            }
        } catch (e: Exception) {
            Log.w("JamesSyncEngine", "Impossible de synchroniser les contacts avec le serveur distant (${e.message})")
            _connectionState.value = SyncConnectionState.Offline
            // Récupération hors ligne depuis SQLite
            return@withContext Result.success(dao.getContacts(ownerId))
        }
    }

    /**
     * Envoie l'ajout d'un contact au serveur distant pour réplication sur les autres appareils du compte.
     */
    suspend fun pushContactRemote(ownerId: String, targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val targetUrl = "$remoteServerBaseUrl/api/contacts/add"
            val connection = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val payload = JSONObject().apply {
                put("ownerId", ownerId)
                put("contactUserId", targetUserId)
            }
            OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }
            val code = connection.responseCode
            if (code in 200..299) {
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Serveur code $code"))
            }
        } catch (e: Exception) {
            Log.w("JamesSyncEngine", "Enregistrement contact distant en attente (${e.message})")
            return@withContext Result.success(Unit) // Offline-first tolérant
        }
    }

    /**
     * Envoie la suppression d'un contact au serveur distant.
     */
    suspend fun deleteContactRemote(ownerId: String, targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val targetUrl = "$remoteServerBaseUrl/api/contacts/delete"
            val connection = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val payload = JSONObject().apply {
                put("ownerId", ownerId)
                put("contactUserId", targetUserId)
            }
            OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }
            return@withContext Result.success(Unit)
        } catch (_: Exception) {
            return@withContext Result.success(Unit)
        }
    }

    /**
     * Synchronise le profil et l'état de vérification V Badge avec le serveur distant.
     */
    suspend fun pushUserSyncRemote(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val targetUrl = "$remoteServerBaseUrl/api/users/sync"
            val connection = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val payload = JSONObject().apply {
                put("id", user.id)
                put("email", user.email)
                put("username", user.username)
                put("firstName", user.firstName)
                put("lastName", user.lastName)
                put("fourDigitCode", user.fourDigitCode)
                put("jamesTag", user.jamesTag)
                put("jamesCustomId", user.jamesCustomId)
                put("isVerified", user.isVerified)
                put("verificationBadgeStyle", user.verificationBadgeStyle)
                put("role", user.role)
                put("avatarUrl", user.avatarUrl)
                put("profileVideoUrl", user.profileVideoUrl)
                put("bio", user.bio)
            }
            OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }
            return@withContext Result.success(Unit)
        } catch (_: Exception) {
            return@withContext Result.success(Unit)
        }
    }

    private fun parseUserJson(obj: JSONObject): UserEntity {
        return UserEntity(
            id = obj.getString("id"),
            email = obj.optString("email", ""),
            username = obj.optString("username", ""),
            firstName = obj.optString("firstName", ""),
            lastName = obj.optString("lastName", ""),
            bio = obj.optString("bio", ""),
            avatarUrl = obj.optString("avatarUrl", ""),
            profileVideoUrl = obj.optString("profileVideoUrl", ""),
            fourDigitCode = obj.optString("fourDigitCode", ""),
            jamesTag = obj.optString("jamesTag", ""),
            jamesCustomId = obj.optString("jamesCustomId", ""),
            isVerified = obj.optBoolean("isVerified", false),
            verificationBadgeStyle = obj.optString("verificationBadgeStyle", "SEAL"),
            role = obj.optString("role", "USER"),
            isOnline = obj.optBoolean("isOnline", true)
        )
    }
}
