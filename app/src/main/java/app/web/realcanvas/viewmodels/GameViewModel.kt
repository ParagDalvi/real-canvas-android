package app.web.realcanvas.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.web.realcanvas.models.*
import app.web.realcanvas.remote.SocketService
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
    private val _lobby: MutableLiveData<Lobby?> = MutableLiveData()
    val lobby: LiveData<Lobby?> get() = _lobby

    private val _gameState: MutableLiveData<GameState> = MutableLiveData()
    val gameState: LiveData<GameState> get() = _gameState

    private lateinit var client: HttpClient
    private lateinit var socket: WebSocketSession
    private val json = Json { encodeDefaults = true }

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
                    send(json.encodeToString(request))
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
            else -> {}
        }
    }

    private fun updateLobby(change: Change) {
        _lobby.value = change.lobbyUpdateData

        // navigate if required
        when (change.gameState) {
            GameState.LOBBY -> {
                if (gameState.value != GameState.LOBBY)
                    _gameState.value = GameState.LOBBY
            }
            else -> {
                Log.e(TAG, "updateLobby: Did not find GameState")
            }
        }
    }
}