package ph.rentconnect.app.core.util

fun isAppOutdated(appVersion: String, minVersion: String): Boolean {
    val min = parseVersion(minVersion) ?: return false
    val app = parseVersion(appVersion) ?: return false
    return app < min
}

private fun parseVersion(version: String): List<Int>? {
    if (version.isBlank()) return null
    val parts = version.split(".")
    if (parts.any { it.toIntOrNull() == null }) return null
    val segments = parts.map { it.toInt() }
    return List(3) { segments.getOrElse(it) { 0 } }
}

private operator fun List<Int>.compareTo(other: List<Int>): Int {
    for (i in indices) {
        val cmp = this[i].compareTo(other[i])
        if (cmp != 0) return cmp
    }
    return 0
}
