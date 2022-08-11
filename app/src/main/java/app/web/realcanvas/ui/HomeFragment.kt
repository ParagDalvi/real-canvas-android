package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.web.realcanvas.R
import app.web.realcanvas.viewmodels.GameViewModel

class HomeFragment : Fragment() {
    private lateinit var gameViewModel: GameViewModel
    private lateinit var etUserName: EditText
    private lateinit var etCode: EditText
    private lateinit var btnCreateOrJoin: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        intiUi(view)
        initViewModels()
        observe()
        return view
    }

    private fun observe() {

    }

    private fun initViewModels() {
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
    }

    private fun intiUi(view: View) {
        btnCreateOrJoin = view.findViewById(R.id.btn_create_join)
        btnCreateOrJoin.setOnClickListener { createOrJoin() }
        etUserName = view.findViewById(R.id.et_username)
        etCode = view.findViewById(R.id.et_lobby_code)
    }

    private fun createOrJoin() {
        val userName = etUserName.text.toString().trim()
        val code = etCode.text.toString().trim()

        if (userName.isEmpty()) {
            Toast.makeText(context, "Please enter Username", Toast.LENGTH_SHORT).show()
            return
        }

        if (userName.length <= 2) {
            Toast.makeText(context, "Please enter at least 3 letters", Toast.LENGTH_SHORT).show()
            return
        }

        gameViewModel.startSession(userName, code.ifEmpty { null })
    }
}
