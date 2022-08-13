package app.web.realcanvas.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.web.realcanvas.models.*
import app.web.realcanvas.remote.SocketService
import app.web.realcanvas.util.RESET
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameViewModel : ViewModel() {
    var currentPlayer: Player? = null

    private val _lobby: MutableLiveData<Lobby?> = MutableLiveData()
    val lobby: LiveData<Lobby?> get() = _lobby

    private val _toast: MutableLiveData<String> = MutableLiveData()
    val toast: LiveData<String> get() = _toast

    private val _screen: MutableLiveData<Screen> = MutableLiveData(Screen.HOME)
    val screen: LiveData<Screen> get() = _screen

    private lateinit var client: HttpClient
    private lateinit var socket: WebSocketSession

    companion object {
        const val TAG = "GameViewModel"
    }

    fun startSession(userName: String, lobbyId: String? = null) {
        viewModelScope.launch {
            client = HttpClient(CIO) { install(WebSockets) }
            try {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = SocketService.HOST,
                    port = SocketService.PORT,
                    path = SocketService.PATH
                ) {
                    socket = this
                    val request = createOrJoin(userName, lobbyId)
                    send(Json.encodeToString(request))
                    currentPlayer = Player(userName, lobbyId != null, false)
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val json = frame.readText()
                        onChange(json)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "startSession: ${e.message}")
            } finally {
                socket.close()
                Log.i(TAG, "startSession: Session closed")
            }
        }
    }

    private fun createOrJoin(userName: String, lobbyId: String?): Change {
        return if (lobbyId == null)
            Change(
                type = ChangeType.CREATE,
                createData = CreateData(userName)
            )
        else
            Change(
                type = ChangeType.JOIN,
                joinData = JoinData(userName, lobbyId)
            )
    }

    private fun onChange(json: String) {
        val change = Json.decodeFromString<Change>(json)
        when (change.type) {
            ChangeType.LOBBY_UPDATE -> updateLobby(change)
            ChangeType.ERROR -> handleError(change)
            else -> {}
        }
    }

    private fun handleError(change: Change) {
        val error = change.errorData!!
        _toast.value = error.displayMessage
        if (error.doWhat == RESET) {
            _screen.value = Screen.HOME
        }
    }

    private fun updateLobby(change: Change) {
        currentPlayer = change.lobbyUpdateData!!.data.players[currentPlayer?.userName]
        _lobby.value = change.lobbyUpdateData.data
        navigate(lobby.value?.whatsHappening)
    }

    private fun navigate(whatsHappening: WhatsHappening?) {
        when (whatsHappening) {
            WhatsHappening.WAITING -> _screen.value = Screen.LOBBY
            WhatsHappening.DRAWING -> _screen.value = Screen.GAME
            WhatsHappening.CHOOSING -> _screen.value = Screen.GAME
            else -> _screen.value = Screen.HOME
        }
    }

    fun sendMessage(message: Message) {
        if (lobby.value == null) return
        viewModelScope.launch {
            val returnChange = Change(
                type = ChangeType.LOBBY_UPDATE,
                lobbyUpdateData = LobbyUpdateData(
                    Lobby.addMessage,
                    lobby.value!!.copy(
                        messages = mutableListOf(message),
                        players = mutableMapOf()
                    )
                )
            )
            socket.send(Json.encodeToString(returnChange))
        }
    }

    fun startGame() {
        if (lobby.value == null) return
        viewModelScope.launch {
            val returnChange = Change(
                type = ChangeType.LOBBY_UPDATE,
                lobbyUpdateData = LobbyUpdateData(
                    Lobby.whatsHappening,
                    lobby.value!!.copy(
                        messages = mutableListOf(),
                        players = mutableMapOf(),
                        whatsHappening = WhatsHappening.CHOOSING
                    )
                )
            )
            socket.send(Json.encodeToString(returnChange))
        }
    }

    fun disconnect() {
        if (lobby.value == null || currentPlayer == null) return
        viewModelScope.launch {
            val returnChange = Change(
                type = ChangeType.DISCONNECT,
                disconnectData = DisconnectData(lobby.value!!.id, currentPlayer!!.userName)
            )
            socket.send(Json.encodeToString(returnChange))
            clearData()
        }
    }

    private suspend fun clearData() {
        socket.close()
        _lobby.value = null
        currentPlayer = null
    }
}