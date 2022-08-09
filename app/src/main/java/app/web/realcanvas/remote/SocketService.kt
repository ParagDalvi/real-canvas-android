package app.web.realcanvas.remote

import androidx.lifecycle.MutableLiveData
import app.web.realcanvas.util.Resource

interface SocketService<T> {
    companion object {
        const val HOST = "192.168.29.244"
        const val PORT = 8080
        const val PATH = "/"
    }

    suspend fun initSocketSession()

    suspend fun closeSession()

    fun listen(): MutableLiveData<Resource<T>>
}