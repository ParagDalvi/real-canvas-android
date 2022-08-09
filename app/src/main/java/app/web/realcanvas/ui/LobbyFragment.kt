package app.web.realcanvas.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import app.web.realcanvas.R
import app.web.realcanvas.viewmodels.GameViewModel

class LobbyFragment : Fragment() {
    private lateinit var gameViewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lobby, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]

        gameViewModel.lobby.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.temp).text = it.toString()
        }

        return view
    }
}