package app.web.realcanvas.models

import kotlinx.serialization.Serializable

enum class ChangeType {
    CREATE, JOIN, DISCONNECT, LOBBY_UPDATE, ERROR, DRAWING, MESSAGE
}

@Serializable
data class Change(
    val type: ChangeType,
    val createData: CreateData? = null,
    val joinData: JoinData? = null,
    val disconnectData: DisconnectData? = null,
    val lobbyUpdateData: Lobby? = null,
    val errorData: ErrorData? = null,
    val drawingData: DrawingData? = null,
    val messageData: MessageData? = null
)

@Serializable
data class CreateData(
    val userName: String
)

@Serializable
data class JoinData(
    val userName: String,
    val lobbyId: String
)

@Serializable
data class DisconnectData(
    val lobbyId: String,
    val playerId: String
)

@Serializable
data class ErrorData(
    val message: String,
    val displayMessage: String,
    val where: String,
    val doWhat: String
)

@Serializable
data class DrawingData(
    val lobbyId: String,
    val userName: String,
    val list: List<DrawPoints>
)

@Serializable
data class MessageData(
    val lobbyId: String,
    val message: Message
)