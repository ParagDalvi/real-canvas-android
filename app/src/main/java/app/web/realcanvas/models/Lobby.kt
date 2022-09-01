package app.web.realcanvas.models

import kotlinx.serialization.Serializable

enum class WhatsHappening {
    WAITING, CHOOSING, DRAWING
}

@Serializable
data class Lobby(
    val id: String,
    var players: MutableMap<String, Player>,
    val messages: MutableList<Message>,
    var whatsHappening: WhatsHappening,
    var timer: Short,
    val words: List<String>
)

