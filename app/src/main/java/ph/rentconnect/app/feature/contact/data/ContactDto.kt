package ph.rentconnect.app.feature.contact.data

import kotlinx.serialization.Serializable

@Serializable
data class ContactRequest(
    val name: String,
    val email: String,
    val message: String,
)

@Serializable
data class ContactValidationErrorResponse(
    val message: String,
    val errors: Map<String, List<String>> = emptyMap(),
)
