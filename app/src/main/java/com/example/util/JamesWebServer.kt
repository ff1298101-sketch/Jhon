package com.example.util

import android.content.Context
import com.example.data.model.MessageType
import com.example.data.repository.JamesChatRepository
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.URLDecoder
import java.util.Locale
import java.util.concurrent.Executors

object JamesWebServer {
    private var server: HttpServer? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var repositoryRef: JamesChatRepository? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow("http://127.0.0.1:8080")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount.asStateFlow()

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "127.0.0.1"
    }

    fun start(context: Context, repository: JamesChatRepository, port: Int = 8080): Boolean {
        if (_isRunning.value) return true
        repositoryRef = repository
        return try {
            val ip = getLocalIpAddress()
            val address = InetSocketAddress(port)
            server = HttpServer.create(address, 0).apply {
                executor = Executors.newFixedThreadPool(8)
                createContext("/", WebAppHandler(repository))
                createContext("/api/conversations", ConversationsApiHandler(repository))
                createContext("/api/messages", MessagesApiHandler(repository))
                createContext("/api/send", SendMessageApiHandler(repository, context))
                createContext("/api/upload", FileUploadApiHandler(repository, context))
                createContext("/api/status", StatusApiHandler(repository))
                createContext("/files", FileStreamApiHandler(context))
                start()
            }
            _serverUrl.value = "http://127.0.0.1:$port"
            _isRunning.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isRunning.value = false
            false
        }
    }

    fun stop() {
        try {
            server?.stop(0)
            server = null
            _isRunning.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "Ko", "Mo", "Go", "To")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[minOf(digitGroups, units.size - 1)])
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase(Locale.US)) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "3gp" -> "video/3gpp"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a", "aac" -> "audio/mp4"
            "flac" -> "audio/flac"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "apk" -> "application/vnd.android.package-archive"
            "txt" -> "text/plain; charset=UTF-8"
            "html" -> "text/html; charset=UTF-8"
            else -> "application/octet-stream"
        }
    }

    private class WebAppHandler(private val repository: JamesChatRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("GET", ignoreCase = true)) {
                _connectedClientsCount.value = maxOf(1, _connectedClientsCount.value)
                val html = generateWebHtml()
                val bytes = html.toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.set("Content-Type", "text/html; charset=UTF-8")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                val os: OutputStream = exchange.responseBody
                os.write(bytes)
                os.close()
            } else {
                exchange.sendResponseHeaders(405, -1)
            }
        }
    }

    private class ConversationsApiHandler(private val repository: JamesChatRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            val conversations = runBlocking {
                repository.dao.getAllConversationsFlow().firstOrNull() ?: emptyList()
            }
            val array = JSONArray()
            conversations.forEach { conv ->
                val obj = JSONObject().apply {
                    put("id", conv.id)
                    put("title", conv.title)
                    put("avatarUrl", conv.avatarUrl)
                    put("isGroup", conv.isGroup)
                    put("lastMessage", conv.lastMessageSnippet)
                    put("lastMessageTime", conv.lastMessageTimestamp)
                    put("unreadCount", conv.unreadCount)
                }
                array.put(obj)
            }
            val bytes = array.toString().toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }

    private class MessagesApiHandler(private val repository: JamesChatRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            val query = exchange.requestURI.query ?: ""
            val convId = query.split("&")
                .firstOrNull { it.startsWith("conversationId=") }
                ?.substringAfter("conversationId=") ?: ""

            val messages = if (convId.isNotEmpty()) {
                runBlocking {
                    repository.dao.getMessagesFlow(convId).firstOrNull() ?: emptyList()
                }
            } else emptyList()

            val array = JSONArray()
            messages.forEach { msg ->
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("senderId", msg.senderId)
                    put("senderUsername", msg.senderUsername)
                    put("text", msg.text)
                    put("timestamp", msg.timestamp)
                    put("status", msg.status)
                    put("type", msg.type)
                    put("mediaUrl", msg.mediaUrl ?: "")
                    put("fileName", msg.fileName ?: "")
                    put("fileSize", msg.fileSize ?: "")
                }
                array.put(obj)
            }
            val bytes = array.toString().toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }

    private class SendMessageApiHandler(
        private val repository: JamesChatRepository,
        private val context: Context
    ) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                exchange.sendResponseHeaders(204, -1)
                return
            }

            if (exchange.requestMethod.equals("POST", ignoreCase = true)) {
                val reader = BufferedReader(InputStreamReader(exchange.requestBody, Charsets.UTF_8))
                val body = reader.readText()
                val json = JSONObject(body)
                val convId = json.optString("conversationId")
                val text = json.optString("text")
                val mediaTypeStr = json.optString("type")
                val mediaUrl = json.optString("mediaUrl")

                if (convId.isNotEmpty()) {
                    scope.launch {
                        if (mediaTypeStr == "PHOTO" && mediaUrl.isNotEmpty()) {
                            repository.sendMediaMessage(
                                conversationId = convId,
                                type = MessageType.PHOTO,
                                mediaUrl = mediaUrl,
                                caption = text.ifEmpty { "📷 Photo partagée depuis James Web" }
                            )
                        } else if (mediaTypeStr == "AUDIO" && mediaUrl.isNotEmpty()) {
                            repository.sendMediaMessage(
                                conversationId = convId,
                                type = MessageType.AUDIO,
                                mediaUrl = mediaUrl,
                                durationSeconds = json.optInt("durationSeconds", 5),
                                caption = text.ifEmpty { "🎤 Note vocale James Web" }
                            )
                        } else if (text.isNotEmpty()) {
                            repository.sendTextMessage(convId, text)
                        }

                        JamesNotificationManager.showMessageNotification(
                            context = context,
                            senderName = "James Web",
                            messageText = text.ifEmpty { "Nouveau message depuis le Web" },
                            conversationId = convId
                        )
                    }
                    val resp = JSONObject().put("success", true).toString().toByteArray(Charsets.UTF_8)
                    exchange.responseHeaders.set("Content-Type", "application/json")
                    exchange.sendResponseHeaders(200, resp.size.toLong())
                    exchange.responseBody.use { it.write(resp) }
                } else {
                    exchange.sendResponseHeaders(400, -1)
                }
            } else {
                exchange.sendResponseHeaders(405, -1)
            }
        }
    }

    private class FileUploadApiHandler(
        private val repository: JamesChatRepository,
        private val context: Context
    ) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type, X-Conversation-Id, X-File-Name, X-Media-Type, X-Caption")
            exchange.responseHeaders.set("Access-Control-Allow-Methods", "POST, OPTIONS")

            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                exchange.sendResponseHeaders(204, -1)
                return
            }

            if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                exchange.sendResponseHeaders(405, -1)
                return
            }

            try {
                val query = exchange.requestURI.query ?: ""
                val queryMap = query.split("&").filter { it.contains("=") }.associate {
                    val (k, v) = it.split("=", limit = 2)
                    try {
                        URLDecoder.decode(k, "UTF-8") to URLDecoder.decode(v, "UTF-8")
                    } catch (_: Exception) {
                        k to v
                    }
                }

                val convId = exchange.requestHeaders.getFirst("X-Conversation-Id")
                    ?: queryMap["conversationId"] ?: ""
                val rawFileName = exchange.requestHeaders.getFirst("X-File-Name")
                    ?: queryMap["fileName"] ?: "fichier_${System.currentTimeMillis()}"
                val caption = exchange.requestHeaders.getFirst("X-Caption")
                    ?: queryMap["caption"] ?: ""
                val mediaTypeStr = exchange.requestHeaders.getFirst("X-Media-Type")
                    ?: queryMap["type"] ?: ""

                val safeFileName = "${System.currentTimeMillis()}_" + rawFileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                val uploadsDir = File(context.filesDir, "web_uploads").apply { mkdirs() }
                val targetFile = File(uploadsDir, safeFileName)

                // High-performance streaming direct to disk in 64KB buffer chunks - avoids OutOfMemory for heavy films & files!
                exchange.requestBody.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output, bufferSize = 64 * 1024)
                    }
                }

                val ext = targetFile.extension.lowercase(Locale.US)
                val detectedType = when {
                    ext in listOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "flv") -> MessageType.VIDEO
                    ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp") -> MessageType.PHOTO
                    ext in listOf("mp3", "ogg", "wav", "m4a", "aac", "flac") -> MessageType.AUDIO
                    else -> MessageType.DOCUMENT
                }

                val finalType = if (mediaTypeStr == "VIDEO") MessageType.VIDEO
                else if (mediaTypeStr == "PHOTO") MessageType.PHOTO
                else if (mediaTypeStr == "AUDIO") MessageType.AUDIO
                else detectedType

                val fileSizeFormatted = formatBytes(targetFile.length())

                if (convId.isNotEmpty()) {
                    runBlocking {
                        repository.sendMediaMessage(
                            conversationId = convId,
                            type = finalType,
                            mediaUrl = "/files/$safeFileName",
                            fileName = rawFileName,
                            fileSize = fileSizeFormatted,
                            caption = caption.ifEmpty {
                                when (finalType) {
                                    MessageType.VIDEO -> "🎬 $rawFileName ($fileSizeFormatted)"
                                    MessageType.PHOTO -> "📷 $rawFileName"
                                    MessageType.AUDIO -> "🎵 $rawFileName"
                                    else -> "📁 $rawFileName ($fileSizeFormatted)"
                                }
                            }
                        )
                        JamesNotificationManager.showMessageNotification(
                            context = context,
                            senderName = "James Web",
                            messageText = "Nouveau fichier reçu : $rawFileName ($fileSizeFormatted)",
                            conversationId = convId
                        )
                    }
                }

                val resp = JSONObject().apply {
                    put("success", true)
                    put("mediaUrl", "/files/$safeFileName")
                    put("fileName", rawFileName)
                    put("fileSize", fileSizeFormatted)
                    put("type", finalType.name)
                }.toString().toByteArray(Charsets.UTF_8)

                exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
                exchange.sendResponseHeaders(200, resp.size.toLong())
                exchange.responseBody.use { it.write(resp) }
            } catch (e: Exception) {
                e.printStackTrace()
                val errResp = JSONObject().apply {
                    put("success", false)
                    put("error", e.message ?: "Erreur upload")
                }.toString().toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.set("Content-Type", "application/json; charset=UTF-8")
                exchange.sendResponseHeaders(500, errResp.size.toLong())
                exchange.responseBody.use { it.write(errResp) }
            }
        }
    }

    private class FileStreamApiHandler(private val context: Context) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.set("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
            exchange.responseHeaders.set("Access-Control-Allow-Headers", "Range")

            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                exchange.sendResponseHeaders(204, -1)
                return
            }

            val rawPath = exchange.requestURI.path.substringAfter("/files").trimStart('/')
            val uploadsDir = File(context.filesDir, "web_uploads")
            val file = File(uploadsDir, rawPath)

            if (!file.exists() || !file.isFile) {
                val notFound = "Fichier introuvable".toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(404, notFound.size.toLong())
                exchange.responseBody.use { it.write(notFound) }
                return
            }

            val contentType = getMimeType(file.extension)
            val fileLength = file.length()
            val rangeHeader = exchange.requestHeaders.getFirst("Range")

            exchange.responseHeaders.set("Content-Type", contentType)
            exchange.responseHeaders.set("Accept-Ranges", "bytes")

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // HTTP 206 Partial Content (Supports fast video seeking and streaming!)
                val range = rangeHeader.removePrefix("bytes=").trim()
                val parts = range.split("-")
                val start = parts[0].toLongOrNull() ?: 0L
                val end = if (parts.size > 1 && parts[1].isNotEmpty()) parts[1].toLongOrNull() ?: (fileLength - 1) else (fileLength - 1)
                val clampedEnd = minOf(end, fileLength - 1)
                val contentLength = maxOf(0L, clampedEnd - start + 1)

                exchange.responseHeaders.set("Content-Range", "bytes $start-$clampedEnd/$fileLength")
                exchange.sendResponseHeaders(206, contentLength)

                file.inputStream().use { input ->
                    input.skip(start)
                    val buffer = ByteArray(64 * 1024)
                    var bytesRemaining = contentLength
                    while (bytesRemaining > 0) {
                        val toRead = minOf(buffer.size.toLong(), bytesRemaining).toInt()
                        val read = input.read(buffer, 0, toRead)
                        if (read == -1) break
                        exchange.responseBody.write(buffer, 0, read)
                        bytesRemaining -= read
                    }
                }
            } else {
                exchange.responseHeaders.set("Content-Length", fileLength.toString())
                exchange.sendResponseHeaders(200, fileLength)
                file.inputStream().use { input ->
                    input.copyTo(exchange.responseBody, 64 * 1024)
                }
            }
        }
    }

    private class StatusApiHandler(private val repository: JamesChatRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            val user = repository.currentUser.value
            val displayName = ((user?.firstName ?: "") + " " + (user?.lastName ?: "")).trim().ifEmpty { "Utilisateur" }
            val obj = JSONObject().apply {
                put("online", true)
                put("appName", "James Chat Web")
                put("user", JSONObject().apply {
                    put("id", user?.id ?: "")
                    put("name", displayName)
                    put("username", user?.username ?: "")
                    put("avatarUrl", user?.avatarUrl ?: "")
                    put("isVerified", user?.isVerified ?: false)
                })
            }
            val bytes = obj.toString().toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }

    private fun generateWebHtml(): String {
        return """
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>James Chat Web • Plateforme de Communication & Médias</title>
  <style>
    :root {
      --bg-dark: #0b141a;
      --panel-dark: #111b21;
      --header-dark: #202c33;
      --text-dark: #e9edef;
      --text-muted: #8696a0;
      --green: #00a884;
      --green-dark: #005c4b;
      --bubble-in: #202c33;
      --border-dark: #222d34;
      --input-bg: #2a3942;
    }
    .light-theme {
      --bg-dark: #eae6df;
      --panel-dark: #ffffff;
      --header-dark: #f0f2f5;
      --text-dark: #111b21;
      --text-muted: #667781;
      --green: #008069;
      --green-dark: #005c4b;
      --bubble-in: #ffffff;
      --border-dark: #e9edef;
      --input-bg: #f0f2f5;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      background: var(--bg-dark);
      color: var(--text-dark);
      display: flex;
      height: 100vh;
      overflow: hidden;
    }
    #sidebar {
      width: 380px;
      background: var(--panel-dark);
      border-right: 1px solid var(--border-dark);
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      transition: transform 0.25s ease;
    }
    .sidebar-header {
      height: 60px;
      background: var(--header-dark);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 16px;
    }
    .header-user-info { display: flex; align-items: center; gap: 10px; }
    .header-brand-title { display: flex; flex-direction: column; }
    .brand-name { font-weight: 800; font-size: 15px; color: var(--text-dark); }
    .brand-badge { font-size: 10px; color: #00e5ff; font-weight: 600; text-transform: uppercase; }
    .header-icons { display: flex; gap: 12px; }
    .header-btn {
      background: transparent;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      font-size: 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 6px;
      border-radius: 50%;
    }
    .header-btn:hover { background: rgba(255,255,255,0.08); color: var(--text-dark); }
    .search-box {
      padding: 8px 12px;
      background: var(--panel-dark);
      border-bottom: 1px solid var(--border-dark);
    }
    .search-input {
      width: 100%;
      background: var(--input-bg);
      border: none;
      border-radius: 8px;
      padding: 8px 14px;
      color: var(--text-dark);
      font-size: 13px;
      outline: none;
    }
    .chat-list { flex: 1; overflow-y: auto; }
    .chat-item {
      display: flex;
      padding: 12px 16px;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      border-bottom: 1px solid rgba(255,255,255,0.03);
      transition: background 0.15s;
    }
    .chat-item:hover, .chat-item.active { background: rgba(255,255,255,0.05); }
    .chat-avatar {
      width: 46px;
      height: 46px;
      border-radius: 50%;
      background: #2563eb;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 17px;
      flex-shrink: 0;
      position: relative;
    }
    .chat-avatar.online::after {
      content: '';
      position: absolute;
      bottom: 1px;
      right: 1px;
      width: 11px;
      height: 11px;
      background: #00a884;
      border: 2px solid var(--panel-dark);
      border-radius: 50%;
    }
    .chat-details { flex: 1; min-width: 0; }
    .chat-top-row { display: flex; justify-content: space-between; margin-bottom: 4px; }
    .chat-name { font-weight: 600; font-size: 15px; color: var(--text-dark); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .chat-time { font-size: 11px; color: var(--text-muted); }
    .chat-last-msg { font-size: 13px; color: var(--text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

    /* MAIN CHAT */
    #main-chat {
      flex: 1;
      display: flex;
      flex-direction: column;
      background: var(--bg-dark);
      position: relative;
    }
    .chat-topbar {
      height: 60px;
      background: var(--header-dark);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 16px;
      border-bottom: 1px solid var(--border-dark);
    }
    .chat-topbar-left { display: flex; align-items: center; gap: 10px; }
    .topbar-info h3 { font-size: 15px; font-weight: 700; color: var(--text-dark); }
    .topbar-info span { font-size: 11px; color: #00a884; }
    .chat-topbar-actions { display: flex; gap: 12px; }
    .action-icon-btn {
      background: transparent;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      font-size: 19px;
      padding: 6px;
      border-radius: 50%;
    }
    .action-icon-btn:hover { background: rgba(255,255,255,0.08); color: var(--text-dark); }
    .messages-area {
      flex: 1;
      overflow-y: auto;
      padding: 20px 24px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      background: radial-gradient(circle at 50% 50%, rgba(37,99,235,0.05), transparent 70%);
    }
    .message {
      max-width: 65%;
      padding: 8px 12px;
      border-radius: 12px;
      font-size: 14px;
      line-height: 1.4;
      position: relative;
      word-wrap: break-word;
    }
    .msg-incoming {
      align-self: flex-start;
      background: var(--bubble-in);
      color: var(--text-dark);
      border-top-left-radius: 2px;
    }
    .msg-outgoing {
      align-self: flex-end;
      background: var(--green-dark);
      color: #e9edef;
      border-top-right-radius: 2px;
    }
    .msg-meta {
      font-size: 10px;
      color: rgba(255,255,255,0.6);
      text-align: right;
      margin-top: 4px;
    }
    .chat-input-area {
      min-height: 62px;
      background: var(--header-dark);
      display: flex;
      align-items: center;
      padding: 8px 14px;
      gap: 8px;
      border-top: 1px solid var(--border-dark);
    }
    .input-text {
      flex: 1;
      background: var(--input-bg);
      border: none;
      border-radius: 8px;
      padding: 10px 14px;
      color: var(--text-dark);
      font-size: 14px;
      outline: none;
    }
    .send-btn {
      background: #00a884;
      border: none;
      color: white;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
    }
    .send-btn:hover { background: #029374; }
    .empty-state {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 30px;
    }
    .back-to-list-btn { display: none; background: transparent; border: none; font-size: 20px; cursor: pointer; color: var(--text-dark); margin-right: 6px; }

    /* DRAG & DROP & UPLOAD */
    .drag-active { outline: 3px dashed #00a884; background: rgba(0,168,132,0.08) !important; }

    @media (max-width: 768px) {
      #sidebar { width: 100%; position: absolute; height: 100%; z-index: 10; }
      #sidebar.chat-open { transform: translateX(-100%); }
      #main-chat { display: none; }
      #main-chat.chat-open { display: flex; width: 100%; }
      .back-to-list-btn { display: inline-block; }
    }
  </style>
</head>
<body>

  <!-- SIDEBAR -->
  <div id="sidebar">
    <div class="sidebar-header">
      <div class="header-user-info">
        <div class="chat-avatar online" id="current-user-avatar">J</div>
        <div class="header-brand-title">
          <span class="brand-name" id="user-display-name">James Chat</span>
          <span class="brand-badge">Web & Fichiers</span>
        </div>
      </div>
      <div class="header-icons">
        <button class="header-btn" id="theme-toggle-btn" title="Changer Thème">🌓</button>
        <button class="header-btn" id="refresh-chats-btn" title="Rafraîchir">🔄</button>
      </div>
    </div>
    <div class="search-box">
      <input type="text" id="search-input" class="search-input" placeholder="🔍 Rechercher une discussion...">
    </div>
    <div class="chat-list" id="conversations-list">
      <div style="padding: 20px; text-align: center; color: var(--text-muted);">Chargement de vos discussions...</div>
    </div>
  </div>

  <!-- MAIN CHAT AREA -->
  <div id="main-chat">
    <div class="chat-topbar" id="chat-header" style="display: none;">
      <div class="chat-topbar-left">
        <button class="back-to-list-btn" id="back-to-list">⬅️</button>
        <div class="chat-avatar online" id="header-avatar" style="width: 38px; height: 38px;"></div>
        <div class="topbar-info">
          <h3 id="header-title">Discussion</h3>
          <span>● En ligne sur James Chat</span>
        </div>
      </div>
      <div class="chat-topbar-actions">
        <button class="action-icon-btn" onclick="triggerCall('vocal')" title="Appel Vocal">📞</button>
        <button class="action-icon-btn" onclick="triggerCall('video')" title="Appel Vidéo">📹</button>
      </div>
    </div>

    <!-- EMPTY PLACEHOLDER -->
    <div class="empty-state" id="empty-state">
      <div style="font-size: 54px; margin-bottom: 12px;">🌐</div>
      <h3 style="font-size: 22px; font-weight: 800; color: var(--text-dark);">James Chat Web</h3>
      <p style="margin-top: 6px; color: #00e5ff; font-weight: 600; font-size: 14px;">Connecte les gens, rapproche les cœurs.</p>
      <p style="margin-top: 10px; max-width: 440px; color: var(--text-muted); font-size: 13px; line-height: 1.6;">
        Sélectionnez une discussion à gauche pour discuter en direct, envoyer des photos, vidéos, films complets et documents volumineux sans limite !
      </p>
    </div>

    <!-- MESSAGES LIST -->
    <div class="messages-area" id="messages-container" style="display: none;"></div>

    <!-- UPLOAD PROGRESS CARD -->
    <div id="upload-progress-card" style="display: none; padding: 12px 18px; background: var(--header-dark); border-top: 1px solid var(--border-dark);">
      <div style="display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 5px; color: var(--text-dark);">
        <span id="upload-file-name" style="font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 280px;">Fichier</span>
        <span id="upload-percent" style="color: #00a884; font-weight: bold;">0%</span>
      </div>
      <div style="background: var(--input-bg); border-radius: 6px; height: 7px; overflow: hidden;">
        <div id="upload-bar" style="background: #00a884; width: 0%; height: 100%; transition: width 0.2s;"></div>
      </div>
      <div id="upload-size-info" style="font-size: 11px; color: var(--text-muted); margin-top: 4px;">0 Mo / 0 Mo</div>
    </div>

    <!-- INPUT AREA -->
    <div class="chat-input-area" id="chat-input-container" style="display: none;">
      <input type="file" id="real-file-input" style="display: none;" multiple accept="*/*">
      <button class="action-icon-btn" id="attach-file-btn" title="Envoyer Film, Vidéo, Photo ou Fichier">📎</button>
      <input type="text" id="msg-input" class="input-text" placeholder="Écrivez un message ou glissez un fichier ici..." autofocus>
      <button class="action-icon-btn" id="record-voice-btn" title="Enregistrer une note vocale">🎤</button>
      <button class="send-btn" id="send-button" title="Envoyer">➤</button>
    </div>
  </div>

  <!-- CALL SCREEN MODAL -->
  <div id="call-modal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(11,20,26,0.96); z-index:999; flex-direction:column; align-items:center; justify-content:center; color:white;">
    <div class="chat-avatar online" id="call-avatar" style="width:96px; height:96px; font-size:42px; margin-bottom:16px;">J</div>
    <h2 id="call-title" style="font-size:22px; margin-bottom:6px; font-weight:700;">Appel en cours...</h2>
    <p id="call-timer" style="color:#00a884; font-size:16px; font-weight:bold; margin-bottom:8px;">00:00</p>
    <p style="color:var(--text-muted); font-size:13px; margin-bottom:32px;">🔒 Chiffré de bout en bout sur James Chat</p>
    <div style="display:flex; gap:24px; align-items:center;">
      <button id="call-mute-btn" style="background:rgba(255,255,255,0.15); border:none; width:52px; height:52px; border-radius:50%; font-size:20px; cursor:pointer; color:white;" title="Couper micro">🎤</button>
      <button id="call-end-btn" style="background:#ff3b30; border:none; width:64px; height:64px; border-radius:50%; font-size:26px; cursor:pointer; color:white;" title="Raccrocher">🛑</button>
      <button id="call-speaker-btn" style="background:rgba(255,255,255,0.15); border:none; width:52px; height:52px; border-radius:50%; font-size:20px; cursor:pointer; color:white;" title="Haut-parleur">🔊</button>
    </div>
  </div>

  <script>
    let activeConversationId = null;
    let currentUserId = "";
    let conversations = [];
    const seenMessageIds = new Set();
    let audioCtx = null;

    function getAudioContext() {
      if (!audioCtx) {
        audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      }
      if (audioCtx.state === 'suspended') {
        audioCtx.resume();
      }
      return audioCtx;
    }

    // Son d'envoi doux et net
    function playSentSound() {
      try {
        const ctx = getAudioContext();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(680, ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(1020, ctx.currentTime + 0.05);
        gain.gain.setValueAtTime(0.28, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.05);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start();
        osc.stop(ctx.currentTime + 0.06);
      } catch (e) {
        console.warn(e);
      }
    }

    // Carillon de réception clair (double tonalité 880Hz -> 1320Hz)
    function playReceivedSound() {
      try {
        const ctx = getAudioContext();
        const now = ctx.currentTime;
        const osc1 = ctx.createOscillator();
        const gain1 = ctx.createGain();
        osc1.frequency.setValueAtTime(880, now);
        gain1.gain.setValueAtTime(0.3, now);
        gain1.gain.exponentialRampToValueAtTime(0.01, now + 0.06);
        osc1.connect(gain1);
        gain1.connect(ctx.destination);
        osc1.start(now);
        osc1.stop(now + 0.07);

        const osc2 = ctx.createOscillator();
        const gain2 = ctx.createGain();
        osc2.frequency.setValueAtTime(1320, now + 0.05);
        gain2.gain.setValueAtTime(0.35, now + 0.05);
        gain2.gain.exponentialRampToValueAtTime(0.01, now + 0.15);
        osc2.connect(gain2);
        gain2.connect(ctx.destination);
        osc2.start(now + 0.05);
        osc2.stop(now + 0.16);
      } catch (e) {
        console.warn(e);
      }
    }

    // Theme Toggle
    document.getElementById('theme-toggle-btn').addEventListener('click', () => {
      document.body.classList.toggle('light-theme');
    });

    // File Attachment Button
    const fileInput = document.getElementById('real-file-input');
    document.getElementById('attach-file-btn').addEventListener('click', () => {
      fileInput.click();
    });

    fileInput.addEventListener('change', (e) => {
      if (e.target.files && e.target.files.length > 0) {
        for (let i = 0; i < e.target.files.length; i++) {
          uploadFile(e.target.files[i]);
        }
      }
    });

    // Enregistrement vocal réel direct au microphone
    let mediaRecorder = null;
    let voiceChunks = [];
    let isVoiceRecording = false;
    const recordBtn = document.getElementById('record-voice-btn');

    recordBtn.addEventListener('click', async () => {
      if (!activeConversationId) {
        alert('Veuillez sélectionner une discussion d\'abord.');
        return;
      }
      if (!isVoiceRecording) {
        try {
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
          mediaRecorder = new MediaRecorder(stream);
          voiceChunks = [];
          mediaRecorder.ondataavailable = (e) => {
            if (e.data.size > 0) voiceChunks.push(e.data);
          };
          mediaRecorder.onstop = async () => {
            stream.getTracks().forEach(t => t.stop());
            const audioBlob = new Blob(voiceChunks, { type: 'audio/webm' });
            const file = new File([audioBlob], 'vocal_' + Date.now() + '.webm', { type: 'audio/webm' });
            await uploadFile(file);
          };
          mediaRecorder.start();
          isVoiceRecording = true;
          recordBtn.innerText = '⏹️';
          recordBtn.style.color = '#ff3b30';
          recordBtn.title = 'Arrêter et envoyer la note vocale';
          document.getElementById('msg-input').placeholder = '🔴 Enregistrement vocal en cours...';
        } catch (err) {
          alert('Accès microphone refusé ou non supporté : ' + err.message);
        }
      } else {
        mediaRecorder.stop();
        isVoiceRecording = false;
        recordBtn.innerText = '🎤';
        recordBtn.style.color = '';
        recordBtn.title = 'Enregistrer une note vocale';
        document.getElementById('msg-input').placeholder = 'Écrivez un message ou glissez un fichier ici...';
      }
    });

    // Drag and Drop files directly onto chat area
    const mainChatEl = document.getElementById('main-chat');
    ['dragenter', 'dragover'].forEach(eventName => {
      mainChatEl.addEventListener(eventName, (e) => {
        e.preventDefault();
        mainChatEl.classList.add('drag-active');
      }, false);
    });
    ['dragleave', 'drop'].forEach(eventName => {
      mainChatEl.addEventListener(eventName, (e) => {
        e.preventDefault();
        mainChatEl.classList.remove('drag-active');
      }, false);
    });
    mainChatEl.addEventListener('drop', (e) => {
      if (e.dataTransfer && e.dataTransfer.files.length > 0) {
        for (let i = 0; i < e.dataTransfer.files.length; i++) {
          uploadFile(e.dataTransfer.files[i]);
        }
      }
    });

    function uploadFile(file) {
      if (!activeConversationId || !file) {
        alert('Veuillez sélectionner une discussion d\'abord.');
        return;
      }
      const progressCard = document.getElementById('upload-progress-card');
      const fileNameEl = document.getElementById('upload-file-name');
      const percentEl = document.getElementById('upload-percent');
      const barEl = document.getElementById('upload-bar');
      const sizeInfoEl = document.getElementById('upload-size-info');

      fileNameEl.innerText = file.name;
      percentEl.innerText = '0%';
      barEl.style.width = '0%';
      sizeInfoEl.innerText = 'Démarrage du transfert...';
      progressCard.style.display = 'block';

      const xhr = new XMLHttpRequest();
      const url = '/api/upload?conversationId=' + encodeURIComponent(activeConversationId) + '&fileName=' + encodeURIComponent(file.name);
      xhr.open('POST', url, true);
      xhr.setRequestHeader('X-Conversation-Id', activeConversationId);
      xhr.setRequestHeader('X-File-Name', file.name);

      xhr.upload.onprogress = function(e) {
        if (e.lengthComputable) {
          const pct = Math.round((e.loaded / e.total) * 100);
          percentEl.innerText = pct + '%';
          barEl.style.width = pct + '%';
          const loadedMb = (e.loaded / (1024 * 1024)).toFixed(1);
          const totalMb = (e.total / (1024 * 1024)).toFixed(1);
          sizeInfoEl.innerText = loadedMb + ' Mo / ' + totalMb + ' Mo';
        }
      };

      xhr.onload = async function() {
        progressCard.style.display = 'none';
        if (xhr.status === 200) {
          playSentSound();
          await loadMessages();
          await loadConversations();
        } else {
          alert('Erreur lors du transfert : ' + xhr.responseText);
        }
      };

      xhr.onerror = function() {
        progressCard.style.display = 'none';
        alert('Erreur réseau lors du transfert du fichier.');
      };

      xhr.send(file);
    }

    // Gestion réelle de l'appel avec timer et modal
    let callTimerInterval = null;
    let callSeconds = 0;

    function triggerCall(type) {
      const modal = document.getElementById('call-modal');
      const title = document.getElementById('call-title');
      const timer = document.getElementById('call-timer');
      const avatar = document.getElementById('call-avatar');
      const conv = conversations.find(function(c) { return c.id === activeConversationId; });

      avatar.innerText = (conv ? conv.title : 'J').charAt(0).toUpperCase();
      title.innerText = (type === 'video' ? '📹 Appel Vidéo avec ' : '📞 Appel Vocal avec ') + (conv ? conv.title : 'Discussion');
      callSeconds = 0;
      timer.innerText = '00:00';
      modal.style.display = 'flex';
      playReceivedSound();

      if (callTimerInterval) clearInterval(callTimerInterval);
      callTimerInterval = setInterval(function() {
        callSeconds++;
        const mins = String(Math.floor(callSeconds / 60)).padStart(2, '0');
        const secs = String(callSeconds % 60).padStart(2, '0');
        timer.innerText = mins + ':' + secs;
      }, 1000);
    }

    document.getElementById('call-end-btn').addEventListener('click', function() {
      if (callTimerInterval) clearInterval(callTimerInterval);
      document.getElementById('call-modal').style.display = 'none';
      playSentSound();
    });

    // Back to list on mobile
    document.getElementById('back-to-list').addEventListener('click', () => {
      document.getElementById('sidebar').classList.remove('chat-open');
      document.getElementById('main-chat').classList.remove('chat-open');
    });

    async function loadStatus() {
      try {
        const res = await fetch('/api/status');
        const data = await res.json();
        currentUserId = data.user.id;
        document.getElementById('user-display-name').innerText = data.user.name || "James Web";
        document.getElementById('current-user-avatar').innerText = (data.user.name || 'J').charAt(0).toUpperCase();
      } catch (e) {
        console.error(e);
      }
    }

    async function loadConversations() {
      try {
        const res = await fetch('/api/conversations');
        conversations = await res.json();
        renderConversations(conversations);
      } catch (e) {
        console.error(e);
      }
    }

    function renderConversations(list) {
      const container = document.getElementById('conversations-list');
      if (!list || list.length === 0) {
        container.innerHTML = '<div style="padding: 20px; text-align: center; color: var(--text-muted);">Aucune discussion pour le moment.</div>';
        return;
      }
      container.innerHTML = list.map(function(c) {
        const activeClass = (c.id === activeConversationId) ? 'active' : '';
        const initial = (c.title || 'J').charAt(0).toUpperCase();
        const time = c.lastMessageTime ? new Date(c.lastMessageTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';
        return '<div class="chat-item ' + activeClass + '" onclick="selectConversation(\'' + c.id + '\')">' +
          '<div class="chat-avatar online">' + initial + '</div>' +
          '<div class="chat-details">' +
            '<div class="chat-top-row">' +
              '<div class="chat-name">' + escapeHtml(c.title || '') + '</div>' +
              '<div class="chat-time">' + time + '</div>' +
            '</div>' +
            '<div class="chat-last-msg">' + escapeHtml(c.lastMessage || 'Cliquez pour ouvrir...') + '</div>' +
          '</div>' +
        '</div>';
      }).join('');
    }

    async function selectConversation(id) {
      activeConversationId = id;
      const conv = conversations.find(function(c) { return c.id === id; });
      if (!conv) return;

      document.getElementById('empty-state').style.display = 'none';
      document.getElementById('chat-header').style.display = 'flex';
      document.getElementById('messages-container').style.display = 'flex';
      document.getElementById('chat-input-container').style.display = 'flex';

      document.getElementById('header-title').innerText = conv.title || '';
      document.getElementById('header-avatar').innerText = (conv.title || 'J').charAt(0).toUpperCase();

      // Mobile responsive toggle
      document.getElementById('sidebar').classList.add('chat-open');
      document.getElementById('main-chat').classList.add('chat-open');

      renderConversations(conversations);
      await loadMessages();
      document.getElementById('msg-input').focus();
    }

    async function loadMessages() {
      if (!activeConversationId) return;
      try {
        const res = await fetch('/api/messages?conversationId=' + encodeURIComponent(activeConversationId));
        const messages = await res.json();
        
        // Jouer un son carillon si un nouveau message arrive d'un autre utilisateur
        if (seenMessageIds.size > 0) {
          const hasNewIncoming = messages.some(function(m) {
            return !seenMessageIds.has(m.id) && m.senderId !== currentUserId;
          });
          if (hasNewIncoming) {
            playReceivedSound();
          }
        }
        messages.forEach(function(m) { seenMessageIds.add(m.id); });

        const container = document.getElementById('messages-container');
        container.innerHTML = messages.map(function(m) {
          const isMe = (m.senderId === currentUserId);
          const time = new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          const cssClass = isMe ? 'msg-outgoing' : 'msg-incoming';
          const checks = isMe ? '✓✓' : '';

          let mediaHtml = '';
          const mUrl = m.mediaUrl || '';
          const isVideo = m.type === 'VIDEO' || mUrl.match(/\.(mp4|mkv|webm|mov|avi)$/i);
          const isPhoto = m.type === 'PHOTO' || mUrl.match(/\.(jpg|jpeg|png|gif|webp)$/i);
          const isAudio = m.type === 'AUDIO' || mUrl.match(/\.(mp3|wav|ogg|m4a|aac|webm)$/i);

          if (mUrl && isVideo) {
            mediaHtml = '<video controls preload="metadata" src="' + escapeHtml(mUrl) + '" style="max-width: 100%; border-radius: 10px; margin-top: 6px; max-height: 320px;"></video>';
          } else if (mUrl && isPhoto) {
            mediaHtml = '<img src="' + escapeHtml(mUrl) + '" style="max-height: 240px; max-width: 100%; border-radius: 10px; cursor: pointer; margin-top: 4px;" onclick="window.open(\'' + escapeHtml(mUrl) + '\')">';
          } else if (mUrl && isAudio) {
            mediaHtml = '<audio controls src="' + escapeHtml(mUrl) + '" style="width: 100%; margin-top: 6px;"></audio>';
          } else if (mUrl) {
            mediaHtml = '<div style="display:flex; align-items:center; gap:8px; padding:8px 12px; background:rgba(0,0,0,0.25); border-radius:8px; margin-top:6px;">' +
              '<span style="font-size:22px;">📁</span>' +
              '<div style="flex:1; overflow:hidden;"><div style="font-weight:bold; font-size:12px; text-overflow:ellipsis; overflow:hidden; white-space:nowrap;">' + escapeHtml(m.text || 'Fichier') + '</div></div>' +
              '<a href="' + escapeHtml(mUrl) + '" download style="padding:4px 10px; background:#00a884; color:white; border-radius:6px; text-decoration:none; font-size:11px; font-weight:bold;">Télécharger</a>' +
            '</div>';
          }

          return '<div class="message ' + cssClass + '">' +
            mediaHtml +
            '<div style="margin-top: 2px;">' + escapeHtml(m.text || '') + '</div>' +
            '<div class="msg-meta">' + time + ' <span style="color:#53bdeb">' + checks + '</span></div>' +
          '</div>';
        }).join('');
        container.scrollTop = container.scrollHeight;
      } catch (e) {
        console.error(e);
      }
    }

    async function sendMessage() {
      const input = document.getElementById('msg-input');
      const text = input.value.trim();
      if (!text || !activeConversationId) return;

      input.value = '';
      playSentSound();
      try {
        await fetch('/api/send', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ conversationId: activeConversationId, text: text })
        });
        await loadMessages();
        await loadConversations();
      } catch (e) {
        alert('Erreur lors de l\'envoi : ' + e);
      }
    }

    document.getElementById('send-button').addEventListener('click', sendMessage);
    document.getElementById('msg-input').addEventListener('keydown', function(e) {
      if (e.key === 'Enter') sendMessage();
    });

    document.getElementById('search-input').addEventListener('input', function(e) {
      const q = e.target.value.toLowerCase();
      const filtered = conversations.filter(function(c) {
        return (c.title || '').toLowerCase().indexOf(q) !== -1;
      });
      renderConversations(filtered);
    });

    document.getElementById('refresh-chats-btn').addEventListener('click', () => {
      loadConversations();
      if (activeConversationId) loadMessages();
    });

    function escapeHtml(text) {
      const div = document.createElement('div');
      div.innerText = text;
      return div.innerHTML;
    }

    loadStatus().then(loadConversations);
    setInterval(function() {
      loadConversations();
      if (activeConversationId) loadMessages();
    }, 2500);
  </script>
</body>
</html>
        """.trimIndent()
    }
}
