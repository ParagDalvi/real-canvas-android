package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.web.realcanvas.R
import app.web.realcanvas.viewmodels.GameViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class HomeFragment : Fragment() {
    private lateinit var gameViewModel: GameViewModel
    private lateinit var etUserName: TextInputLayout
    private lateinit var etCode: TextInputLayout
    private lateinit var btnCreateOrJoin: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        intiUi(view)
        observe()
        return view
    }

    private fun observe() {
        gameViewModel.isLoading.observe(viewLifecycleOwner) {
            if (it != null) {
                btnCreateOrJoin.isEnabled = !it
            }
        }
    }

    private fun intiUi(view: View) {
        btnCreateOrJoin = view.findViewById(R.id.btn_create_join)
        btnCreateOrJoin.setOnClickListener { createOrJoin() }
        etUserName = view.findViewById(R.id.et_username)
        etCode = view.findViewById(R.id.et_lobby_code)
        view.findViewById<TextInputEditText>(R.id.edit_text_code)
            .setOnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_GO) {
                    createOrJoin()
                    true
                } else false
            }
    }

    private fun createOrJoin() {
        if (gameViewModel.isLoading.value == true) return

        val userName = etUserName.editText?.text.toString().trim()
        val code = etCode.editText?.text.toString().trim()

        if (userName.isEmpty()) {
            gameViewModel.showToast("Please enter Username")
            return
        }

        if (userName.length <= 2) {
            gameViewModel.showToast("Username is short")
            return
        }

        gameViewModel.startSession(userName, code.ifEmpty { null })
    }
}
