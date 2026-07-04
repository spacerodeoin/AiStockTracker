package com.aitracker.app.data.remote.realtime

/** Lifecycle of the real-time feed, surfaced to the UI via the live status pill. */
enum class ConnectionState {
    /** Socket is opening / no data yet. */
    Connecting,

    /** Connected to the real provider WebSocket and receiving live trades. */
    LiveReal,

    /** Serving the simulated fallback feed (no key, or market closed / provider silent). */
    LiveDemo,

    /** User paused the stream; client has sent unsubscribe frames. */
    Paused,

    /** Not connected — connection failed and could not be recovered. */
    Offline,
}
