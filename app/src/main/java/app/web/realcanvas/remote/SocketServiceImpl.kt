package app.web.realcanvas.remote

import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.web.realcanvas.util.Resource
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*

class SocketServiceImpl<T>(
    private val client: HttpClient
) : SocketService<T> {

    companion object {
        const val TAG = "SocketServiceImpl"
    }

    private lateinit var socket: WebSocketSession
    private val result: MutableLiveData<Resource<T>> = MutableLiveData()

    override fun listen(): MutableLiveData<Resource<T>> = result

    override suspend fun initSocketSession() {
        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = SocketService.HOST,
                port = SocketService.PORT,
                path = SocketService.PATH
            ) {
                socket = this

                val temp = "{\n" +
                        "    \"type\": \"CREATE\",\n" +
                        "    \"createData\": {\n" +
                        "        \"userName\": \"PARoAG\"\n" +
                        "    },\n" +
                        "    \"joinData\": null,\n" +
                        "    \"disconnectData\": null\n" +
                        "}"
                send(temp)
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val json = frame.readText()
                    Log.e(TAG, "initSocketSession: $json")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "initSocketSession: ${e.message}")
        } finally {
            socket.close()
        }
    }

    override suspend fun closeSession() {
        TODO("Not yet implemented")
    }
}