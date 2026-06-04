package ph.rentconnect.app.core.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VersionComparatorTest {

    @Test
    fun `it returns true when app version equals min version`() {
        assertFalse(isAppOutdated(appVersion = "1.0.0", minVersion = "1.0.0"))
    }

    @Test
    fun `it returns true when app version is above min version`() {
        assertFalse(isAppOutdated(appVersion = "1.1.0", minVersion = "1.0.0"))
    }

    @Test
    fun `it returns true when app version is below min version by patch`() {
        assertTrue(isAppOutdated(appVersion = "1.0.0", minVersion = "1.0.1"))
    }

    @Test
    fun `it returns true when app version is below min version by minor`() {
        assertTrue(isAppOutdated(appVersion = "1.0.0", minVersion = "1.1.0"))
    }

    @Test
    fun `it returns true when app version is below min version by major`() {
        assertTrue(isAppOutdated(appVersion = "1.0.0", minVersion = "2.0.0"))
    }

    @Test
    fun `it returns false when app has higher major but lower minor`() {
        assertFalse(isAppOutdated(appVersion = "2.0.0", minVersion = "1.9.9"))
    }

    @Test
    fun `it returns false when min version is empty`() {
        assertFalse(isAppOutdated(appVersion = "1.0.0", minVersion = ""))
    }

    @Test
    fun `it returns false when min version is malformed`() {
        assertFalse(isAppOutdated(appVersion = "1.0.0", minVersion = "abc"))
    }

    @Test
    fun `it handles two-segment version like 1 dot 0`() {
        assertFalse(isAppOutdated(appVersion = "1.0", minVersion = "1.0.0"))
    }

    @Test
    fun `it handles missing patch in min version`() {
        assertTrue(isAppOutdated(appVersion = "0.9.0", minVersion = "1.0"))
    }
}
