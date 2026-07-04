package com.aitracker.app.data.remote.realtime

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Real-time trade feed backed by Finnhub's WebSocket API (`wss://ws.finnhub.io?token=KEY`).
 *
 * Outbound (client -> server): [subscribe] / [unsubscribe] send one JSON frame per symbol.
 * Inbound (server -> client): trade frames are parsed by [parseFinnhubTrades] into [PriceTick]s.
 * On failure the socket reconnects with exponential backoff and re-subscribes the active symbols.
 */
class FinnhubWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val apiKey: String,
    private val gson: Gson = Gson(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : RealtimeQuoteService {

    override val providerName: String = "Finnhub (WebSocket)"

    private val _connectionState = MutableStateFlow(ConnectionState.Connecting)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _ticks = MutableSharedFlow<PriceTick>(extraBufferCapacity = 256)
    override val ticks: SharedFlow<PriceTick> = _ticks.asSharedFlow()

    private val subscribed = CopyOnWriteArraySet<String>()

    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var closed = false
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0

    override fun prime(basePrices: Map<String, Double>) {
        // Real trades carry their own prices; nothing to seed.
    }

    override fun connect() {
        if (webSocket != null || closed) return
        _connectionState.value = ConnectionState.Connecting
        val request = Request.Builder()
            .url("$WS_BASE_URL?token=$apiKey")
            .build()
        webSocket = okHttpClient.newWebSocket(request, Listener())
    }

    override fun subscribe(symbols: Collection<String>) {
        val added = symbols.filter { subscribed.add(it) }
        val socket = webSocket ?: return
        added.forEach { socket.send(frame("subscribe", it)) }
    }

    override fun unsubscribe(symbols: Collection<String>) {
        val socket = webSocket
        symbols.forEach { symbol ->
            if (subscribed.remove(symbol)) socket?.send(frame("unsubscribe", symbol))
        }
    }

    override fun disconnect() {
        closed = true
        reconnectJob?.cancel()
        webSocket?.close(NORMAL_CLOSURE, "client disconnect")
        webSocket = null
        scope.cancel()
    }

    private fun frame(type: String, symbol: String): String =
        gson.toJson(mapOf("type" to type, "symbol" to symbol))

    private fun scheduleReconnect() {
        if (closed) return
        webSocket = null
        val attempt = reconnectAttempts++
        if (attempt >= MAX_RECONNECT_ATTEMPTS) {
            _connectionState.value = ConnectionState.Offline
            return
        }
        _connectionState.value = ConnectionState.Connecting
        val backoffMillis = (BASE_BACKOFF_MILLIS shl attempt).coerceAtMost(MAX_BACKOFF_MILLIS)
        reconnectJob = scope.launch {
            delay(backoffMillis)
            connect()
        }
    }

    private inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            reconnectAttempts = 0
            _connectionState.value = ConnectionState.LiveReal
            // Re-subscribe everything the client currently wants.
            subscribed.forEach { webSocket.send(frame("subscribe", it)) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val ticks = runCatching { parseFinnhubTrades(text) }.getOrDefault(emptyList())
            if (ticks.isNotEmpty()) _connectionState.value = ConnectionState.LiveReal
            ticks.forEach { _ticks.tryEmit(it) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "WebSocket failure: ${t.message}")
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (!closed) scheduleReconnect()
        }
    }

    companion object {
        const val WS_BASE_URL = "wss://ws.finnhub.io"
        private const val NORMAL_CLOSURE = 1000
        private const val TAG = "FinnhubWS"
        private const val MAX_RECONNECT_ATTEMPTS = 6
        private const val BASE_BACKOFF_MILLIS = 1_000L
        private const val MAX_BACKOFF_MILLIS = 30_000L
    }
}

/**
 * Pure parser for a Finnhub WebSocket text frame. Returns the [PriceTick]s contained in a
 * `{"type":"trade","data":[{"s","p","t"}]}` message, or an empty list for `ping` / other /
 * malformed frames. Kept free of any socket/Android dependency so it is unit-testable.
 */
fun parseFinnhubTrades(text: String): List<PriceTick> {
    val root = runCatching { JsonParser.parseString(text) }.getOrNull()?.takeIf { it.isJsonObject }
        ?.asJsonObject ?: return emptyList()
    if (root.get("type")?.asString != "trade") return emptyList()
    val data = root.get("data")?.takeIf { it.isJsonArray }?.asJsonArray ?: return emptyList()
    return data.mapNotNull { element ->
        val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
        val symbol = obj.get("s")?.asString ?: return@mapNotNull null
        val price = obj.get("p")?.asDouble ?: return@mapNotNull null
        val timestamp = obj.get("t")?.asLong ?: 0L
        PriceTick(symbol = symbol, price = price, timestampMillis = timestamp)
    }
}
