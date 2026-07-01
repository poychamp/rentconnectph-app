package ph.rentconnect.app.core.util

import java.net.URI
import java.net.URLDecoder

/**
 * Destinations reachable from a verified https://rentconnectph.com deep link.
 * Framework-agnostic on purpose so it stays a pure-JVM unit (no android.net.Uri).
 */
sealed interface DeepLinkTarget {
    data object Home : DeepLinkTarget

    data class Search(
        val q: String? = null,
        val area: String? = null,
        val type: String? = null,
        val budgetMin: Int? = null,
        val budgetMax: Int? = null,
    ) : DeepLinkTarget

    data object About : DeepLinkTarget

    data object Contact : DeepLinkTarget

    data class Detail(val uuid: String) : DeepLinkTarget
}

/**
 * Maps a full URL to its in-app destination. Allowlist only — any path the app
 * does not own (/privacy, /terms, /admin, /api, unknowns) returns null so the
 * caller leaves it for the browser. Malformed URLs also return null.
 */
fun parseDeepLink(url: String): DeepLinkTarget? {
    val uri = runCatching { URI(url) }.getOrNull() ?: return null
    return when (val path = uri.path.orEmpty().trimEnd('/')) {
        "" -> DeepLinkTarget.Home
        "/search" -> {
            val params = parseQuery(uri.rawQuery)
            DeepLinkTarget.Search(
                q = params["q"]?.ifBlank { null },
                area = params["area"]?.ifBlank { null },
                type = params["type"]?.ifBlank { null },
                budgetMin = params["budget_min"]?.toIntOrNull(),
                budgetMax = params["budget_max"]?.toIntOrNull(),
            )
        }
        "/about" -> DeepLinkTarget.About
        "/contact" -> DeepLinkTarget.Contact
        else -> {
            val segments = path.trim('/').split('/')
            if (segments.size == 2 && segments[0] == "listings" && segments[1].isNotBlank()) {
                DeepLinkTarget.Detail(uuid = segments[1])
            } else {
                null
            }
        }
    }
}

private fun parseQuery(rawQuery: String?): Map<String, String> {
    if (rawQuery.isNullOrBlank()) return emptyMap()
    return rawQuery.split('&').mapNotNull { pair ->
        val separator = pair.indexOf('=')
        if (separator < 0) return@mapNotNull null
        val key = pair.substring(0, separator)
        if (key.isBlank()) return@mapNotNull null
        key to decode(pair.substring(separator + 1))
    }.toMap()
}

private fun decode(value: String): String =
    runCatching { URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)
