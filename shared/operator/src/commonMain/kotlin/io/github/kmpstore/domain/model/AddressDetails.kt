package io.github.kmpstore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressDetails(
    val city: String?,
    val line1: String?,
    val line2: String?,
    val state: String?,
    val country: String?,
    @SerialName("postal_code") val postalCode: String?
) {
    fun toFormattedString(): String {
        val sb = StringBuilder()

        line1?.let { sb.appendLine(it) }
        line2?.let { sb.appendLine(it) }

        val cityLine = listOfNotNull(city, state, postalCode, country)
            .joinToString(" ")
        if (cityLine.isNotBlank()) sb.appendLine(cityLine)

        return sb.toString().trim()
    }
}