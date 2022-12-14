package app.web.realcanvas.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val userName: String,
    val isAdmin: Boolean,
    var isDrawing: Boolean,
    var score: Int
)
