package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.models.Message
import app.web.realcanvas.models.MessageType
import app.web.realcanvas.ui.adapters.MessageAdapter
import app.web.realcanvas.viewmodels.GameViewModel
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MessageFragment : Fragment() {

    companion object {
        const val TAG = "MessageFragment"
    }

    private lateinit var gameViewModel: GameViewModel
    private lateinit var rvMessage: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var btnSend: ImageButton
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
        adapter = MessageAdapter(mutableListOf(), gameViewModel.currentPlayer?.userName)
        rvMessage.adapter = adapter
        rvMessage.layoutManager = LinearLayoutManager(context)
        btnSend = view.findViewById(R.id.btn_send_message)
        btnSend.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        val msg = etSend.editText?.text.toString().trim()
        etSend.editText?.text?.clear()
        if (msg.isEmpty() || msg.length > maxChars) return
        if (gameViewModel.currentPlayer == null) return
        gameViewModel.sendMessage(
            Message(
                gameViewModel.currentPlayer!!.userName,
                MessageType.DEFAULT,
                msg
            )
        )
    }
}