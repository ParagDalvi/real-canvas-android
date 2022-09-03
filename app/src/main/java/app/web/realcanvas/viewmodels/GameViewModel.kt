package app.web.realcanvas.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.web.realcanvas.models.*
import app.web.realcanvas.util.HOST
import app.web.realcanvas.util.PATH
import app.web.realcanvas.util.PORT
import app.web.realcanvas.util.RESET
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameViewModel : ViewModel() {
    var currentPlayer: Player? = null

    private val _currentLobby: MutableLiveData<Lobby?> = MutableLiveData(null)
    val currentLobby: LiveData<Lobby?> get() = _currentLobby

    private val _drawingData: MutableLiveData<DrawingData> = MutableLiveData()
    val drawingData: LiveData<DrawingData> get() = _drawingData

    private val _newMessage: MutableLiveData<Message> = MutableLiveData()
    val newMessage: LiveData<Message> get() = _newMessage

    var currentlySelectedWord: String? = null

    private val _toast: MutableLiveData<String> = MutableLiveData()
    val toast: LiveData<String> get() = _toast

    private val _screen: MutableLiveData<Screen> = MutableLiveData(Screen.HOME)
    val screen: LiveData<Screen> get() = _screen

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private lateinit var client: HttpClient
    private lateinit var socket: WebSocketSession

    companion object {
        const val TAG = "GameViewModel"
    }

    fun startSession(userName: String, lobbyId: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            client = HttpClient(CIO) { install(WebSockets) }
            try {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = HOST,
                    port = PORT,
                    path = PATH
                ) {
                    socket = this
                    val request = createOrJoin(userName, lobbyId)
                    send(Json.encodeToString(request))
                    currentPlayer = Player(userName, lobbyId != null, false, 0)
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val json = frame.readText()
                        onChange(json)
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "startSession: ${e.message}")
                _isLoading.value = false
                //todo: show error
            } finally {
                if (::socket.isInitialized && socket.isActive)
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
            ChangeType.DRAWING -> handleDrawingPoints(change)
            ChangeType.MESSAGE -> handleNewMessage(change)
            ChangeType.SELECTED_WORD -> handleSelectedWord(change)
            else -> {}
        }
    }

    private fun handleSelectedWord(change: Change) {
        currentlySelectedWord = change.selectedWordData!!.word
    }

    private fun handleNewMessage(change: Change) {
        if (currentLobby.value == null || currentPlayer == null) return

        _newMessage.value = change.messageData!!.message
    }

    private fun handleDrawingPoints(change: Change) {
        val data = change.drawingData!!
        _drawingData.value = data
    }

    private fun handleError(change: Change) {
        val error = change.errorData!!
        _toast.value = error.displayMessage
        if (error.doWhat == RESET) {
            _screen.value = Screen.HOME
        }
    }

    private fun updateLobby(change: Change) {
        val receivedLobby = change.lobbyUpdateData!!
        checkIfAnyPlayerLeft(currentLobby.value?.players, receivedLobby.players)
        _currentLobby.value = receivedLobby
        updateCurrentPlayer()
        navigate()
    }

    private fun checkIfAnyPlayerLeft(
        oldPlayers: MutableMap<String, Player>?,
        newPlayers: MutableMap<String, Player>
    ) {
        oldPlayers?.keys?.forEach {
            if (!newPlayers.containsKey(it)) {
                showToast("$it left")
                return
            }
        }
    }

    private fun updateCurrentPlayer() {
        if (currentLobby.value == null) {
            currentPlayer = null
            return
        }
        currentPlayer = currentLobby.value!!.players[currentPlayer?.userName]
    }

    private fun navigate() {
        when (currentLobby.value?.whatsHappening) {
            WhatsHappening.WAITING -> _screen.value = Screen.LOBBY
            WhatsHappening.DRAWING -> _screen.value = Screen.GAME
            WhatsHappening.CHOOSING -> _screen.value = Screen.GAME
            else -> _screen.value = Screen.HOME
        }
    }

    fun sendMessage(message: Message) {
        if (currentLobby.value == null) return
        viewModelScope.launch {
            val returnChange = Change(
                type = ChangeType.MESSAGE,
                messageData = MessageData(
                    currentLobby.value!!.id,
                    currentlySelectedWord,
                    currentPlayer?.userName,
                    message
                )
            )
            socket.send(Json.encodeToString(returnChange))
        }
    }

    fun startGame() {
        if (currentLobby.value == null) return
        viewModelScope.launch {
            currentLobby.value!!.whatsHappening = WhatsHappening.CHOOSING
            val returnChange = Change(
                type = ChangeType.LOBBY_UPDATE,
                lobbyUpdateData = currentLobby.value
            )
            socket.send(Json.encodeToString(returnChange))
        }
    }

    fun disconnect() {
        if (currentLobby.value == null || currentPlayer == null) return
        viewModelScope.launch {
            val returnChange = Change(
                type = ChangeType.DISCONNECT,
                disconnectData = DisconnectData(currentLobby.value!!.id, currentPlayer!!.userName)
            )
            socket.send(Json.encodeToString(returnChange))
            clearData()
        }
    }

    private suspend fun clearData() {
        //todo: call when returning home
        socket.close()
        _currentLobby.value = null
        currentPlayer = null
    }

    fun sendDrawingPath(list: List<DrawPoints>, doWhatWhenDrawing: DoWhatWhenDrawing) {
        if (currentPlayer == null) return
        viewModelScope.launch {
            val change = Change(
                ChangeType.DRAWING,
                drawingData = DrawingData(
                    currentLobby.value!!.id,
                    currentPlayer!!.userName,
                    doWhatWhenDrawing,
                    list
                )
            )
            socket.send(Json.encodeToString(change))
        }
    }

    fun updateSelectedWord(index: Int) {
        if (currentLobby.value == null) return
        viewModelScope.launch {
            val change = Change(
                ChangeType.SELECTED_WORD,
                selectedWordData = SelectedWordData(
                    currentLobby.value!!.id,
                    currentLobby.value!!.words[index - 1]
                )
            )
            socket.send(Json.encodeToString(change))
        }
    }

    fun showToast(s: String) {
        _toast.value = s
    }
}