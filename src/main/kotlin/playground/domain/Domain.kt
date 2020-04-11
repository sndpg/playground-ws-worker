package playground.domain

import lombok.With

/**
 * Data class to store user data
 */
data class User(
        var givenName: String,
        var lastName: String,
        var userId: Long,
        var username: String
)

/**
 * Data class to store information data.
 */
@With
data class Information(
        var id: Long,
        var name: String,
        var content: String
)