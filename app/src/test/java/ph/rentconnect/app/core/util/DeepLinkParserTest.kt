package ph.rentconnect.app.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DeepLinkParserTest {

    private val host = "https://rentconnectph.com"

    // --- Home ---

    @Test
    fun `it maps root path to Home`() {
        assertEquals(DeepLinkTarget.Home, parseDeepLink("$host/"))
    }

    @Test
    fun `it maps bare host with no path to Home`() {
        assertEquals(DeepLinkTarget.Home, parseDeepLink(host))
    }

    // --- Search ---

    @Test
    fun `it maps bare search path to Search with all null filters`() {
        assertEquals(
            DeepLinkTarget.Search(null, null, null, null, null),
            parseDeepLink("$host/search"),
        )
    }

    @Test
    fun `it maps a full search url to Search with all filters`() {
        assertEquals(
            DeepLinkTarget.Search(
                q = "asd",
                area = "bayabas",
                type = "house,studio",
                budgetMin = 7000,
                budgetMax = 20000,
            ),
            parseDeepLink("$host/search?q=asd&budget_min=7000&budget_max=20000&area=bayabas&type=house%2Cstudio"),
        )
    }

    @Test
    fun `it decodes a url-encoded csv type into a comma joined string`() {
        val result = parseDeepLink("$host/search?type=house%2Cstudio%2Ccondo") as DeepLinkTarget.Search
        assertEquals("house,studio,condo", result.type)
    }

    @Test
    fun `it treats a blank query param as null`() {
        val result = parseDeepLink("$host/search?q=&area=") as DeepLinkTarget.Search
        assertNull(result.q)
        assertNull(result.area)
    }

    @Test
    fun `it maps a malformed budget to null instead of crashing`() {
        val result = parseDeepLink("$host/search?budget_min=abc&budget_max=20000") as DeepLinkTarget.Search
        assertNull(result.budgetMin)
        assertEquals(20000, result.budgetMax)
    }

    // --- About / Contact ---

    @Test
    fun `it maps about path to About`() {
        assertEquals(DeepLinkTarget.About, parseDeepLink("$host/about"))
    }

    @Test
    fun `it maps contact path to Contact`() {
        assertEquals(DeepLinkTarget.Contact, parseDeepLink("$host/contact"))
    }

    // --- Detail ---

    @Test
    fun `it maps a listing path to Detail with the uuid`() {
        assertEquals(
            DeepLinkTarget.Detail(uuid = "019e4fd0-f987-7354-a404-d0073a06dfa5"),
            parseDeepLink("$host/listings/019e4fd0-f987-7354-a404-d0073a06dfa5"),
        )
    }

    @Test
    fun `it returns null for a listings path with no uuid`() {
        assertNull(parseDeepLink("$host/listings"))
    }

    // --- Excluded routes (must fall through to browser) ---

    @Test
    fun `it returns null for privacy`() {
        assertNull(parseDeepLink("$host/privacy"))
    }

    @Test
    fun `it returns null for terms`() {
        assertNull(parseDeepLink("$host/terms"))
    }

    @Test
    fun `it returns null for admin`() {
        assertNull(parseDeepLink("$host/admin"))
    }

    @Test
    fun `it returns null for an api path`() {
        assertNull(parseDeepLink("$host/api/v1/listings/019e4fd0-f987-7354-a404-d0073a06dfa5"))
    }

    // --- Robustness ---

    @Test
    fun `it tolerates a trailing slash on a known path`() {
        assertEquals(DeepLinkTarget.About, parseDeepLink("$host/about/"))
    }

    @Test
    fun `it returns null for an unknown path`() {
        assertNull(parseDeepLink("$host/foo"))
    }

    @Test
    fun `it returns null for a malformed url`() {
        assertNull(parseDeepLink("ht!tp://[not a url"))
    }
}
