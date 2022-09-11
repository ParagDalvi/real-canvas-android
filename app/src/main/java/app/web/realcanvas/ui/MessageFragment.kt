package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.models.Message
import app.web.realcanvas.models.MessageType
import app.web.realcanvas.ui.adapters.MessageAdapter
import app.web.realcanvas.util.hideKeyboard
import app.web.realcanvas.viewmodels.GameViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MessageFragment : Fragment() {

    private lateinit var gameViewModel: GameViewModel
    private lateinit var rvMessage: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var etSend: TextInputLayout
    private val maxChars = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        initUi(view)
        observe()
        return view
    }

    private fun observe() {
        gameViewModel.newMessage.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.addMessage(it)
                if (adapter.messages.isNotEmpty())
                    lifecycleScope.launch { rvMessage.smoothScrollToPosition(adapter.messages.size - 1) }
            }
        }
    }

    private fun initUi(view: View) {
        rvMessage = view.findViewById(R.id.rv_messages)
        etSend = view.findViewById(R.id.et_message)
        etSend.setEndIconOnClickListener { sendMessage() }
        adapter = MessageAdapter(mutableListOf(), gameViewModel.currentPlayer?.userName)
        rvMessage.adapter = adapter
        rvMessage.layoutManager = LinearLayoutManager(context)
        view.findViewById<TextInputEditText>(R.id.edit_text_message)
            .setOnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_SEND) {
                    sendMessage()
                    true
                } else false
            }
    }

    private fun sendMessage() {
        val msg = etSend.editText?.text.toString().trim()
        if (msg.length > maxChars) {
            gameViewModel.showToast("Message too long")
            return
        }
        etSend.editText?.text?.clear()
        if (msg.isEmpty()) return
        if (gameViewModel.currentPlayer == null) return
        gameViewModel.sendMessage(
            Message(
                gameViewModel.currentPlayer!!.userName,
                MessageType.DEFAULT,
                msg
            )
        )
        hideKeyboard(context, activity?.currentFocus)
    }
}