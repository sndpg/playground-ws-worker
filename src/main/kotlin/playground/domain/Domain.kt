package playground.domain

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
data class Information(
        var id: Long,
        var name: String,
        var content: String
)
