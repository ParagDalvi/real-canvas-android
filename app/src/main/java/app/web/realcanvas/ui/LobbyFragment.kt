package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.ui.adapters.LobbyAdapter
import app.web.realcanvas.viewmodels.GameViewModel

class LobbyFragment : Fragment() {
    private lateinit var gameViewModel: GameViewModel
    private lateinit var rvLobby: RecyclerView
    private lateinit var lobbyAdapter: LobbyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lobby, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        initUi(view)
        observe()
        return view
    }

    private fun observe() {
        gameViewModel.lobby.observe(viewLifecycleOwner) {
            it?.players?.values?.toList()?.let { it1 -> lobbyAdapter.updatePlayers(it1) }
        }
    }

    private fun initUi(view: View) {
        rvLobby = view.findViewById(R.id.rv_lobby)
        lobbyAdapter = LobbyAdapter(listOf())
        rvLobby.adapter = lobbyAdapter
        rvLobby.layoutManager = LinearLayoutManager(context)
    }
}