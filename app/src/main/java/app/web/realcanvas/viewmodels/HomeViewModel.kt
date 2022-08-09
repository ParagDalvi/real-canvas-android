package app.web.realcanvas.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _userName = MutableLiveData("")
    val userName: LiveData<String> get() = _userName

    private val _code = MutableLiveData("")
    val code: LiveData<String> get() = _code

    fun onUserNameChange(s: String) {
        _userName.value = s
    }

    fun onCodeChange(s: String) {
        _code.value = s
    }
}